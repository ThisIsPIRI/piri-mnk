package com.thisispiri.mnk;

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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.Point;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.thisispiri.dialogs.DecisionDialogFragment;
import com.thisispiri.dialogs.DialogListener;
import com.thisispiri.dialogs.EditTextDialogFragment;
import com.thisispiri.util.AndroidUtilsKt;
import com.thisispiri.util.GameTimer;
import com.thisispiri.util.TimedGameManager;

import static com.thisispiri.mnk.IoThread.*;

/**The main {@code Activity} for PIRI MNK. Handles all interactions between the UI, communications and game logic.*/
public class MainActivity extends AppCompatActivity implements MnkManager, TimedGameManager, DialogListener {
	private Board board;
	private Highlighter highlighter;
	private LegalMnkGame game;
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
	private Handler fillHandler = new FillHandler(this);
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
	/**TODO: WIP. Whether the rules has changed since the last rule sync(in multiplayer)*/
	private final boolean ruleChanged = true;
	/**A temporary cache of rules changed by this player, but not yet agreed upon by the other player. Format: {horSize, verSizse, winStreak, timeLimit}*/
	private int[] changedRules;
	private final static int SAVE_REQUEST_CODE = 412, LOAD_REQUEST_CODE = 413, LOCATION_REQUEST_CODE = 414, BLUETOOTH_ENABLE_CODE = 415;
	private final static String DECISION_TAG = "decision", FILE_TAG = "file", BLUETOOTH_TAG = "bluetooth";
	private final static String DIRECTORY_NAME = "PIRI MNK", FILE_EXTENSION = ".sgf";
	/**The {@code Map} mapping {@link Info}s to IDs of {@code String}s that are displayed when the {@code Activity} receives them from the {@link IoThread}.*/
	private final static Map<Info, Integer> ioMessages = new HashMap<>();

