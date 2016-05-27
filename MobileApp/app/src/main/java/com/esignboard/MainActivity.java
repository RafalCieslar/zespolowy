package com.esignboard;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.List;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.esignboard.R;

public class MainActivity extends Activity {

    private static final int REQUEST_ENABLE_BT = 1;

    //moj pc: 001a7dda71
    //maciek WinPhone: 48507368ca3a
    //maciek PC: 419039111468

    private Button findBtn;
    private Button updateBtn;
    private ImageView image;


    private BluetoothAdapter myBluetoothAdapter;
    private ArrayAdapter<String> BTArrayAdapter;
    private HashSet<String> eSignboardDevices  = new HashSet<String>();


    private WifiManager myWifiManager;
    private WifiReceiver myWifiReceiver;
    private int netId;
    private String wifiSSID = "\"ESignboard\"";
    //private String wifiPASS = "\"haslo\"";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        image = (ImageView) findViewById(R.id.imageView);
        image.setImageResource(R.drawable.ulotka);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);


        //get list of  devices defined in fcking asset folder

//            for (File file : getFilesDir().listFiles()) {
//
//                    if(file.isDirectory()) {
//                        eSignboardDevices.add(file.getName());
//                    }
//            }
        //v2


//        ten update listy folderow dałem do przycisku Search (do funkcji find)!
        //updateFolderList();

           // eSignboardDevices.add("001A7DDA7107");
        //CIESLAR TU SO WSZYSTKIE PLIKI
        //String f = getFilesDir().toString()+"/";
