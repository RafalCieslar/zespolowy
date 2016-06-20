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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
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

    private Button findBtn;
    private Button updateBtn;
    private ImageView image;


    private BluetoothAdapter myBluetoothAdapter;
    private ArrayAdapter<String> BTArrayAdapter;
    private HashSet<String> eSignboardDevices  = new HashSet<String>();
    private HashSet<BTObj> BTArrayAdapterObjects;

    private WifiManager myWifiManager;
    private WifiReceiver myWifiReceiver;
    private int netId;
    private boolean dontAskAgain = false;
    private String ip = "mdevice";
    private String wifiSSID = "\"ESignboard\"";
    private String wifiPASS = "";
    private String userID = "000000000000";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        image = (ImageView) findViewById(R.id.imageView);
        image.setImageResource(R.drawable.ulotka);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        BTArrayAdapterObjects = new HashSet<BTObj>();

        // TODO DO TESTOW
        //ip = "192.168.0.69:8080";
        //wifiSSID = "\"Ruter Sruter Dd\"";
        //wifiPASS = "\"trudnehaslo\"";
        // USUN JESLI ZOSTAWILEM



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

            checkAndRequestForBT();

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

       if (!myBluetoothAdapter.isDiscovering()) {myBluetoothAdapter.startDiscovery();}
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
                BluetoothDevice poi = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String poiAddress = poi.getAddress().replace(":","");
                // add the name and the MAC address of the object to the arrayAdapter
                BTArrayAdapter.add(poiAddress);
                BTArrayAdapter.notifyDataSetChanged();
                BTObj b = new BTObj();
                b.setAddress(poiAddress);
                int  rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                b.setRssi(rssi);

                //if new poi discovered, add it
                if(eSignboardDevices.contains(poiAddress)){

                    BTArrayAdapterObjects.add(b);
                    poiAddress = getDeviceWithMaxDBMAddress();

                    notyfikuj("ESignboard in range!", "");
                    addToVisits(poiAddress);
                    Intent showHtmlIntent = new Intent(getApplicationContext(), HtmlDisplay.class);
                    showHtmlIntent.putExtra("device-name", poiAddress);
                    myBluetoothAdapter.cancelDiscovery();
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

        if (wifiPASS.equals("")) {
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
        }
        else {
            conf.preSharedKey = wifiPASS;
        }
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



    private void requestWifiReconnection() {
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



    private void checkAndRequestForBT() {
        //turn on BT
        if (!myBluetoothAdapter.isEnabled()) {
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

            //Toast.makeText(getApplicationContext(),"Bluetooth turned on" ,
            //        Toast.LENGTH_LONG).show();
        }
        //else{
        //Toast.makeText(getApplicationContext(),"Bluetooth is already enabled",
        //        Toast.LENGTH_LONG).show();
        //}
    }



    private void updateFolderList() {
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



    private void addToVisits(String poi) {

        File visitsFile = new File(getFilesDir().toString(), "visits.txt");
        Calendar c = Calendar.getInstance();
        String thisVisit = userID + " " + c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH)+1) + "-" +
                c.get(Calendar.DAY_OF_MONTH) + " " + poi;
        StringBuffer allVisits = new StringBuffer();

        if (visitsFile.exists()) {

            try {

                FileInputStream visitsInputStream = new FileInputStream(visitsFile);
                BufferedReader visitsReader = new BufferedReader(new InputStreamReader(visitsInputStream));
                String visitLine;
                String visit;
                int visitCount;
                boolean newVisit = true;

                if (visitsInputStream != null) {
                    while ((visitLine = visitsReader.readLine()) != null) {
                        visit = visitLine.substring(0, thisVisit.length());
                        visitCount = Integer.parseInt(visitLine.substring(thisVisit.length() + 1, visitLine.length() - 1));
                        if (visit.equals(thisVisit)) {
                            visitCount++;
                            newVisit = false;
                        }
                        allVisits.append(visit + " " + visitCount + " \n");
                    }
                    if (newVisit) {
                        allVisits.append(thisVisit + " 1 \n");
                    }
                    visitsInputStream.close();
                }

            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Can't read visits! Probably this is your first visit after update...", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        }
        else {
            allVisits.append(thisVisit + " 1 \n");
        }

        try {

            FileOutputStream visitsOutputStream = new FileOutputStream(visitsFile);
            visitsOutputStream.write(allVisits.toString().getBytes());
            visitsOutputStream.close();

        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Can't save visit!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }



    private void uploadVisits() {

        File visitsFile = new File(getFilesDir().toString(), "visits.txt");

        if (visitsFile.exists()) {

            try {

                FileInputStream visitsFileStream = new FileInputStream(visitsFile);
                BufferedReader visitsReader = new BufferedReader(new InputStreamReader(visitsFileStream));
                String visit;
                StringBuffer allVisits = new StringBuffer();
                if (visitsFileStream != null) {
                    while ((visit = visitsReader.readLine()) != null) {
                        allVisits.append(visit);
                    }

                    sendPost(allVisits.toString());

                    visitsFileStream.close();
                    visitsFile.delete();
                }
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Can't read visits! Probably you didn't visited anything...", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        }
    }



    private void sendPost(String postAllData) {
        try {
            URL url = new URL("http://" + ip + "/statistics");

            String[] postData = postAllData.split(" ");
            StringBuilder postDataBuilder = new StringBuilder();
            for (int i = 0; i < postData.length; i+=4) {
                postDataBuilder.append(
                        URLEncoder.encode(postData[i], "UTF-8") + "," );
                postDataBuilder.append(
                        URLEncoder.encode(postData[i+1], "UTF-8") + "," );
                postDataBuilder.append(
                        URLEncoder.encode(postData[i+2], "UTF-8") + "," );
                postDataBuilder.append(
                        URLEncoder.encode(postData[i+3], "UTF-8") + "\n" );
            }
            byte[] postDataBytes = postDataBuilder.toString().getBytes("UTF-8");

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setFixedLengthStreamingMode(postDataBytes.length);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            urlConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            urlConnection.setDoOutput(true);
            urlConnection.getOutputStream().write(postDataBytes);
            urlConnection.getOutputStream().flush();

            // to do czytania odpowiedzi (nie potrzebne, ale chyba musi byc, moj servlet sie blokowal bez tego)
            Reader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            for (int c; (c = in.read()) >= 0;) {}

        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Can't upload visits!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }



    private void checkForUpdate() {

        if (myWifiManager.isWifiEnabled() && myWifiManager.getConnectionInfo().getSSID().equals(wifiSSID)) {

            uploadVisits();

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
                    Toast.makeText(getApplicationContext(), "No local checksum! Updating!",
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

    private String getDeviceWithMaxDBMAddress()
    {
        BTObj bt = new BTObj();
        bt.setRssi(-1000);
        bt.setAddress("");
        for (BTObj b: BTArrayAdapterObjects) {
            if(b.getRssi()>bt.getRssi()){
                bt.setAddress(b.getAddress());
                bt.setRssi(b.getRssi());
            }
        }
        return bt.getAddress();
    }

    private void find(View view) {
        checkAndRequestForBT();

        if (myBluetoothAdapter.isEnabled()) {

            if (userID.equals("000000000000")) {
                userID = myBluetoothAdapter.getAddress().replace(":", "");
                if (userID.equals("020000000000")) {
                    userID = android.provider.Settings.Secure.getString(this.getContentResolver(), "bluetooth_address").replace(":", "");
                }
            }

            // TODO DO USUNIECIA
            //addToVisits("012345678910");
            //addToVisits("999999999999");
            //addToVisits("333333333333");
            // USUN/ZAKOMENTUJ JEZELI ZOSTAWILEM

            if (myBluetoothAdapter.isDiscovering()) {
                // the button is pressed when it discovers, so cancel the discovery
                myBluetoothAdapter.cancelDiscovery();
            } else {
                updateFolderList();
                Toast.makeText(getApplicationContext(), "Searching in progress...",
                        Toast.LENGTH_LONG).show();
                BTArrayAdapter.clear();
                myBluetoothAdapter.startDiscovery();
                registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
        }
    }



    private void notyfikuj(String title,String message)
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