	static {
		//Map the Infos to String IDs.
		ioMessages.put(Info.REJECTION, R.string.requestRejected);
		ioMessages.put(Info.INVALID_MOVE, R.string.moveWasInvalid);
		ioMessages.put(Info.READ_FAIL, R.string.failedToReceive);
		ioMessages.put(Info.WRITE_FAIL, R.string.failedToSend);
	}
	//SECTION: Rules, UI and Android API
	/**{@inheritDoc}*/
	@Override public int[] getRules() {
		return new int[]{game.getHorSize(), game.getVerSize(), game.winStreak, enableTimeLimit ? timeLimit : -1};
	}
	/**{@inheritDoc}*/
	@Override public void setRulesFrom(int[] array) {
		game.setSize(array[0], array[1]);
		game.winStreak = array[2];
		setTimeLimit(array[3]);
	}
	/**Read the rules from {@code pref}. Also initializes the game if the size has changed.*/ //TODO: Move initialization to readData?
	private void readRules(final SharedPreferences pref) {
		if(game.setSize(pref.getInt("horSize", 15), pref.getInt("verSize", 15))) initialize(); //initialize MainActivity fields too if the game was initialized.
		game.winStreak = pref.getInt("winStreak", 5);
		setTimeLimit(pref.getBoolean("enableTimeLimit", false) ? pref.getInt("timeLimit", 60000) : -1);
	}
	/**Saves the rules from {@code pref} to {@link MainActivity#changedRules}.*/
	private void cacheRules(final SharedPreferences pref) {
		changedRules = new int[]{pref.getInt("horSize", 15), pref.getInt("verSize", 15), pref.getInt("winStreak", 5),
				pref.getBoolean("enableTimeLimit", false) ? pref.getInt("timeLimit", 60000) : -1};
	}
	/**Reads the default {@code SharedPreferences} and sets values for {@link MainActivity#game}, {@link MainActivity#board}, {@link MainActivity#highlighter} and more.*/
	private void readData() {
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		//rules
		cacheRules(pref);
		if(!onBluetooth)
			readRules(pref);
		//graphics. TODO: pass functions as symbol type?
		int backColor;
		Board.Symbol symbolType;
		Board.Line lineType;
		backColor = pref.getInt("backgroundColor", 0xFFFFFFFF);
		switch(pref.getString("symbols", "xsAndOs")) {
		case "xsAndOs": symbolType = Board.Symbol.XS_AND_OS; break;
		case "goStones": symbolType = Board.Symbol.GO_STONES; break;
		default: symbolType = Board.Symbol.XS_AND_OS;
		}
		switch(pref.getString("lineType", "linesEnclosingSymbols")) {
		case "linesEnclosingSymbols": lineType = Board.Line.LINES_ENCLOSING_SYMBOLS; break;
		case "linesUnderSymbols": lineType = Board.Line.LINES_UNDER_SYMBOLS; break;
		case "diagonalsEnclosingSymbols" : lineType = Board.Line.DIAGONAL_ENCLOSING_SYMBOLS; break;
		default: lineType = Board.Line.LINES_ENCLOSING_SYMBOLS;
		}
		parentView.setBackgroundColor(backColor);
		board.setGame(game);
		board.setSideLength(screenX);
		board.updateValues(backColor, pref.getInt("lineColor", 0xFF000000), pref.getInt("oColor", 0xFFFF0000), pref.getInt("xColor", 0xFF0000FF), symbolType, lineType);
		highlighter.updateValues(game.getHorSize(), game.getVerSize(), screenX, pref.getInt("highlightColor", 0x7F000000), pref.getInt("highlightDuration", 120), pref.getInt("highlightHowMany", 3));
		enableHighlight = pref.getBoolean("enableHighlight", true);
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
	}
	/**Instantiates some fields, finds {@code View}s, registers listeners to them and saves the resolution of the screen.*/
	@SuppressLint("ClickableViewAccessibility")
	@Override protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		game = new LegalMnkGame();
		ai = new PiriMnkAi();
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
		Point screenSize = new Point();
		getWindowManager().getDefaultDisplay().getSize(screenSize);
		screenX = screenSize.x;
	}
	/**Listens for clicks on various buttons.*/
	private class ButtonListener implements View.OnClickListener {
		public void onClick(final View v) {
			switch(v.getId()) {
			case R.id.restart:
				if(onBluetooth) {
					if(ruleChanged) //Request to restart AND change the rules.
						bluetoothThread.write(19, REQUEST_HEADER, REQUEST_RESTART, (byte)1, changedRules);
					else
						bluetoothThread.write(new byte[]{REQUEST_HEADER, REQUEST_RESTART});
				}
				else initialize();
				break;
			case R.id.buttonAI:
				if(!gameEnd) {
					if(useAI.isChecked()) endTurn(ai.playTurn(game), true);
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
					AndroidUtilsKt.showToast(MainActivity.this, R.string.sgfLimit);
					return;
				}
				if(getPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, SAVE_REQUEST_CODE, R.string.saveRationale))
					saveGame(null);
				break;
			case R.id.load:
				if(android.os.Build.VERSION.SDK_INT < 19 || getPermission(Manifest.permission.READ_EXTERNAL_STORAGE, LOAD_REQUEST_CODE, R.string.loadRationale)) //Reading permission is not enforced under API 19
					loadGame(null);
				break;
			}
		}
	}
	/**Sees if the {@code permission} is granted to the {@code Context} and, if it isn't, requests that it be.
	 * @return if the permission was already granted at the time of call.*/
	private boolean getPermission(String permission, int requestCode, int rationaleId) {
		if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) { //if writing permission hasn't been granted
			if(ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
				AndroidUtilsKt.showToast(this, rationaleId);
			}
			ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
			return false;
		}
		else return true;
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
			connectBluetooth(); break;
		}
		displayDialog = 0;
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
		highlighter.updateValues(game.getHorSize(), game.getVerSize(), screenX);
		if(Looper.myLooper() != Looper.getMainLooper()) {
			runOnUiThread(() -> {
				board.invalidate();
				winText.setText("");
			});
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
			if (enableTimeLimit) {
				limitTimer.cancel();
				limitTimer.start();
			}
			if (Looper.myLooper() != Looper.getMainLooper()) {
				runOnUiThread(() -> {
					board.invalidate();
					winText.setText("");}); //gameEnd is usually set to false before this line is executed, so checking if it's true is a bad idea here.
			}
			else {
				board.invalidate();
				if (gameEnd) winText.setText("");
			}
			gameEnd = false;
			preventPlaying = false;
		}
	}
	/**Checks if the game has ended(whether it's because of a win or draw) and set {@link MainActivity#gameEnd} to true if it's ended.
	 * @return a {@code String} that should be displayed to the user. Null if the game hasn't ended.*/
	private String checkEnd(final int x, final int y) {
		Point[] result = game.checkWin(x, y);
		if (result != null) { //if someone won the game
			gameEnd = true;
			if(enableHighlight) highlighter.highlight(result);
			if(game.getNextIndex() == 0) return (game.shapes.length) + getResources().getString(R.string.winAnnouncement); //since MnkGame.shapes starts at 0
			else return game.getNextIndex() + getResources().getString(R.string.winAnnouncement);
		}
		else if (game.history.size() == game.getHorSize() * game.getVerSize()) { //if the board is full
			gameEnd = true;
			return getResources().getString(R.string.draw);
		}
		return null;
	}
	/**Places a stone on the designated position and highlights the position.*/
	@Override public boolean endTurn(final int x, final int y) {return endTurn(x, y, true);}
	/**@see MainActivity#endTurn(int, int, boolean)*/
	private boolean endTurn(final Point p, final boolean highlight) {return endTurn(p.x, p.y, highlight);}
	/**Places a stone on the designated position and updates the graphics.
	 * @param highlight Whether to highlight the position.*/
	private boolean endTurn(final int x, final int y, final boolean highlight) {
		//Check the legality of the move. Don't check for preventPlaying here; see GameTimer and BoardListener for details.
		if(!game.place(x, y)) return false; //Do nothing if the move was invalid.
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
			if (result != null) message.getData().putString("result", result);
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
		if (!enableTimeLimit) return;
		preventPlaying = false;
		game.changeShape(1);
		if (useAI.isChecked()) //Single player with AI. Let the AI play.
			endTurn(ai.playTurn(game), true);
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
					endTurn(ai.playTurn(game), true);
				return false;
			}
			return true;
		}
	}
	/**Call to return the result of a {@code Dialog} to this {@code Activity}.*/
	@Override public <T> void giveResult(T result, final Bundle arguments) {
		if(arguments != null) {
			String tag = arguments.getString(getString(R.string.i_tagInBundle));
			if(tag != null) {
				switch (tag) {
				case DECISION_TAG:
					boolean wasRequest = arguments.getBoolean(getString(R.string.i_wasRequest)); //TODO: replace with string resources
					if(wasRequest) {
						if(!onBluetooth) break; //The opponent might cancel connection after sending a request.
						byte request = arguments.getByte(getString(R.string.i_action));
						if ((Boolean) result) {
							switch (request) {
							case REQUEST_RESTART:
								if (arguments.getIntArray(getString(R.string.i_rulesRequestToResultKey)) != null)
									setRulesFrom(arguments.getIntArray(getString(R.string.i_rulesRequestToResultKey)));
								initialize();
								break;
							case REQUEST_REVERT:
								revertLast();
								break;
							}
							if(arguments.getIntArray(getString(R.string.i_rulesRequestToResultKey)) != null)
								bluetoothThread.write(20, RESPONSE_HEADER, RESPONSE_PERMIT, request, (byte)1, getRules());
							else
								bluetoothThread.write(new byte[]{RESPONSE_HEADER, RESPONSE_PERMIT, request});
						}
						else bluetoothThread.write(new byte[]{RESPONSE_HEADER, RESPONSE_REJECT, request});
					}
					else { //TODO: generalize to process different kinds of non-requests
						if((Boolean) result) {
							stopBluetooth(true);
							configureUI(false);
						}
						else
							hiddenClick(radioBluetooth);
					}
					break;
				case FILE_TAG:
					String message = arguments.getString(getString(R.string.piri_dialogs_messageArgument));
					if (result != null && message != null) {
						if (message.equals(getString(R.string.save))) saveGame((String) result);
						else if (message.equals(getString(R.string.load))) loadGame((String) result);
					}
					break;
				case BLUETOOTH_TAG:
					if (result == null)
						hiddenClick(radioLocal); //connection failed or canceled
					else {
						this.socket = (BluetoothSocket) result;
						runOnUiThread(() -> configureUI(true));
						try {
							bluetoothThread = new IoThread(this, socket.getInputStream(), socket.getOutputStream());
							bluetoothThread.start();
						}
						catch (IOException e) {
							AndroidUtilsKt.showToast(this, R.string.couldntGetStream);
							radioLocal.setChecked(true);
						}
						initialize();
						if (arguments.getBoolean(getString(R.string.i_isServer))) {
							bluetoothThread.write(18, ORDER_HEADER, ORDER_INITIALIZE, game.getHorSize(), game.getVerSize(), game.winStreak, enableTimeLimit ? timeLimit : -1);
							myIndex = 0;
						}
						else {
							myIndex = 1;
						}
					}
					break;
				}
			} //TODO: add some way to inform the user that a Dialog didn't "return" correctly
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
			case R.id.radioLocal: //TODO: request confirmation of the user when he clicks the local button in Bluetooth mode
				requestConfirm(new Bundle(), getString(R.string.termConnection));
				break;
			case R.id.radioBluetooth:
				if (adapter == null) {
					hiddenClick(radioLocal);
					AndroidUtilsKt.showToast(MainActivity.this, R.string.noBluetoothSupport);
				}
				else if(!getPermission(Manifest.permission.ACCESS_COARSE_LOCATION, LOCATION_REQUEST_CODE, R.string.locationRationale)) { //if location permission hasn't been granted
					break;
				}
				else if (!adapter.isEnabled()) {
					startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BLUETOOTH_ENABLE_CODE);
				}
				else connectBluetooth();
				break;
			}
		}
	}
	/**Calls {@link MainActivity#connectBluetooth} or checks radioLocal and tells the user he must enable Bluetooth to play wirelessly depending on the {@code resultCode}.*/
	@Override public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if(requestCode == BLUETOOTH_ENABLE_CODE) {
			if(resultCode == RESULT_OK)
				displayDialog = BLUETOOTH_ENABLE_CODE; //IllegalStateException is thrown if we call DialogFragment.show() directly. onResumeFragments() will call it indirectly by calling connectBluetooth()
			else {
				hiddenClick(radioLocal);
				AndroidUtilsKt.showToast(this, R.string.enableBluetooth);
			}
		}
		else super.onActivityResult(requestCode, resultCode, data); //to have BluetoothDialogFragment.onActivityResult() called
	}
	/**Opens a {@link BluetoothDialogFragment}.*/
	private void connectBluetooth() {
		BluetoothDialogFragment fragment = new BluetoothDialogFragment();
		Bundle arguments = new Bundle();
		arguments.putString(getString(R.string.i_tagInBundle), BLUETOOTH_TAG);
		fragment.setArguments(arguments);
		fragment.show(getFragmentManager(), "bluetooth");
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
			AndroidUtilsKt.showToast(MainActivity.this, R.string.connectionTerminated);
		});
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
			runOnUiThread(() -> AndroidUtilsKt.showToast(this, R.string.problemWhileClosing));
		}
	}
	/**Shows a {@code DecisionDialogFragment} asking the user to confirm something.
	 * Adds {@link MainActivity#DECISION_TAG} to the arguments as tagInBundle.*/
	private void requestConfirm(Bundle arguments, String message) {
		arguments.putString(getString(R.string.i_tagInBundle), DECISION_TAG);
		DecisionDialogFragment decisionDialog = new DecisionDialogFragment();
		decisionDialog.setArguments(arguments);
		decisionDialog.show(getFragmentManager(), "request", message);
		decisionDialog.setCancelable(false);
		runOnUiThread(() -> {
				getFragmentManager().executePendingTransactions();
				decisionDialog.getDialog().setCanceledOnTouchOutside(false);});

	}
	@Override public void requestToUser(byte action) {
		requestToUser(action, null);
	}
	/**Informs that the opponent requested the {@code action} and lets the user choose whether to allow it or not.
	 * If the requested {@code action} is {@link IoThread#REQUEST_RESTART}, displays the changed rules.
	 * @param action The action the opponent requested.*/
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
		//TODO: Localize
		StringBuilder builder = new StringBuilder();
		final String[] names = {getString(R.string.horSize), getString(R.string.verSize), getString(R.string.winCondition), getString(R.string.timeLimit)};
		for(int i = 0;i < 4;i++) {
			builder.append(names[i]);
			builder.append(": ");
			builder.append(rules[i] != -1 ? rules[i] : getString(R.string.none)); //For timeLimit == -1
			builder.append('\n');
		}
		return builder.toString();
	}

	//SECTION: File and communication
	@Override public void onRequestPermissionsResult(final int requestCode, @NonNull final String permissions[], @NonNull final int[] grantResults) {
		if(grantResults.length > 0) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
	@Override public void informUser(final Info of) {
		AndroidUtilsKt.showToast(this, ioMessages.get(of));
	}

	//SECTION: Files
	/**Shows an {@code EditTextDialogFragment} with the supplied tag, message and hint.*/
	private void showEditTextDialog(final String tag, final String message, final String hint) {
		EditTextDialogFragment fragment = new EditTextDialogFragment();
		Bundle arguments = new Bundle();
		arguments.putString(getString(R.string.i_tagInBundle), tag);
		fragment.setArguments(arguments);
		fragment.show(getFragmentManager(), tag, message, hint);
	}
	/**Saves the game.
	 * @param fileName If null, shows a {@code Dialog} prompting the user to input the file name. If not, saves the game.*/
	private void saveGame(final String fileName) {
		if(fileName == null) showEditTextDialog(FILE_TAG, getString(R.string.save), getString(R.string.fileNameHint));
		else {
			try {
				saveLoader.save(game, DIRECTORY_NAME, fileName + FILE_EXTENSION);
			}
			catch (IOException e) {
				AndroidUtilsKt.showToast(this, R.string.couldntCreateFile);
			}
		}
	}
	/**Loads the game.
	 * @param fileName If null, shows a {@code Dialog} prompting the user to input the file name. If not, loads the game.*/
	private void loadGame(final String fileName) {
		if(fileName == null) showEditTextDialog(FILE_TAG, getString(R.string.load), getString(R.string.fileNameHint));
		else {
			try {
				MnkGame loaded = saveLoader.load(DIRECTORY_NAME, fileName + FILE_EXTENSION, game.winStreak);
				initialize(); //Initialize after loading the game so that if loading fails, the previous game doesn't get initialized
				game = new LegalMnkGame(loaded);
				board.setGame(game);
				board.invalidate();
				highlighter.updateValues(game.getHorSize(), game.getVerSize(), screenX);
			}
			catch (IOException e) {
				AndroidUtilsKt.showToast(this, R.string.couldntFindFile);
			}
		}
	}

	//SECTION: Miscellaneous
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
			while (!gameEnd) {
				endTurn(ai.playTurn(game), false);
				//if it doesn't wait until the UI thread finishes and keep calling endTurn, most of board.invalidate() calls will be ignored and the result will be shown at once when the game's end, breaking animation.
				try { wait(); }
				catch(InterruptedException e) { break; }
			}
		}
	}
}