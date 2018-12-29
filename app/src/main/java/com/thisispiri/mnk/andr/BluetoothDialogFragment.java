package com.thisispiri.mnk.andr;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import android.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.thisispiri.dialogs.ListenerDialogFragment;
import com.thisispiri.mnk.R;
import com.thisispiri.util.AndroidUtilsKt;

import java.io.IOException;
import java.util.UUID;

/**A {@code DialogFragment} for finding and connecting to another Android device via Bluetooth.
 * The calling {@code Context} must implement {@code DialogListener} to receive the results and call {@code setArguments()} on this. If either isn't fulfilled, it will throw an {@code Exception}.*/
public class BluetoothDialogFragment extends ListenerDialogFragment {
	private final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
	private BluetoothSocket socket = null;
	private RadioButton radioClient;
	private EditText uuidEditText;
	private ProgressBar progressBar;
	private UUID targetUUID;
	private BluetoothReceiver receiver;
	private Thread runningThread;
	private boolean discoverable = false;
	private String serviceName, isServerString;
	private final static int DISCOVERABLE_REQUEST_CODE = 415;
	private final static int DISCOVERABLE_TIME = 60;

	//SECTION: Initialization
	/**@param serviceName The service name to be used for the SDP record.*/
	public void show(FragmentManager manager, String tag, String serviceName) {
		this.serviceName = serviceName;
		show(manager, tag);
	}
	@Override @NonNull public Dialog onCreateDialog(final Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		@SuppressLint("InflateParams") View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_bluetooth, null);
		builder.setView(view);
		builder.setMessage(R.string.multiplayerOnBluetooth);
		builder.setNegativeButton(R.string.cancel, (dialog, id) -> giveSocket(null, false)); //return to local if the user cancels connection
		radioClient = view.findViewById(R.id.dialogRadioClient);
		uuidEditText = view.findViewById(R.id.uuidEditText);
		progressBar = view.findViewById(R.id.bluetoothProgress);
		view.findViewById(R.id.connectButton).setOnClickListener(new ConnectButtonListener());
		((RadioGroup)view.findViewById(R.id.dialogRadioMethod)).setOnCheckedChangeListener(new RadioListener());
		receiver = new BluetoothReceiver();
		getActivity().registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		getActivity().registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
		Dialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);
		dialog.setOnKeyListener(new BackListener());
		return dialog;
	}
	@Override public void onStart() {
		super.onStart();
		isServerString = getString(R.string.i_isServer);
	}

	//SECTION: Establishing connection
	private class RadioListener implements RadioGroup.OnCheckedChangeListener {
		@Override public void onCheckedChanged(RadioGroup group, int id) {
			if(runningThread != null) runningThread.interrupt();
			switch(group.getCheckedRadioButtonId()) {
			case R.id.dialogRadioServer:
				uuidEditText.setHint(R.string.serverIdentifierHint);
				adapter.cancelDiscovery();
				break;
			case R.id.dialogRadioClient:
				uuidEditText.setHint(R.string.clientIdentifierHint);
				break;
			}
			progressBar.setVisibility(View.GONE);
		}
	}
	private class ConnectButtonListener implements View.OnClickListener {
		@Override public void onClick(final View v) {
			if(radioClient.isChecked()) {
				targetUUID = UUID.nameUUIDFromBytes(uuidEditText.getText().toString().getBytes());
				adapter.cancelDiscovery();
				adapter.startDiscovery();
				progressBar.setVisibility(View.VISIBLE);
			}
			else {
				if(!discoverable) startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_TIME), DISCOVERABLE_REQUEST_CODE);
				else openServer();
			}
		}
	}
	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if(resultCode == DISCOVERABLE_TIME) {
			discoverable = true;
			openServer();
		}
		else radioClient.setChecked(true);
	}
	private void openServer() {
		progressBar.setVisibility(View.VISIBLE);
		adapter.cancelDiscovery();
		final UUID uuid = UUID.nameUUIDFromBytes(uuidEditText.getText().toString().getBytes());
		if(runningThread != null) runningThread.interrupt();
		runningThread = new Thread() {
			public void run() {
				try {
					BluetoothServerSocket serverSocket = adapter.listenUsingRfcommWithServiceRecord(serviceName, uuid);
					while (!radioClient.isChecked()) {
						socket = serverSocket.accept();
						if (socket != null) {
							giveSocket(socket, true);
							serverSocket.close();
							dismiss();
							break;
						}
						if(interrupted()) break;
					}
					serverSocket.close();
				}
				catch (IOException e) {
					Log.e("PIRIMNK", "adapter.listen...() or serverSocket.accept() failed");
					AndroidUtilsKt.showToast(getActivity(), R.string.ioError);
					progressBar.setVisibility(View.GONE);
				}
			}
		};
		runningThread.start();
	}
	class BluetoothReceiver extends BroadcastReceiver {
		@Override public void onReceive(final Context context, final Intent intent) {
			if(intent.getAction() == null) return;
			if(intent.getAction().equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED) && intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.SCAN_MODE_NONE) != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
				discoverable = false;
				if(!radioClient.isChecked()) //if discoverability times out while in server mode, request for discoverability again.
					startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_TIME), DISCOVERABLE_REQUEST_CODE);
			}
			else if(intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				try {
					adapter.cancelDiscovery();
					socket = device.createRfcommSocketToServiceRecord(targetUUID);
					if(runningThread != null) runningThread.interrupt();
					runningThread = new Thread() {
						public void run() {
							try {
								socket.connect();
								giveSocket(socket, false);
							}
							catch (IOException e) {
								Log.e("PIRIMNK", "socket.connect() failed");
								AndroidUtilsKt.showToast(getActivity(), R.string.ioError);
								giveSocket(null, false);
							}
							finally {
								dismiss();
							}
						}
					};
					runningThread.start();
				}
				catch (IOException e) {
					Log.e("PIRIMNK", "device.createRfcommSocketToServiceRecord() failed");
					AndroidUtilsKt.showToast(getActivity(), R.string.ioError);
					progressBar.setVisibility(View.GONE);
				}
			}
		}
	}

	//SECTION: Connection established/canceled
	private void giveSocket(final BluetoothSocket socket, final boolean isServer) {
		getArguments().putBoolean(isServerString, isServer);
		getListener().giveResult(socket, getArguments());
	}
	@Override public void onDestroy() {
		getActivity().unregisterReceiver(receiver);
		if(runningThread != null) runningThread.interrupt();
		super.onDestroy();
	}
	private class BackListener implements DialogInterface.OnKeyListener {
		@Override public boolean onKey(final DialogInterface dialog, final int keyCode, final KeyEvent event) {
			if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
				giveSocket(null, false); //return to local if the user cancels connection
			return false;
		}
	}
}
