package e_signboard.mobileapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ACCESS_COARSE_LOCATION_PERMISSION = 2;

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
    private boolean dontAskAgain = false;
    private String ip = "mdevice";
    private String wifiSSID = "\"ESignboard\"";
    //private String ip = "192.168.0.59";
    //private String wifiSSID = "\"Ruter Sruter Dd\"";
    //private String wifiPASS = "\"trudnehaslo\"";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        image = (ImageView) findViewById(R.id.imageView);
        image.setImageResource(R.drawable.ulotka);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);



        // sprawdzenie czy apka ma uprawnienia do COARSE_LOCATION
        // jak nie to robi request
        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_ACCESS_COARSE_LOCATION_PERMISSION);
        }



//        ten update listy folderow dałem do przycisku Search (do funkcji find)!
//        updateFolderList();



        // connecting to Wifi Manager
        myWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        // adding WiFi configuration
        addWifiConf();
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

                //Toast.makeText(getApplicationContext(),"Bluetooth turned on" ,
                //        Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(getApplicationContext(),"Bluetooth is already enabled",
                        Toast.LENGTH_LONG).show();
            }



            // create the arrayAdapter that contains the BTDevices
            BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);



            findBtn = (Button)findViewById(R.id.search);
            findBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    find(v);
                }
            });



            updateBtn = (Button)findViewById(R.id.update);
            updateBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    checkForUpdate();
                }
            });



        }
    }



    @Override
    protected void onPause() {
        try {
            unregisterReceiver(myWifiReceiver);
        } catch (RuntimeException re) {
            re.toString();
        }

        super.onPause();
    }



    @Override
    protected void onResume() {
        if (!dontAskAgain)
            registerReceiver(myWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT: {
                if(myBluetoothAdapter.isEnabled()) {
                    Toast.makeText(MainActivity.this, "Bluetooth enabled", Toast.LENGTH_SHORT).show();
                }
                //else {
                //    Toast.makeText(MainActivity.this, "Bluetooth disabled", Toast.LENGTH_SHORT).show();
                //}
            }
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_COARSE_LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(getApplicationContext(), "Permissions granted!",
                            Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(getApplicationContext(), "Permissions denied!",
                            Toast.LENGTH_LONG).show();

                }
                return;
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
                    Intent showHtmlIntent = new Intent(getApplicationContext(),HtmlDisplay.class);
                    showHtmlIntent.putExtra("device-name",device.getAddress().replace(":",""));
                    startActivity(showHtmlIntent);
                }
            }
        }
    };



    class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (myWifiManager.isWifiEnabled() && !(myWifiManager.getConnectionInfo().getSSID().equals(wifiSSID))) {
                List<ScanResult> wifiScanList = myWifiManager.getScanResults();

                for (ScanResult result : wifiScanList) {
                    if (result.SSID.equals(wifiSSID.substring(1, wifiSSID.length()-1))) {
                        // zamiast notyfikacji jest laczenie do sieci WiFi
                        //notyfikuj("mDevice in range!", "Connect to this network if you want to check for update.");

                        // asking for reconnection
                        requestWifiReconnection();

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
        conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        conf.allowedAuthAlgorithms.clear();
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        //conf.preSharedKey = wifiPASS;
        List<WifiConfiguration> list = myWifiManager.getConfiguredNetworks();

        netId = -1;
        for( WifiConfiguration i : list ) {
            if (i.SSID != null && i.SSID.equals(wifiSSID)) {
                //Toast.makeText(getApplicationContext(),"Updating WiFi config!" ,Toast.LENGTH_LONG).show();
                netId = i.networkId;
                myWifiManager.removeNetwork(netId);
            }
        }
        //if (netId == -1) {
        //    Toast.makeText(getApplicationContext(),"New WiFi config!" ,Toast.LENGTH_LONG).show();
        //}
        netId = myWifiManager.addNetwork(conf);

    }



    public void requestWifiReconnection() {
        if (!dontAskAgain) {
            dontAskAgain = true;

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Czy chcesz połączyć się z mDevice za pomocą WiFi?");
            builder.setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Yes button clicked
                    // connecting to WiFi
                    dialog.dismiss();
                    myWifiManager.disconnect();
                    myWifiManager.enableNetwork(netId, false);
                    myWifiManager.reconnect();
                }
            });
            builder.setNegativeButton("Nie", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // No button clicked
                    unregisterReceiver(myWifiReceiver);
                    dontAskAgain = true;
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
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
        //Toast.makeText(getApplicationContext(), eSignboardDevices.toString() ,Toast.LENGTH_LONG).show();
    }



    public void checkForUpdate() {

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
                    Toast.makeText(getApplicationContext(),"No local file! Updating!",
                            Toast.LENGTH_LONG).show();
                    update = true;
                }

                if (update) {
                    final DownloadTask downloadSum = new DownloadTask(this);
                    final DownloadTask downloadZip = new DownloadTask(this);

                    downloadSum.execute("http://" + ip + "/esignboard_data_checksum");
                    downloadZip.execute("http://" + ip + "/esignboard_data.zip");
                } else {
                    Toast.makeText(getApplicationContext(), "Data are up to date!",
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
        // gdy nie jest zerejestrowany jeszcze to sie sypal...
        // a tak w sumie to gdyby go zarejestrowac w on create to by wyszukiwal chyba od poczatku
        try {
            unregisterReceiver(bReceiver);
        } catch (RuntimeException re) {
            re.toString();
        }
        myBluetoothAdapter.disable();
        try {
        unregisterReceiver(myWifiReceiver);
        } catch (RuntimeException re) {
            re.toString();
        }

        Toast.makeText(getApplicationContext(),"Bluetooth disabled",
                Toast.LENGTH_LONG).show();

        super.onDestroy();
    }

}