//
//        File yourDir = new File(getFilesDir().);
//        for (File f : yourDir.listFiles()) {
//            if (f.isFile())
//               eSignboardDevices.add(f.getName().replaceAll(".html",""));
//            // make something with the name
//        }

        // connecting to Wifi Manager
        myWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        // starting discovering WiFi
        myWifiReceiver = new WifiReceiver();
        registerReceiver(myWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        myWifiManager.startScan();



        // take an instance of BluetoothAdapter - Bluetooth radio
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(myBluetoothAdapter == null) {

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


            // create the arrayAdapter that contains the BTDevices
            BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);



            findBtn = (Button)findViewById(R.id.search);
            findBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    find(v);
                }
            });



            updateBtn = (Button)findViewById(R.id.update);
            updateBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    checkForUpdate();
                }
            });


            // adding WiFi configuration
            addWifiConf();
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
                Toast.makeText(MainActivity.this, "BT Enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "BT Disabled", Toast.LENGTH_SHORT).show();
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
                BTArrayAdapter.add(device.getAddress().replace(":",""));
                BTArrayAdapter.notifyDataSetChanged();

                if(eSignboardDevices.contains(device.getAddress().replace(":",""))){
                    notyfikuj("ESignboard in range!", "");
                    Intent intent2 = new Intent(getApplicationContext(),HtmlDisplay.class);
                    intent2.putExtra("device-name",device.getAddress().replace(":",""));
                    startActivity(intent2);
                }
            }
        }
    };



    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            if (myWifiManager.isWifiEnabled() && !(myWifiManager.getConnectionInfo().getSSID().equals(wifiSSID))) {
                List<ScanResult> wifiScanList = myWifiManager.getScanResults();
                for (ScanResult result : wifiScanList) {
                    if (result.SSID.equals(wifiSSID)) {
                        // zamiast notyfikacji jest laczenie do sieci WiFi
                        //notyfikuj("mDevice in range!", "Connect to this network if you want to check for update.");

                        // connecting to WiFi
                        Toast.makeText(MainActivity.this, "Connecting to WiFi!", Toast.LENGTH_SHORT).show();
                        myWifiManager.disconnect();
                        myWifiManager.enableNetwork(netId, true);
                        myWifiManager.reconnect();
                    }
                }
            }
            // Tutaj mozna dodac automatyczny update
            //if (myWifiManager.isWifiEnabled() && (myWifiManager.getConnectionInfo().getSSID().equals(wifiSSID)))
                //Toast.makeText(MainActivity.this, "Connected to WiFi!", Toast.LENGTH_SHORT).show();
        }
    }



    private void addWifiConf() {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = wifiSSID;
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        //conf.preSharedKey = wifiPASS;
        List<WifiConfiguration> list = myWifiManager.getConfiguredNetworks();
        netId = -1;
        for( WifiConfiguration i : list ) {
            if (i.SSID != null && i.SSID.equals(wifiSSID)) {
                //Toast.makeText(getApplicationContext(),"Updating WiFi conf!" ,Toast.LENGTH_LONG).show();
                netId = i.networkId;
                conf.networkId = netId;
                myWifiManager.updateNetwork(conf);
            }
        }
        if (netId == -1) {
            //Toast.makeText(getApplicationContext(),"New WiFi conf!" ,Toast.LENGTH_LONG).show();
            netId = myWifiManager.addNetwork(conf);
        }
    }



    public void updateFolderList() {
        String path = getFilesDir().toString();

        eSignboardDevices.clear();
        File f = new File(path);
        File[] files = f.listFiles();
        for (File inFile : files) {
            if (inFile.isDirectory() && ! inFile.getName().equals("instant-run") ) {
                // is directory (but not instant-run)
                eSignboardDevices.add( inFile.getName().toUpperCase() );
            }
        }
        Toast.makeText(getApplicationContext(), eSignboardDevices.toString() ,Toast.LENGTH_LONG).show();
    }



    public void checkForUpdate() {
        String ip = "mdevice";

        if (myWifiManager.isWifiEnabled() && myWifiManager.getConnectionInfo().getSSID().equals(wifiSSID)) {
            boolean update = false;
            try {
                File local_checksum = new File(getFilesDir().toString() + "/esignboard_data_checksum.bin");



                URL url1 = new URL("http://" + ip + "/esignboard_data_checksum");
                URL url2 = new URL("file:///" + this.getFilesDir().toString() + "/esignboard_data_checksum.bin");

                if (local_checksum.exists()) {
                    BufferedReader mdevice = new BufferedReader(new InputStreamReader(url1.openStream()));
                    BufferedReader local = new BufferedReader(new InputStreamReader(url2.openStream()));
                    String str1, str2;
                    while ((str1 = mdevice.readLine()) != null && (str2 = local.readLine()) != null) {
                        if (!str1.equals(str2)) {
                            Toast.makeText(getApplicationContext(),"New version available! Updating!",
                                    Toast.LENGTH_LONG).show();
                            update = true;
                        }
                    }
                    mdevice.close();
                    local.close();
                } else {
                    Toast.makeText(getApplicationContext(),"No local file!",
                            Toast.LENGTH_LONG).show();
                    update = true;
                }

                if (update) {
                    final DownloadTask downloadSum = new DownloadTask(this);
                    final DownloadTask downloadZip = new DownloadTask(this);

                    downloadSum.execute("http://" + ip + "/esignboard_data_checksum");
                    downloadZip.execute("http://" + ip + "/esignboard_data.zip");
                } else {
                    Toast.makeText(getApplicationContext(),"Data are up to date!",
                            Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(),"You're not connected to mDevice!",
                    Toast.LENGTH_LONG).show();
        }
    }



    public void find(View view) {
        if (myBluetoothAdapter.isDiscovering()) {
            // the button is pressed when it discovers, so cancel the discovery
            myBluetoothAdapter.cancelDiscovery();
        }
        else {
            updateFolderList();
            Toast.makeText(getApplicationContext(),"Searching in progress...",
                    Toast.LENGTH_LONG).show();
            BTArrayAdapter.clear();
            myBluetoothAdapter.startDiscovery();

            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }



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
        // gdy nie jest zerejestrowany jeszcze to sie sypal...
        // a tak w sumie to gdyby go zarejestrowac w on create to by wyszukiwal chyba od poczatku
        try {
            unregisterReceiver(bReceiver);
        } catch (RuntimeException re) {
            re.toString();
        }
        myBluetoothAdapter.disable();

        Toast.makeText(getApplicationContext(),"Bluetooth turned off",
                Toast.LENGTH_LONG).show();
    }

}
