package com.example.r2d2.mobileapp;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.bluetoothexample.R;

public class MainActivity extends Activity {

    private static final int REQUEST_ENABLE_BT = 1;

    private Button findBtn;
    private TextView text;
    private BluetoothAdapter myBluetoothAdapter;
    private Button updateBtn;

    private ListView myListView;
    private ArrayAdapter<String> BTArrayAdapter;
    private HashSet<String> eSignboardDevices  = new HashSet<String>();

    private WifiManager myWifiManager;
    private WifiReceiver myWifiReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // define ESignboard's Mac Adresses
        eSignboardDevices.add("00:1A:7D:DA:71:07");

        // connect to Wifi Manager and start discovering
        myWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        myWifiReceiver = new WifiReceiver();
        registerReceiver(myWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        myWifiManager.startScan();

        // take an instance of BluetoothAdapter - Bluetooth radio
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(myBluetoothAdapter == null) {

            text.setText("Status: not supported");

            Toast.makeText(getApplicationContext(),"Your device does not support Bluetooth",
                    Toast.LENGTH_LONG).show();
        } else {

            //turn on BT
            if (!myBluetoothAdapter.isEnabled()) {
                Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

                Toast.makeText(getApplicationContext(),"Bluetooth turned on" ,
                        Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(getApplicationContext(),"Bluetooth is already on",
                        Toast.LENGTH_LONG).show();
            }


            text = (TextView) findViewById(R.id.text);

            findBtn = (Button)findViewById(R.id.search);
            findBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    find(v);
                }
            });

            myListView = (ListView)findViewById(R.id.listView1);

            // create the arrayAdapter that contains the BTDevices, and set it to the ListView
            BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

            myListView.setAdapter(BTArrayAdapter);

            updateBtn = (Button)findViewById(R.id.update);
            updateBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    checkForUpdate();
                }
            });
        }
    }

    @Override
    protected void onPause() {
        unregisterReceiver(myWifiReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        registerReceiver(myWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if(requestCode == REQUEST_ENABLE_BT){
            if(myBluetoothAdapter.isEnabled()) {
                text.setText("Status: Enabled");
            } else {
                text.setText("Status: Disabled");
            }
        }
    }



    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name and the MAC address of the object to the arrayAdapter
                BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                BTArrayAdapter.notifyDataSetChanged();

                if(eSignboardDevices.contains(device.getAddress())){
                    notyfikuj("ESignboard in range!", "");
                }
            }
        }
    };


    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            if (myWifiManager.isWifiEnabled() && !(myWifiManager.getConnectionInfo().getSSID().equals("\"ESignboard\""))) {
                List<ScanResult> wifiScanList = myWifiManager.getScanResults();
                for (ScanResult result : wifiScanList) {
                    if (result.SSID.equals("ESignboard")) {
                        notyfikuj("mDevice in range!", "Connect to this network if you want to check for updates.");
                    }
                }
            }
        }
    }

    public void checkForUpdate() {
        if (myWifiManager.isWifiEnabled() && myWifiManager.getConnectionInfo().getSSID().equals("\"ESignboard\"")) {
            boolean update = false;
            try {
                File local_checksum = new File(getFilesDir().toString() + "/esignboard_data_checksum.bin");
                URL url1 = new URL("http://192.168.0.1/esignboard_data_checksum");
                URL url2 = new URL("file:///" + this.getFilesDir().toString() + "/esignboard_data_checksum.bin");

                Toast.makeText(getApplicationContext(),local_checksum.getPath().toString(),
                        Toast.LENGTH_LONG).show();

                if (local_checksum.exists()) {
                    BufferedReader mdevice = new BufferedReader(new InputStreamReader(url1.openStream()));
                    BufferedReader local = new BufferedReader(new InputStreamReader(url2.openStream()));
                    String str1, str2;
                    while ((str1 = mdevice.readLine()) != null && (str2 = local.readLine()) != null) {
                        if (!str1.equals(str2)) {
                            Toast.makeText(getApplicationContext(),"Pliki sie roznia!",
                                    Toast.LENGTH_LONG).show();
                            update = true;
                        }
                    }
                    mdevice.close();
                    local.close();
                } else {
                    Toast.makeText(getApplicationContext(),"Nie ma lokalnego pliku!",
                            Toast.LENGTH_LONG).show();
                    update = true;
                }

                if (update) {
                    final DownloadTask downloadTask = new DownloadTask(this);
                    downloadTask.execute("http://192.168.0.1/esignboard_data_checksum");
                    downloadTask.execute("http://192.168.0.1/esignboard_data.zip");
                } else {
                    Toast.makeText(getApplicationContext(),"Dane sa aktualne!",
                            Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(),"You're not connected to mDevice",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void find(View view) {
        if (myBluetoothAdapter.isDiscovering()) {
            // the button is pressed when it discovers, so cancel the discovery
            myBluetoothAdapter.cancelDiscovery();
        }
        else {
            BTArrayAdapter.clear();
            myBluetoothAdapter.startDiscovery();

            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }
    //a
    public void notyfikuj(String title,String message)
    {
        Intent intent = new Intent();
        PendingIntent pIntent = PendingIntent.getActivity(this,0,intent,0);

        Notification notify = new Notification.Builder(this)
                .setTicker(title)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent).getNotification();

        notify.flags = Notification.FLAG_AUTO_CANCEL;
        NotificationManager notifyManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notifyManager.notify(0,notify);
    }



    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unregisterReceiver(bReceiver);
        myBluetoothAdapter.disable();
        //   text.setText("Status: Disconnected");

        Toast.makeText(getApplicationContext(),"Bluetooth turned off",
                Toast.LENGTH_LONG).show();
    }

}
