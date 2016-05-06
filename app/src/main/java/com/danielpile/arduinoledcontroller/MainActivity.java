package com.danielpile.arduinoledcontroller;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.Preference;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.larswerkman.holocolorpicker.ColorPicker;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.prefs.Preferences;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final int REQUEST_ENABLE_BT = 1;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice device;

    public static final String MyPREFERENCES = "MyPrefs";

    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;


    ArrayList<BluetoothDevice> pairedDeviceArrayList;

    long starttime = 0L;
    long elapsedtime = 0L;
    boolean runner;
    TextView textInfo, textStatus;
    ListView listViewPairedDevice;
    LinearLayout splashPane;
    LinearLayout connectionPane;

    LinearLayout colorPane;
    TextView textView;
    /*TextView txt_red;
    TextView txt_green;
    TextView txt_blue;*/

    TextView txt_debug;

    Spinner cmb_mode;

    ArrayAdapter<BluetoothDevice> pairedDeviceAdapter;
    private UUID myUUID;
    private final String UUID_STRING_WELL_KNOWN_SPP =
            "00001101-0000-1000-8000-00805F9B34FB";

    ThreadConnectBTdevice myThreadConnectBTdevice;
    ThreadConnected myThreadConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textInfo = (TextView)findViewById(R.id.info);
        textStatus = (TextView)findViewById(R.id.status);
        listViewPairedDevice = (ListView)findViewById(R.id.pairedlist);
        connectionPane = (LinearLayout) findViewById(R.id.connectionPane);

        starttime = SystemClock.uptimeMillis();

        splashPane = (LinearLayout)findViewById(R.id.splashPane);
        colorPane = (LinearLayout)findViewById(R.id.colorPane);

        ColorPicker picker = (ColorPicker) findViewById(R.id.picker);

        picker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener()
        {
            @Override
            public void onColorChanged(int color) {
                elapsedtime = SystemClock.uptimeMillis() - starttime;
                Log.i("ColorSeekBar","color:" + color);
                Log.i("ColorSeekBar","ElapsedTime:" + elapsedtime);
                if (elapsedtime < 95)
                {
                    Log.i("ColorSeekBar","RateLimiting:" + elapsedtime);
                    return;
                }
                starttime = SystemClock.uptimeMillis();
                String command = "";
                String r = String.valueOf(Color.red(color));
                String g = String.valueOf(Color.green(color));
                String b = String.valueOf(Color.blue(color));
                command = new StringBuilder("kSetLEDs,s,").append(r).append(",").append(g).append(",").append(b).append(",0").toString();
                sendCommmand(command);
            }
        });


        txt_debug = (TextView)findViewById(R.id.txt_debug);
        cmb_mode = (Spinner) findViewById(R.id.cmb_mode);

        // Spinner click listener
        cmb_mode.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("Modes");
        categories.add("White");
        categories.add("Red");
        categories.add("Green");
        categories.add("Blue");
        categories.add("Rainbow");
        categories.add("RainbowCycle");
        categories.add("Demo");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        cmb_mode.setAdapter(dataAdapter);


        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)){
            Toast.makeText(this,
                    "FEATURE_BLUETOOTH NOT support",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        //using the well-known SPP UUID
        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this,
                    "Bluetooth is not supported on this hardware platform",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String stInfo = bluetoothAdapter.getName() + "\n" +
                bluetoothAdapter.getAddress();
        textInfo.setText(stInfo);
    }

    private void sendCommmand(String command) {
        try {
            if (myThreadConnectBTdevice == null) {
                txt_debug.setText("No device connected!");
            }
            else
            {
                try {
                    command = new StringBuilder("cmdMessenger.sendCmd(").append(command).append(");").toString();
                    byte[] bytesToSend = command.getBytes();
                    myThreadConnected.write(bytesToSend);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            txt_debug.setText("Exception occurred sending command: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Turn ON BlueTooth if it is OFF
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        String address = sharedpreferences.getString("Device_Address","");
        Log.i("Derp","Address: " + address);
        if(address != ""){
            Toast.makeText(MainActivity.this,
                    "Attempting to connect to : " + address,
                    Toast.LENGTH_LONG).show();
            device = bluetoothAdapter.getRemoteDevice(address);
            if (device != null) {
                myThreadConnectBTdevice = new ThreadConnectBTdevice(device);
                myThreadConnectBTdevice.start();
            } else {
                Toast.makeText(MainActivity.this,
                        "Derp",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            splashPane.setVisibility(View.GONE);
            connectionPane.setVisibility(View.VISIBLE);
            setup();
        }

    }

    private void setup() {

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                pairedDeviceArrayList = new ArrayList<BluetoothDevice>();

                for (BluetoothDevice device : pairedDevices) {
                    pairedDeviceArrayList.add(device);
                }

                pairedDeviceAdapter = new ArrayAdapter<BluetoothDevice>(this,
                        android.R.layout.simple_list_item_1, pairedDeviceArrayList);
                listViewPairedDevice.setAdapter(pairedDeviceAdapter);

                listViewPairedDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        device =
                                (BluetoothDevice) parent.getItemAtPosition(position);
                        Toast.makeText(MainActivity.this,
                                "Name: " + device.getName() + "\n"
                                        + "Address: " + device.getAddress() + "\n"
                                        + "BondState: " + device.getBondState() + "\n"
                                        + "BluetoothClass: " + device.getBluetoothClass() + "\n"
                                        + "Class: " + device.getClass(),
                                Toast.LENGTH_LONG).show();

                        textStatus.setText("start ThreadConnectBTdevice");


                        CheckBox checkBox = (CheckBox) findViewById(R.id.chk_save);
                        if (checkBox.isChecked()) {
                            editor.putString("Device_Address", device.getAddress());
                            editor.commit();
                        }

                        if (device != null) {
                            myThreadConnectBTdevice = new ThreadConnectBTdevice(device);
                            myThreadConnectBTdevice.start();
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Failed to connect to device",
                                    Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                });
            }

    }

    @Override
    protected void onPause(){
        super.onPause();
        try {
            myThreadConnected.cancel();
        } catch (NullPointerException e) {
            //methreadconnected null
        }
        if(myThreadConnectBTdevice!=null){
            myThreadConnectBTdevice.cancel();
        }
    }
    private void reconnect() {
        myThreadConnectBTdevice = new ThreadConnectBTdevice(device);
        myThreadConnectBTdevice.start();
    }


    @Override
    protected void onResume(){
        super.onResume();
        if(myThreadConnectBTdevice!=null){
            reconnect();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            myThreadConnected.cancel();
        } catch (NullPointerException e) {
            //methreadconnected null
        }

        if(myThreadConnectBTdevice!=null){
            myThreadConnectBTdevice.cancel();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_ENABLE_BT){
            if(resultCode == Activity.RESULT_OK){
                setup();
            }else{
                Toast.makeText(this,
                        "BlueTooth NOT enabled",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    //Called in ThreadConnectBTdevice once connect successed
    //to start ThreadConnected
    private void startThreadConnected(BluetoothSocket socket){

        myThreadConnected = new ThreadConnected(socket);
        myThreadConnected.start();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String command = "";
        switch (position) {
            case 0:
                // Whatever you want to happen when the first item gets selected

                break;
            case 1:
                // Whatever you want to happen when the first item gets selected
                command = "kSetLEDs,s,255,255,255,25";
                sendCommmand(command);
                break;
            case 2:
                // Whatever you want to happen when the second item gets selected
                command = "kSetLEDs,s,255,0,0,25";
                sendCommmand(command);
                break;
            case 3:
                // Whatever you want to happen when the thrid item gets selected
                command = "kSetLEDs,s,0,255,0,25";
                sendCommmand(command);
                break;
            case 4:
                // Whatever you want to happen when the thrid item gets selected
                command = "kSetLEDs,s,0,0,255,25";
                sendCommmand(command);
                break;
            case 5:
                // Whatever you want to happen when the thrid item gets selected
                command = "kSetLEDs,r";
                sendCommmand(command);
                break;
            case 6:
                // Whatever you want to happen when the thrid item gets selected
                command = "kSetLEDs,q";
                sendCommmand(command);
                break;
            case 7:
                // Whatever you want to happen when the thrid item gets selected
                command = "kSetLEDs,d";
                sendCommmand(command);
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /*
    ThreadConnectBTdevice:
    Background Thread to handle BlueTooth connecting
    */
    private class ThreadConnectBTdevice extends Thread {

        private BluetoothSocket bluetoothSocket = null;
        private final BluetoothDevice bluetoothDevice;


        private ThreadConnectBTdevice(BluetoothDevice device) {
            bluetoothDevice = device;

            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
                textStatus.setText("bluetoothSocket: \n" + bluetoothSocket);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            boolean success = false;
            try {
                bluetoothSocket.connect();
                success = true;
            } catch (IOException e) {
                e.printStackTrace();

                final String eMessage = e.getMessage();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        textStatus.setText("something wrong bluetoothSocket.connect(): \n" + eMessage);
                    }
                });

                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

            if(success){
                //connect successful
                final String msgconnected = "connect successful:\n"
                        + "BluetoothSocket: " + bluetoothSocket + "\n"
                        + "BluetoothDevice: " + bluetoothDevice;

                runOnUiThread(new Runnable(){

                    @Override
                    public void run() {
                        textStatus.setText(msgconnected);
                        splashPane.setVisibility(View.GONE);
                        connectionPane.setVisibility(View.GONE);
                        colorPane.setVisibility(View.VISIBLE);
                    }});

                startThreadConnected(bluetoothSocket);
            }else{
                //fail
            }
        }

        public void cancel() {

            Toast.makeText(getApplicationContext(),
                    "close bluetoothSocket",
                    Toast.LENGTH_LONG).show();

            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

    /*
    ThreadConnected:
    Background Thread to handle Bluetooth data communication
    after connected
     */
    public class ThreadConnected extends Thread {
        private final BluetoothSocket connectedBluetoothSocket;
        private final InputStream connectedInputStream;
        private final OutputStream connectedOutputStream;

        public ThreadConnected(BluetoothSocket socket) {
            connectedBluetoothSocket = socket;
            InputStream in = null;
            OutputStream out = null;

            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            connectedInputStream = in;
            connectedOutputStream = out;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (runner) {
                try {
                    bytes = connectedInputStream.read(buffer);
                    String strReceived = new String(buffer, 0, bytes);
                    final String msgReceived = String.valueOf(bytes) +
                            " bytes received:\n"
                            + strReceived;

                    runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            textStatus.setText(msgReceived);
                        }});

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                    final String msgConnectionLost = "Connection lost:\n"
                            + e.getMessage();
                    runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            textStatus.setText(msgConnectionLost);
                        }});
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                connectedOutputStream.write(buffer);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                runner = false;
                connectedBluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_clear) {
            sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
            editor = sharedpreferences.edit();
            editor.putString("Device_Address","");
            editor.commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
/*
Android Example to connect to and communicate with Bluetooth
In this exercise, the target is a Arduino Due + HC-06 (Bluetooth Module)

Ref:
- Make BlueTooth connection between Android devices
http://android-er.blogspot.com/2014/12/make-bluetooth-connection-between.html
- Bluetooth communication between Android devices
http://android-er.blogspot.com/2014/12/bluetooth-communication-between-android.html
 */
