package com.example.r2d2.esignboard;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.bluetoothexample.R;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class MainActivity extends Activity {

    // mBtAdapter to moduł BT w tym urządzeniu
    private BluetoothAdapter mBtAdapter;
    // mLvDevices to wyświetlana lista w aplikacji
    private ListView mLvDevices;
    // mStringDeviceList to elementy wyświetlanej listy
    private ArrayList<String> mStringDeviceList;
    // mBtDeviceList to lista znalezionych odbiorników BT
    private ArrayList<BluetoothDevice> mBtDeviceList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLvDevices = (ListView) findViewById(R.id.lvDevices);
        mStringDeviceList = new ArrayList<String>();
        mBtDeviceList = new ArrayList<BluetoothDevice>()
        // Przypisanie do zmiennej domyślnego modułu BT
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        // Zarejestrowanie odbiornika
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBtReceiver, filter);

        // Gdy urządzenie ma moduł BT
        if(mBtAdapter != null) {
            //Gdy moduł BT jest włączony
            if(mBtAdapter.isEnabled()) {
                // Rozpoczęcie wyszukiwania urządzeń BT
                mBtAdapter.startDiscovery();
                Toast.makeText(this, getResources().getString(R.string.BT_start_discovery), Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, getResources().getString(R.string.BT_disabled), Toast.LENGTH_SHORT).show();
                //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        // Gdy urządzenie nie ma modułu BT
        else {
            Toast.makeText(this, getResources().getString(R.string.BT_no_adapter), Toast.LENGTH_SHORT).show();
        }

        // Gdy wybrano któryś element z wyświetlanej listy
        mLvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Zatrzymanie wyszukiwania urządzeń BT
                if (mBtAdapter.isDiscovering()) {
                    mBtAdapter.cancelDiscovery();
                }
                // Wybranie odpowiedniego urządzenia BT
                BluetoothDevice device = mBtDeviceList.get(position);
                // Funkcja łączoca z odpowiednim urządzeniem BT
                connect(device);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        // Odrejestrowanie odbiornika
        unregisterReceiver(mBtReceiver);
    }

    private final BroadcastReceiver mBtReceiver = new BroadcastReceiver() {

        // funkcja wywoływana gdy wykryto nowy odbiornik BT
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Dodanie znalezionego urządzenia do wyświetlanej listy
                mStringDeviceList.add(device.getName() + ", " + device.getAddress());
                // Dodanie znalezionego urządzenia do listy urządzeń
                mBtDeviceList.add(device);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, mStringDeviceList);
                mLvDevices.setAdapter(adapter);
            }
        }
    };

    // Funkcja łącząca z odpowiednim urządzeniem BT
    private Boolean connect(BluetoothDevice bdDevice) {
        Boolean bool = false;
        try {
            Class cl = Class.forName("android.bluetooth.BluetoothDevice");
            Class[] par = {};
            Method method = cl.getMethod("createBond", par);
            Object[] args = {};
            bool = (Boolean) method.invoke(bdDevice);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bool.booleanValue();
    };
}