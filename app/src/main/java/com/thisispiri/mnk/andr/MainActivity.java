package com.thisispiri.mnk.andr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.SwitchCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.thisispiri.dialogs.ChecksDialogFragment;
import com.thisispiri.dialogs.DecisionDialogFragment;
import com.thisispiri.dialogs.DialogListener;
import com.thisispiri.dialogs.EditTextDialogFragment;
import com.thisispiri.mnk.EmacsGomokuAi;
import com.thisispiri.mnk.FillerMnkAi;
import com.thisispiri.mnk.IoThread;
import com.thisispiri.mnk.LegalGravityMnkGame;
import com.thisispiri.mnk.LegalMnkGame;
import com.thisispiri.mnk.MnkAi;
import com.thisispiri.mnk.MnkAiDecision;
import com.thisispiri.mnk.MnkGame;
import com.thisispiri.mnk.MnkManager;
import com.thisispiri.mnk.MnkSaveLoader;
import com.thisispiri.mnk.PiriMnkAi;
import com.thisispiri.mnk.Point;
import com.thisispiri.mnk.R;
import com.thisispiri.util.AndroidUtilsKt;
import com.thisispiri.util.GameTimer;
import com.thisispiri.util.TimedGameManager;

import static com.thisispiri.mnk.IoThread.*;
import static com.thisispiri.util.AndroidUtilsKt.bundleWith;
import static com.thisispiri.util.AndroidUtilsKt.getFile;
import static com.thisispiri.util.AndroidUtilsKt.getPermission;
import static com.thisispiri.util.AndroidUtilsKt.showToast;

//TODO: Decouple more things from Android and Bluetooth
/**The main {@code Activity} for PIRI MNK. Handles all interactions between the UI, communications and game logic.*/
public class MainActivity extends AppCompatActivity implements MnkManager, TimedGameManager, DialogListener {
	private DebugBoard board;
	private Highlighter highlighter;
	private MnkGame game;
	private MnkAi ai;
	private Button buttonFill, buttonAI, buttonLoad;
	private TextView winText;
	private SwitchCompat useAI;
	private View parentView;
	private RadioButton radioLocal, radioBluetooth; //onCheckedChanged() may be called more than once if we use RadioGroup.check(). Do not replace this with radioPlayers.
	private RadioGroup rGroup;
	private final ButtonListener bLis = new ButtonListener();
	private final RadioListener rLis = new RadioListener();
	/**The {@code Thread} used to asynchronously fill all cells when the "fill all" button is pressed.*/
	private FillThread fillThread;
	/**The {@code Thread} used to communicate with another client via Bluetooth.*/
	private IoThread bluetoothThread;
	/**The {@code Handler} used to handle invalidation requests from {@link MainActivity#fillThread}.*/
	private final Handler fillHandler = new FillHandler(this);
	/**The {@code CountDownTimer} for implementing the time limit.*/
	private GameTimer limitTimer = new GameTimer(this, -1); //The first instance is replaced in setTimeLimit()
	private final MnkSaveLoader saveLoader = new MnkSaveLoader();
	private final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
	private BluetoothSocket socket;
	private boolean gameEnd = false;
	private boolean onBluetooth = false;
	/**Indicates if the game's in a latency offset. See the doc of GameTimer for details about it.*/
	private boolean preventPlaying = false;
	private boolean enableHighlight;
	private boolean enableTimeLimit;
	private boolean showAiInternals;
	/**1 if gravity is enabled. Else 0.*/
	private int gravity;
	/**1 if matching only lines with exactly winStreak stones. Else 0.*/
	private int exactOnly;
	/**Used to tie myTurn to a specific Shape in Bluetooth mode.*/
	private int myIndex = 0;
	/**The width of the screen, for updating custom {@code View}s.*/
	private int screenX;
	/**The time limit in milliseconds.*/
	private int timeLimit;
	/**The previous time limit, used to tell if we should stop the timer because of a change in the time limit.*/
	private int previousTimeLimit = -1;
	/**Indicates what {@code Dialog} to show on next {@link MainActivity#onResumeFragments}. Needed because {@code IllegalStateException} is thrown when {@code DialogFragment.show()} is called inside some methods.
	 * Values for this field can be {@link MainActivity#SAVE_REQUEST_CODE}, {@link MainActivity#LOAD_REQUEST_CODE}, {@link MainActivity#LOCATION_REQUEST_CODE} or {@link MainActivity#BLUETOOTH_ENABLE_CODE}. Any other value does nothing.*/
	private int displayDialog = 0;
	/**Whether this user's cached Preference rules are different from the one in effect(in multiplayer)*/
	private boolean ruleDiffersFromPreference = false;
	/**A temporary cache of rules changed by this player, but not yet agreed upon by the other player. Format: {horSize, verSizse, winStreak, timeLimit, myIndex}*/
	private int[] preferenceRules;
	/**Set to {@code true} in onStart() and to {@code false} in onStop().*/
	private boolean activityRunning = false;
	/**Used to call requestConfirm() from onResumeFragment in case a request is received outside MainActivity.*/
	private final List<BunStr> bunStrs = new LinkedList<>();
	private final static int SAVE_REQUEST_CODE = 412, LOAD_REQUEST_CODE = 413, LOCATION_REQUEST_CODE = 414, BLUETOOTH_ENABLE_CODE = 415;
	private final static int REQUEST_RECEIVED_OUTSIDE_MAIN = 416;
	private final static String DECISION_TAG = "decision", EDITTEXT_TAG = "file", BLUETOOTH_TAG = "bluetooth", CHECKS_TAG = "checks";
	private final static String DIRECTORY_NAME = "PIRI MNK", FILE_EXTENSION = ".sgf";
	/**The {@code Map} mapping {@link Info}s to IDs of {@code String}s that are displayed when the {@code Activity} receives them from the {@link IoThread}.*/
	private final static Map<Info, Integer> ioMessages;
	private final static MnkAi[] availableAis = {new FillerMnkAi(), new PiriMnkAi(), new EmacsGomokuAi()};
	private final static Board.Symbol[] availableSymbols = {Board.Symbol.XS_AND_OS, Board.Symbol.GO_STONES};
	private final static Board.Line[] availableLines = {Board.Line.LINES_ENCLOSING_SYMBOLS, Board.Line.LINES_UNDER_SYMBOLS, Board.Line.DIAGONAL_ENCLOSING_SYMBOLS};
	private final static int[] restartOptions = {R.string.playFirst, R.string.sendChangedRules};

