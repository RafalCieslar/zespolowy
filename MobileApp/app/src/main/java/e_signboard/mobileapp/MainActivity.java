package e_signboard.mobileapp;

import java.util.ArrayList;
import java.util.Set;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuItem;

import android.app.Activity;
import android.app.ProgressDialog;

import android.widget.TextView;
import android.widget.Toast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class MainActivity extends Activity {
	private TextView mStatusTv;
	
	private ProgressDialog mProgressDlg;
	
	private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
	
	private BluetoothAdapter mBluetoothAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		mStatusTv 			= (TextView) findViewById(R.id.tv_status);
		
		mBluetoothAdapter	= BluetoothAdapter.getDefaultAdapter();
		
		mProgressDlg 		= new ProgressDialog(this);
		
		mProgressDlg.setMessage("Scanning...");
		mProgressDlg.setCancelable(false);
		mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.dismiss();
		        
		        mBluetoothAdapter.cancelDiscovery();
		    }
		});
		
		if (mBluetoothAdapter == null) {
			showUnsupported();
		}
		else {
			if (mBluetoothAdapter.isEnabled()) {
				showEnabled();
			}
			else {
				showDisabled();
			}
		}
		
		IntentFilter filter = new IntentFilter();
		
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		
		registerReceiver(mReceiver, filter);
	}
	
	@Override
	public void onPause() {
		if (mBluetoothAdapter != null) {
			if (mBluetoothAdapter.isDiscovering()) {
				mBluetoothAdapter.cancelDiscovery();
			}
		}
		
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(mReceiver);
		
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.menu_enable_bt) {
			turnBtOnOff();
			invalidateOptionsMenu();
			return true;
		}
		else if (id == R.id.menu_paired_devices) {
			showPairedDevices();
			return true;
		}
		else if (id == R.id.menu_scan) {
			scanDevices();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		if (mBluetoothAdapter != null) {
			boolean btState = mBluetoothAdapter.isEnabled();
			menu.getItem(0).setTitle(btState ? "Disable" : "Enable");
			menu.getItem(1).setEnabled(btState);
			menu.getItem(2).setEnabled(btState);
		}
		else {
			menu.getItem(0).setEnabled(false);
			menu.getItem(1).setEnabled(false);
			menu.getItem(2).setEnabled(false);
		}
		return true;
	}

	private void showEnabled() {
		mStatusTv.setText("Bluetooth is On");
		mStatusTv.setTextColor(Color.BLUE);
	}
	
	private void showDisabled() {
		mStatusTv.setText("Bluetooth is Off");
		mStatusTv.setTextColor(Color.RED);
	}
	
	private void showUnsupported() {
		mStatusTv.setText("Bluetooth is unsupported by this device");
		mStatusTv.setTextColor(Color.RED);
	}

	private void turnBtOnOff() {
		if (mBluetoothAdapter.isEnabled()) {
			mBluetoothAdapter.disable();
			showDisabled();
		} else {
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, 1000);
		}
	}

	private void showPairedDevices () {
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

		if (pairedDevices == null || pairedDevices.size() == 0) {
			showToast("No Paired Devices Found");
		} else {
			ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();

			list.addAll(pairedDevices);

			Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);

			intent.putParcelableArrayListExtra("device.list", list);

			startActivity(intent);
		}
	}

	private void scanDevices () {
//		int hasPermission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
//		if (hasPermission == PackageManager.PERMISSION_GRANTED) {
			mBluetoothAdapter.startDiscovery();
//		}
//		else {
//			mBluetoothAdapter.startDiscovery();
//			showToast("Dont have permission to FINE_LOCATION");
//			ActivityCompat.requestPermissions(MainActivity.this,
//					new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
//					2);
//		}
	}

	private void showToast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        
	        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
	        	final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

	        	if (state == BluetoothAdapter.STATE_ON) {
	        		showToast("Enabled");

	        		showEnabled();
				}
	        } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
	        	mDeviceList = new ArrayList<BluetoothDevice>();
				
				mProgressDlg.show();
	        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
	        	mProgressDlg.dismiss();
	        	
	        	Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
	        	
	        	newIntent.putParcelableArrayListExtra("device.list", mDeviceList);
				
				startActivity(newIntent);
	        } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	        	BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		        
	        	mDeviceList.add(device);
	        	
	        	showToast("Found device " + device.getName());
	        }
	    }
	};

}