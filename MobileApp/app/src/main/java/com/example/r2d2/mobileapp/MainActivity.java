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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import android.content.Intent;
import android.content.IntentFilter;
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

    private ListView myListView;
    private ArrayAdapter<String> BTArrayAdapter;
    private HashSet<String> eSignboardDevices  = new HashSet<String>();

    private WifiManager myWifiManager;
    private WifiReceiver myWifiReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