	static {
		//Map the Infos to String IDs.
		Map<Info, Integer> tempMap = new HashMap<>();
		tempMap.put(Info.REJECTION, R.string.requestRejected);
		tempMap.put(Info.INVALID_MOVE, R.string.moveWasInvalid);
		tempMap.put(Info.READ_FAIL, R.string.failedToReceive);
		tempMap.put(Info.WRITE_FAIL, R.string.failedToSend);
		ioMessages = Collections.unmodifiableMap(tempMap);
	}

	//SECTION: Rules, UI and Android API
	/**{@inheritDoc}*/
	@Override public int[] getRules() {
		return new int[]{game.getHorSize(), game.getVerSize(), game.winStreak, enableTimeLimit ? timeLimit : -1, gravity, exactOnly, myIndex};
	}
	private int[] getPureRules() {
		return new int[]{game.getHorSize(), game.getVerSize(), game.winStreak, enableTimeLimit ? timeLimit : -1, gravity, exactOnly};
	}
	/**{@inheritDoc}*/
	@Override public void setRulesFrom(int[] array) {
		if(array[4] != gravity) {
			gravity = array[4];
			game = array[4] == 1 ? new LegalGravityMnkGame() : new LegalMnkGame();
		}
		game.setSize(array[0], array[1]);
		game.winStreak = array[2];
		setTimeLimit(array[3]);
		exactOnly = array[5];
		myIndex = array[MnkManager.RULE_SIZE - 1];
		ruleDiffersFromPreference = !Arrays.equals(getPureRules(), preferenceRules);
	}
	/**Reads the rules from {@code pref}. Also initializes the game if the size has changed.*/
	private void readRules(final SharedPreferences pref) { //TODO: Move initialization to readData?
		gravity = pref.getBoolean("enableGravity", false) ? 1 : 0;
		if(game != null)
			game = gravity == 1 ? new LegalGravityMnkGame(game) : new LegalMnkGame(game);
		else
			game = gravity == 1 ? new LegalGravityMnkGame() : new LegalMnkGame();
		if(game.setSize(pref.getInt("horSize", 15), pref.getInt("verSize", 15))) initialize(); //Initialize MainActivity fields too if the game was initialized.
		game.winStreak = pref.getInt("winStreak", 5);
		setTimeLimit(pref.getBoolean("enableTimeLimit", false) ? pref.getInt("timeLimit", 60000) : -1);
		exactOnly = pref.getBoolean("exactOnly", false) ? 1 : 0;
	}
	/**Saves the rules from {@code pref} to {@link MainActivity#preferenceRules} and updates {@link MainActivity#myIndex}.*/
	private void cacheChangedRules(final SharedPreferences pref) {
		preferenceRules = new int[]{pref.getInt("horSize", 15), pref.getInt("verSize", 15), pref.getInt("winStreak", 5),
				pref.getBoolean("enableTimeLimit", false) ? pref.getInt("timeLimit", 60000) : -1,
				pref.getBoolean("enableGravity", false) ? 1 : 0, pref.getBoolean("exactOnly", false) ? 1 : 0};
		ruleDiffersFromPreference = !Arrays.equals(getPureRules(), preferenceRules);
	}
	/**Reads the default {@code SharedPreferences} and sets values for {@link MainActivity#game}, {@link MainActivity#board}, {@link MainActivity#highlighter} and more.*/
	private void readData() {
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		//rules
		if(!onBluetooth)
			readRules(pref);
		cacheChangedRules(pref);
		//graphics. TODO: pass functions as symbol type?
		int backColor;
		Board.Symbol symbolType;
		Board.Line lineType;
		backColor = pref.getInt("backgroundColor", 0xFFFFFFFF);
		ai = availableAis[Integer.parseInt(pref.getString("aiType", "1"))];
		symbolType = availableSymbols[Integer.parseInt(pref.getString("symbols", "0"))];
		lineType = availableLines[Integer.parseInt(pref.getString("lineType", "0"))];
		parentView.setBackgroundColor(backColor);
		board.setGame(game);
		board.setSideLength(screenX);
		board.setAiInternals(null);
		board.updateValues(backColor, pref.getInt("lineColor", 0xFF000000), pref.getInt("oColor", 0xFFFF0000), pref.getInt("xColor", 0xFF0000FF), symbolType, lineType);
		highlighter.updateValues(game.getHorSize(), game.getVerSize(), screenX, pref.getInt("highlightColor", 0x7F000000), pref.getInt("highlightDuration", 120), pref.getInt("highlightHowMany", 3));
		enableHighlight = pref.getBoolean("enableHighlight", true);
		showAiInternals = pref.getBoolean("showAiInternals", false);
	}
	/**Calls {@link MainActivity#readData} and invalidates {@link MainActivity#board}.*/
	@SuppressWarnings("SuspiciousNameCombination")
	@Override protected void onStart() {
		super.onStart();
		readData();
		board.setLayoutParams(new FrameLayout.LayoutParams(screenX, screenX));
		board.invalidate();
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(screenX, screenX);
		params.setMargins(0, 0, 0, -screenX);
		highlighter.setLayoutParams(params);
		highlighter.bringToFront();
		activityRunning = true;
	}
	@Override protected void onStop() {
		activityRunning = false;
		super.onStop();
	}
	/**Instantiates some fields, finds {@code View}s, registers listeners to them and saves the resolution of the screen.*/
	@SuppressLint("ClickableViewAccessibility")
	@Override protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		fillThread = new FillThread();
		//Find views and assign listeners.
		board = findViewById(R.id.illustrator);
		board.setOnTouchListener(new BoardListener());
		highlighter = findViewById(R.id.highlighter);
		winText = findViewById(R.id.winText);
		useAI = findViewById(R.id.useAI);
		radioLocal = findViewById(R.id.radioLocal);
		radioBluetooth = findViewById(R.id.radioBluetooth);
		parentView = findViewById(R.id.mainLayout);
		findViewById(R.id.restart).setOnClickListener(bLis);
		buttonAI = findViewById(R.id.buttonAI);
		buttonAI.setOnClickListener(bLis);
		findViewById(R.id.buttonSettings).setOnClickListener(bLis);
		buttonFill = findViewById(R.id.fill);
		buttonFill.setOnClickListener(bLis);
		findViewById(R.id.revert).setOnClickListener(bLis);
		findViewById(R.id.save).setOnClickListener(bLis);
		buttonLoad = findViewById(R.id.load);
		buttonLoad.setOnClickListener(bLis);
		rGroup = findViewById(R.id.radioPlayers);
		rGroup.setOnCheckedChangeListener(rLis);
		//Save the screen resolution.
		android.graphics.Point screenSize = new android.graphics.Point();
		getWindowManager().getDefaultDisplay().getSize(screenSize);
		screenX = screenSize.x;
	}
	/**Listens for clicks on various buttons.*/
	private class ButtonListener implements View.OnClickListener {
		public void onClick(final View v) {
			switch(v.getId()) {
			case R.id.restart:
				if(onBluetooth) {
					showChecksDialog(getString(R.string.restartRequest), restartOptions);
				}
				else initialize();
				break;
			case R.id.buttonAI:
				if(!gameEnd) {
					if(useAI.isChecked()) aiTurn(true);
					else game.changeShape(1);
				}
				break;
			case R.id.buttonSettings:
				fillThread.interrupt();
				startActivity(new Intent(MainActivity.this, SettingActivity.class));
				break;
			case R.id.fill:
				fillThread.interrupt();
				fillThread = new FillThread();
				fillThread.start();
				break;
			case R.id.revert:
				if(onBluetooth) bluetoothThread.write(new byte[]{REQUEST_HEADER, REQUEST_REVERT});
				else revertLast();
				break;
			case R.id.save:
				if(game.getHorSize() > MnkSaveLoader.SGF_MAX || game.getVerSize() > MnkSaveLoader.SGF_MAX) {
					showToast(MainActivity.this, R.string.sgfLimit);
					return;
				}
				if(getPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, SAVE_REQUEST_CODE, R.string.saveRationale))
					saveGame(null);
				break;
			case R.id.load:
				//Reading permission is not enforced under API 19
				if(android.os.Build.VERSION.SDK_INT < 19 || getPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE, LOAD_REQUEST_CODE, R.string.loadRationale))
					loadGame(null);
				break;
			}
		}
	}

	//SECTION: Game handling
	/**Stops every {@code Thread} started by this {@code Activity} and initializes the game.*/
	@Override public void initialize() {
		gameEnd = true; //to prevent AI from filling.
		if(limitTimer != null) limitTimer.cancel();
		fillThread.interrupt();
		game.initialize();
		//Since the game's values could have been changed, update the values of the views that depend on it.
		board.setGame(game);
		board.setAiInternals(null);
		highlighter.updateValues(game.getHorSize(), game.getVerSize(), screenX);
		if(Looper.myLooper() != Looper.getMainLooper()) {
			runOnUiThread(() -> {
				board.invalidate();
				winText.setText("");});
		}
		else {
			board.invalidate();
			winText.setText("");
		}
		gameEnd = false;
		preventPlaying = false;
	}
	/**{@inheritDoc}*/
	@Override public void revertLast() {
		if(game.revertLast()) {
			if(enableTimeLimit) {
				limitTimer.cancel();
				limitTimer.start();
			}
			if(Looper.myLooper() != Looper.getMainLooper()) {
				runOnUiThread(() -> {
					board.invalidate();
					winText.setText("");}); //gameEnd is usually set to false before this line is executed, so checking if it's true is a bad idea here.
			}
			else {
				board.invalidate();
				if(gameEnd) winText.setText("");
			}
			gameEnd = false;
			preventPlaying = false;
		}
	}
	/**Checks if the game has ended(whether it's because of a win or draw) and sets {@link MainActivity#gameEnd} to true if it's ended.
	 * @return A {@code String} that should be displayed to the user. Null if the game hasn't ended.*/
	private String checkEnd(final int x, final int y) {
		Point[] result = game.checkWin(x, y, exactOnly == 1);
		if(result != null) { //If someone won the game
			gameEnd = true;
			if(enableHighlight) highlighter.highlight(result);
			if(game.getNextIndex() == 0) return (game.shapes.length) + getResources().getString(R.string.winAnnouncement); //since MnkGame.shapes starts at 0
			else return game.getNextIndex() + getResources().getString(R.string.winAnnouncement);
		}
		else if(game.history.size() == game.getHorSize() * game.getVerSize()) { //if the board is full
			gameEnd = true;
			return getResources().getString(R.string.draw);
		}
		return null;
	}
	private void aiTurn(boolean highlight) {
		MnkAiDecision decision = ai.playTurnJustify(game);
		if(showAiInternals)
			board.setAiInternals(decision.values);
		if(!endTurn(decision.coord, highlight))
			showToast(this, "The AI's move was invalid.");
	}
	/**Places a stone on the designated position and highlights the position.*/
	@Override public boolean endTurn(final int x, final int y) {return endTurn(x, y, true);}
	/**@see MainActivity#endTurn(int, int, boolean)*/
	private boolean endTurn(final Point p, final boolean highlight) {
		return p != null && endTurn(p.x, p.y, highlight);
	}
	/**Places a stone on the designated position and updates the graphics.
	 * @param highlight Whether to highlight the position.*/
	private boolean endTurn(final int x, int y, final boolean highlight) {
		//Check the legality of the move. Don't check for preventPlaying here; see GameTimer and BoardListener for details.
		if(!game.place(x, y)) return false; //Do nothing if the move was invalid.
		y = game.history.peek().coord.y; //y may change in a GravityMnkGame.
		if(onBluetooth) {
			//TODO: don't rely on the Looper to determine if it's the user or the opponent playing
			if(Looper.myLooper() == Looper.getMainLooper()) bluetoothThread.write(9, MOVE_HEADER, x, y); //The user played it. Send the coordinates to the opponent.
			//If it's the user's turn, refuse to end the turn for the opponent.
			//Compare myIndex to nextIndexAt(-1) since the game.place() call above has changed nextIndex by 1.
			else if(game.getNextIndexAt(-1) == myIndex) {
				game.revertLast(); //Also revert the last move since we already placed the stone above. MainActivity.revertLast() is not needed; we haven't updated the graphics yet.
				return false;
			}
		}
		if(limitTimer != null) limitTimer.cancel();
		if(highlight && enableHighlight) highlighter.highlight(x, y);
		//update graphics and set gameEnd
		String result = checkEnd(x, y);
		if(Looper.myLooper() != Looper.getMainLooper()) {
			Message message = fillHandler.obtainMessage();
			if(result != null) message.getData().putString("result", result);
			fillHandler.sendMessage(message);
		}
		else {
			if(result != null) winText.setText(result);
			board.invalidate();
		}
		if(!gameEnd && enableTimeLimit) {
			runOnUiThread(() -> limitTimer.start());
		}
		return true;
	}
	@Override public void updateRemaining(long time) {
		winText.setText(String.format(Locale.getDefault(), "%02d : %02d : %03d", TimeUnit.MILLISECONDS.toMinutes(time), TimeUnit.MILLISECONDS.toSeconds(time) % 60, time % 1000));
	}
	@Override public void timerFinished() {
		if(!enableTimeLimit) return;
		preventPlaying = false;
		game.changeShape(1);
		if(useAI.isChecked()) //Single player with AI. Let the AI play.
			aiTurn(true);
		else
			limitTimer.start();
	}
	@Override public void togglePlaying(boolean allow) {
		preventPlaying = !allow;
		if(preventPlaying) winText.setText(R.string.waitDelay);
	}
	/**Listens for touches on the {@link MainActivity#board}.*/
	private class BoardListener implements View.OnTouchListener {
		@SuppressLint("ClickableViewAccessibility")
		public boolean onTouch(final View v, final MotionEvent e) {
			/*This user(using this device) shouldn't be allowed to play while in latency offset, in both single and multiplayer; it would be cheating.
			* However, if a user plays with little time remaining, and the move gets to the other, receiving device AFTER the offset started in it,
			* it must accept the move, since the move was valid in the sending device. That's why we only check for preventPlaying in here and not in endTurn().*/
			if(e.getActionMasked() == MotionEvent.ACTION_UP && !gameEnd && (game.getNextIndex() == myIndex || !onBluetooth) && !preventPlaying) {
				int x = (int)(e.getX() / screenX * game.getHorSize()), y = (int)(e.getY() / screenX * game.getVerSize()); //determine which cell the user touched
				//Let the AI play only if the user's move was valid and placed correctly, the game hasn't ended after the move and AI is enabled
				if(endTurn(x, y, false) && !gameEnd && useAI.isChecked())
					aiTurn(true);
				return false;
			}
			return true;
		}
	}

	//SECTION: Dialogs
	private class BunStr {
		private BunStr(Bundle b, String[] s) {bun = b; strs = s;}
		private final Bundle bun;
		private final String[] strs;
	}
	/**Shows the {@code Dialog} {@link MainActivity#displayDialog} points to and initializes it.*/
	@Override public void onResumeFragments() {
		super.onResumeFragments();
		switch(displayDialog) {
		case SAVE_REQUEST_CODE:
			saveGame(null); break;
		case LOAD_REQUEST_CODE:
			loadGame(null); break;
		case BLUETOOTH_ENABLE_CODE:case LOCATION_REQUEST_CODE:
			showBluetoothDialog(); break;
		case REQUEST_RECEIVED_OUTSIDE_MAIN:
			for (int i = 0;i < bunStrs.size();i++) {
				BunStr bs = bunStrs.get(i);
				requestConfirm(bs.bun, bs.strs[0], bs.strs[1], bs.strs[2]);
			}
			bunStrs.clear();
			break;
		}
		displayDialog = 0;
	}
	/**Shows a {@code DecisionDialogFragment} asking the user to confirm something.
	 * Adds {@link MainActivity#DECISION_TAG} to the arguments as tagInBundle.
	 * The positive and negative buttons will read {@code positive} and {@code negative}, respectively.*/
	private void requestConfirm(Bundle arguments, String message, String positive, String negative) {
		if(activityRunning) {
			arguments.putString(getString(R.string.i_tagInBundle), DECISION_TAG);
			DecisionDialogFragment decisionDialog = new DecisionDialogFragment();
			decisionDialog.setArguments(arguments);
			decisionDialog.show(getSupportFragmentManager(), "request", message, positive, negative);
			decisionDialog.setCancelable(false);
			runOnUiThread(() -> {
				getSupportFragmentManager().executePendingTransactions();
				decisionDialog.getDialog().setCanceledOnTouchOutside(false);
			});
		}
		else { //IllegalStateException is thrown when a Dialog is shown in an inactive Activity.
			bunStrs.add(new BunStr(arguments, new String[]{message, positive, negative}));
			displayDialog = REQUEST_RECEIVED_OUTSIDE_MAIN;
		}
	}
	/**@see MainActivity#requestConfirm(Bundle, String, String, String).
	 * The Dialog class's default text will be used for the buttons.*/
	private void requestConfirm(Bundle arguments, String message) {
		requestConfirm(arguments, message, null, null);
	}
	/**Shows an {@code EditTextDialogFragment} with the supplied tag, message and hint.*/
	private void showEditTextDialog(final String message, final String hint) {
		EditTextDialogFragment fragment = new EditTextDialogFragment();
		fragment.setArguments(bundleWith(getString(R.string.i_tagInBundle), EDITTEXT_TAG));
		fragment.show(getSupportFragmentManager(), EDITTEXT_TAG, message, hint);
	}
	/**Opens a {@link BluetoothDialogFragment}.*/
	private void showBluetoothDialog() {
		BluetoothDialogFragment fragment = new BluetoothDialogFragment();
		fragment.setArguments(bundleWith(getString(R.string.i_tagInBundle), BLUETOOTH_TAG));
		fragment.show(getSupportFragmentManager(), BLUETOOTH_TAG, getString(R.string.app_name));
	}
	private void showChecksDialog(String message, int[] questions) {
		ChecksDialogFragment checks = new ChecksDialogFragment();
		checks.setArguments(bundleWith(getString(R.string.i_tagInBundle), CHECKS_TAG));
		checks.show(getSupportFragmentManager(), CHECKS_TAG, message, questions);
	}
	//Responses to requests, myIndex change(when requesting restart), disconnection confirmation, save/load and receiving Bluetooth sockets
	/**Call to return the result of a {@code Dialog} to this {@code Activity}.*/
	@Override public <T> void giveResult(final T result, final Bundle arguments) {
		if(arguments == null) {
			showToast(this, "Error: giveResults arguments were null.");
			return;
		}
		final String tag = arguments.getString(getString(R.string.i_tagInBundle));
		if(tag == null) {
			showToast(this, "Error: giveResult arguments didn't contain a tag.");
			return;
		}
		switch (tag) {
		case DECISION_TAG:
			boolean wasRequest = arguments.getBoolean(getString(R.string.i_wasRequest));
			if(wasRequest) {
				if(!onBluetooth) break; //The opponent might cancel connection after sending a request.
				byte request = arguments.getByte(getString(R.string.i_action));
				if((Boolean) result) {
					switch (request) {
					case REQUEST_RESTART:
						if(arguments.getIntArray(getString(R.string.i_rulesRequestToResultKey)) != null)
							setRulesFrom(arguments.getIntArray(getString(R.string.i_rulesRequestToResultKey)));
						initialize();
						break;
					case REQUEST_REVERT:
						revertLast();
						break;
					}
					if(arguments.getIntArray(getString(R.string.i_rulesRequestToResultKey)) != null)
						bluetoothThread.write(4 + RULE_SIZE * 4, RESPONSE_HEADER, RESPONSE_PERMIT, request, RULE_CHANGED, getRules());
					else
						bluetoothThread.write(new byte[]{RESPONSE_HEADER, RESPONSE_PERMIT, request});
				}
				else bluetoothThread.write(new byte[]{RESPONSE_HEADER, RESPONSE_REJECT, request});
			}
			else {
				final String decisionKey = arguments.getString(getString(R.string.i_nonreqAction));
				if(decisionKey == null) break;
				if(decisionKey.equals(getString(R.string.i_localConfirm))) {
					if((Boolean) result) {
						stopBluetooth(true);
						configureUI(false);
					}
					else
						hiddenClick(radioBluetooth);
				}
			}
			break;
		case EDITTEXT_TAG:
			final String message = arguments.getString(getString(R.string.piri_dialogs_messageArgument));
			if(result != null && message != null) {
				if(message.equals(getString(R.string.save))) saveGame((String) result);
				else if(message.equals(getString(R.string.load))) loadGame((String) result);
			}
			break;
		case BLUETOOTH_TAG:
			if(result == null)
				runOnUiThread(() -> hiddenClick(radioLocal)); //connection failed or canceled
			else {
				this.socket = (BluetoothSocket) result;
				runOnUiThread(() -> configureUI(true));
				try {
					bluetoothThread = new IoThread(this, socket.getInputStream(), socket.getOutputStream());
					bluetoothThread.start();
				}
				catch (IOException e) {
					showToast(this, R.string.couldntGetStream);
					radioLocal.setChecked(true);
				}
				initialize();
				if(arguments.getBoolean(getString(R.string.i_isServer))) {
					myIndex = 0;
					bluetoothThread.write(2 + RULE_SIZE * 4, ORDER_HEADER, ORDER_INITIALIZE, getRules());
				}
			}
			break;
		case CHECKS_TAG:
			final boolean[] boolArrayResult = (boolean[]) result;
			if(result == null) break;
			final int newMyIndex = boolArrayResult[0] ? 0 : 1;
			if((ruleDiffersFromPreference && boolArrayResult[1]) || myIndex != newMyIndex) { //Request to restart AND change the rules.
				bluetoothThread.write(3 + RULE_SIZE * 4, REQUEST_HEADER, REQUEST_RESTART, RULE_CHANGED,
						boolArrayResult[1] ? preferenceRules : getPureRules(), newMyIndex);
			}
			else
				bluetoothThread.write(new byte[]{REQUEST_HEADER, REQUEST_RESTART});
			break;
		}
	}

	//SECTION: Communication
	/**Clicks the {@code RadioButton} without alerting {@code rLis}.*/
	private void hiddenClick(RadioButton button) {
		AndroidUtilsKt.hiddenClick(rGroup, button, rLis, true);
	}
	/**Listens for changes in the playing mode(local or Bluetooth)*/
	private class RadioListener implements RadioGroup.OnCheckedChangeListener {
		@Override public void onCheckedChanged(final RadioGroup group, final int id) {
			switch(id) {
			case R.id.radioLocal:
				requestConfirm(bundleWith(getString(R.string.i_nonreqAction), getString(R.string.i_localConfirm)), getString(R.string.termConnection));
				break;
			case R.id.radioBluetooth:
				if(adapter == null) {
					hiddenClick(radioLocal);
					showToast(MainActivity.this, R.string.noBluetoothSupport);
				}
				//If location permission isn't granted
				else if(!getPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION, LOCATION_REQUEST_CODE, R.string.locationRationale)) {
					break; //onRequestPermissionsResult() will check radioLocal if the request is denied.
				}
				else if(!adapter.isEnabled()) {
					startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BLUETOOTH_ENABLE_CODE);
				}
				else showBluetoothDialog();
				break;
			}
		}
	}
	/**Calls {@link MainActivity#showBluetoothDialog} or checks radioLocal and tells the user he must enable Bluetooth to play wirelessly depending on the {@code resultCode}.*/
	@Override public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if(requestCode == BLUETOOTH_ENABLE_CODE) {
			if(resultCode == RESULT_OK)
				displayDialog = BLUETOOTH_ENABLE_CODE; //IllegalStateException is thrown if we call DialogFragment.show() directly. onResumeFragments() will call it indirectly by calling showBluetoothDialog()
			else {
				hiddenClick(radioLocal);
				showToast(this, R.string.enableBluetooth);
			}
		}
		else super.onActivityResult(requestCode, resultCode, data); //to have BluetoothDialogFragment.onActivityResult() called
	}
	/**Configures the UI for Bluetooth or local play.*/
	private void configureUI(final boolean toBluetooth) {
		onBluetooth = toBluetooth; //TODO: move this to somewhere else
		useAI.setChecked(!toBluetooth);
		useAI.setEnabled(!toBluetooth);
		buttonFill.setEnabled(!toBluetooth);
		buttonAI.setEnabled(!toBluetooth);
		buttonLoad.setEnabled(!toBluetooth);
	}
	/**Sets up the time limit if {@code limit} is greater than 0. Disables it otherwise.
	 * Cancels the timer if the limit was changed from the last value.
	 * @param limit If greater than 0, time limit is enabled and set to it. Otherwise, time limit is disabled.*/
	@Override public void setTimeLimit(int limit) {
		enableTimeLimit = limit > 0;
		timeLimit = limit;
		if(timeLimit != previousTimeLimit) {
			previousTimeLimit = timeLimit;
			limitTimer.cancel();
			if(enableTimeLimit)
				runOnUiThread(() -> limitTimer = new GameTimer(MainActivity.this, timeLimit));
		}
	}
	@Override public void onDestroy() {
		stopBluetooth(true);
		if(limitTimer != null) limitTimer.cancel();
		super.onDestroy();
	}
	/**{@inheritDoc} Informs the user of the cancellation.*/
	@Override public void cancelConnection() {
		stopBluetooth(false);
		runOnUiThread(() -> {
			configureUI(false);
			hiddenClick(radioLocal);
			showToast(MainActivity.this, R.string.connectionTerminated);});
	}
	/**Stops Bluetooth communications but doesn't set radioLocal to true.
	 * @param informOpponent If true, informs the opponent that we terminated the connection.*/
	private void stopBluetooth(boolean informOpponent) {
		try {
			if(bluetoothThread != null) {
				if(informOpponent) bluetoothThread.write(new byte[]{ORDER_HEADER, ORDER_CANCEL_CONNECTION});
				bluetoothThread.interrupt();
				bluetoothThread = null;
			}
			if(socket != null) socket.close();
		}
		catch(IOException e) {
			showToast(this, R.string.problemWhileClosing);
		}
	}
	@Override public void requestToUser(byte action) {
		requestToUser(action, null);
	}
	/**Informs that the opponent requested the {@code action} and lets the user choose whether to allow it or not.
	 * If the requested {@code action} is {@link IoThread#REQUEST_RESTART}, displays the changed rules.
	 * @param action The action the opponent requested.
	 * @param details Currently used to show changed rules(int[]) to the user.*/
	@Override public <T> void requestToUser(byte action, T details) {
		int actionStringID;
		switch(action) {
		case REQUEST_RESTART: actionStringID = R.string.restart; break;
		case REQUEST_REVERT: actionStringID = R.string.revert; break;
		default: actionStringID = R.string.ioError; break;
		}
		Bundle bundle = new Bundle();
		bundle.putBoolean(getString(R.string.i_wasRequest), true);
		bundle.putByte(getString(R.string.i_action), action);
		String shownString = String.format(Locale.getDefault(), getString(R.string.requested), getString(actionStringID));
		if(action == REQUEST_RESTART && details != null) {
			bundle.putIntArray(getString(R.string.i_rulesRequestToResultKey), (int[])details);
			shownString += '\n' + stringifyRules((int[])details);
		}
		requestConfirm(bundle, shownString);
	}
	private String stringifyRules(int[] rules) {
		StringBuilder builder = new StringBuilder();
		final String[] names = {getString(R.string.horSize), getString(R.string.verSize), getString(R.string.winCondition),
				getString(R.string.timeLimit), getString(R.string.gravity), getString(R.string.exactOnly), getString(R.string.myIndex)};
		for(int i = 0;i < MnkManager.RULE_SIZE;i++) {
			builder.append(names[i]);
			builder.append(": ");
			if(i == MnkManager.RULE_SIZE - 1) { //For myIndex
				builder.append(rules[i] + 1);
				builder.append(getString(R.string.ordinalMarker));
			}
			else if(i >= 4) { //For gravity and exactOnly
				builder.append(rules[i] == 1 ? getString(R.string.enabled) : getString(R.string.disabled));
			}
			else
				builder.append(rules[i] != -1 ? rules[i] : getString(R.string.none_ruleSync)); //For timeLimit == -1
			builder.append('\n');
		}
		return builder.toString();
	}

	//SECTION: File and communication
	@Override public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
		if(grantResults.length > 0) {
			if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				if(requestCode == LOCATION_REQUEST_CODE && !adapter.isEnabled()) {
					Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableBtIntent, BLUETOOTH_ENABLE_CODE);
					return;
				}
				displayDialog = requestCode; //IllegalStateException is thrown if we call DialogFragment.show() directly. onResumeFragments() will indirectly call it by calling saveGame() or loadGame()
			}
			else if(requestCode == LOCATION_REQUEST_CODE)
				hiddenClick(radioLocal);
		}
	}
	@Override @SuppressWarnings("ConstantConditions") public void informUser(final Info of) {
		showToast(this, ioMessages.get(of));
	}

	//SECTION: Files
	/**Saves the game.
	 * @param fileName If null, shows a {@code Dialog} prompting the user to input the file name. If not, saves the game.*/
	private void saveGame(final String fileName) {
		if(fileName == null) showEditTextDialog(getString(R.string.save), getString(R.string.fileNameHint));
		else {
			try {
				saveLoader.save(game, getFile(DIRECTORY_NAME, fileName + FILE_EXTENSION, true));
			}
			catch (IOException e) {
				showToast(this, R.string.couldntCreateFile);
			}
		}
	}
	/**Loads the game.
	 * @param fileName If null, shows a {@code Dialog} prompting the user to input the file name. If not, loads the game.*/
	private void loadGame(final String fileName) {
		if(fileName == null) showEditTextDialog(getString(R.string.load), getString(R.string.fileNameHint));
		else {
			try {
				MnkGame loaded = saveLoader.load(getFile(DIRECTORY_NAME, fileName + FILE_EXTENSION, false), game.winStreak);
				initialize(); //Initialize after loading the game so that if loading fails, the previous game doesn't get initialized
				game = new LegalMnkGame(loaded);
				board.setGame(game);
				board.invalidate();
				highlighter.updateValues(game.getHorSize(), game.getVerSize(), screenX);
			}
			catch (IOException e) {
				showToast(this, R.string.couldntFindFile);
			}
		}
	}

	//SECTION: Fun
	private static class FillHandler extends Handler{
		final WeakReference<MainActivity> activity;
		FillHandler(MainActivity a) {activity = new WeakReference<>(a);}
		@Override public void handleMessage(final Message m) {
			if(m.getData().getString("result") != null) activity.get().winText.setText(m.getData().getString("result"));
			activity.get().board.invalidate();
			if(!activity.get().gameEnd) synchronized(activity.get().fillThread) {activity.get().fillThread.notify();}
		}
	}
	private class FillThread extends Thread {
		@Override synchronized public void run() {
			while(!gameEnd) {
				aiTurn(false);
				//If it doesn't wait until the UI thread finishes and keep calling aiTurn, most of board.invalidate() calls will be ignored and the result will be shown at once when the game's end, breaking animation.
				try { wait(); }
				catch(InterruptedException e) { break; }
			}
		}
	}
}