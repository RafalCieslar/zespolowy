package e_signboard.mobileapp;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuItem;

import android.app.Activity;
import android.app.ProgressDialog;

import android.view.View;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

public class MainActivity extends Activity {
	private TextView mBtOffTv;
	private WebView mContentWv;

	private ProgressDialog mProgressDlg;
	private ProgressDialog mDownloadProgressDlg;

	private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();

	private BluetoothAdapter mBluetoothAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		mBtOffTv 			= (TextView) findViewById(R.id.BT_off_text);
		mContentWv			= (WebView) findViewById(R.id.webView);

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

		mDownloadProgressDlg = new ProgressDialog(this);
		mDownloadProgressDlg.setMessage("Downloading");
		mDownloadProgressDlg.setIndeterminate(true);
		mDownloadProgressDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mDownloadProgressDlg.setCancelable(true);

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
		else if (id == R.id.view_webview) {
			if (mContentWv.getVisibility() == View.VISIBLE)
				hideWebView();
			else
				showWebView();
		}
		else if (id == R.id.test_download) {
			downloadFile("http://www.kurshtml.edu.pl/html/zielony.html");
		}
		else if (id == R.id.test_view) {
			loadFileToWebView("zielony");
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
		mBtOffTv.setText("Bluetooth is On");
		mBtOffTv.setTextColor(Color.BLUE);
	}

	private void showDisabled() {
		mBtOffTv.setText("Bluetooth is Off");
		mBtOffTv.setTextColor(Color.RED);
	}

	private void showUnsupported() {
		mBtOffTv.setText("Bluetooth is unsupported by this device");
		mBtOffTv.setTextColor(Color.RED);
	}

	private void showWebView() {
		mContentWv.setVisibility(View.VISIBLE);
		mBtOffTv.setVisibility(View.GONE);
	}

	private void hideWebView() {
		mContentWv.setVisibility(View.INVISIBLE);
		mBtOffTv.setVisibility(View.VISIBLE);
	}

	private void loadFileToWebView(String file) {
		mContentWv.loadUrl("file:///" + this.getFilesDir().toString() + "/" + file + ".html");
	}

	private void downloadFile(String url) {
		final DownloadTask downloadTask = new DownloadTask(this);
		downloadTask.execute(url);

		mDownloadProgressDlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				downloadTask.cancel(true);
			}
		});
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

	private class DownloadTask extends AsyncTask<String, Integer, String> {

		private Context context;
		private PowerManager.WakeLock mWakeLock;

		public DownloadTask(Context context) {
			this.context = context;
		}

		@Override
		protected String doInBackground(String... sUrl) {
			InputStream input = null;
			OutputStream output = null;
			HttpURLConnection connection = null;
			try {
				URL url = new URL(sUrl[0]);
				connection = (HttpURLConnection) url.openConnection();
				connection.connect();

				// expect HTTP 200 OK, so we don't mistakenly save error report
				// instead of the file
				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
					return "Server returned HTTP " + connection.getResponseCode()
							+ " " + connection.getResponseMessage();
				}

				// this will be useful to display download percentage
				// might be -1: server did not report the length
				int fileLength = connection.getContentLength();

				// download the file
				input = connection.getInputStream();
				output = new FileOutputStream(MainActivity.this.getFilesDir().toString() + "/" + URLUtil.guessFileName(url.toString(), null, null));
				byte data[] = new byte[4096];
				long total = 0;
				int count;
				while ((count = input.read(data)) != -1) {
					// allow canceling with back button
					if (isCancelled()) {
						input.close();
						return null;
					}
					total += count;
					// publishing the progress....
					if (fileLength > 0) // only if total length is known
						publishProgress((int) (total * 100 / fileLength));
					output.write(data, 0, count);
				}
			} catch (Exception e) {
				return e.toString();
			} finally {
				try {
					if (output != null)
						output.close();
					if (input != null)
						input.close();
				} catch (IOException ignored) {
				}

				if (connection != null)
					connection.disconnect();
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// take CPU lock to prevent CPU from going off if the user
			// presses the power button during download
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					getClass().getName());
			mWakeLock.acquire();
			mDownloadProgressDlg.show();
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			// if we get here, length is known, now set indeterminate to false
			mDownloadProgressDlg.setIndeterminate(false);
			mDownloadProgressDlg.setMax(100);
			mDownloadProgressDlg.setProgress(progress[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			mWakeLock.release();
			mDownloadProgressDlg.dismiss();
			if (result != null)
				showToast("Download error: "+result);
			else
				showToast("File downloaded");
		}
	}
}