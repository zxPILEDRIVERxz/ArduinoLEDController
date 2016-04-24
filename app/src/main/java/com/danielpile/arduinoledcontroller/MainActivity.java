package com.danielpile.arduinoledcontroller;

import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
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
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.rtugeek.android.colorseekbar.ColorSeekBar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice device;

    ArrayList<BluetoothDevice> pairedDeviceArrayList;

    long starttime = 0L;
    long elapsedtime = 0L;
    boolean runner;
    TextView textInfo, textStatus;
    ListView listViewPairedDevice;
    //LinearLayout inputPane;
    //EditText inputField;
    //Button btnSend;
    //Button btn_changecolor;

    LinearLayout colorPane;
    ColorSeekBar colorSeekBar;
    TextView textView;
    /*TextView txt_red;
    TextView txt_green;
    TextView txt_blue;*/
    Button btn_color_white;
    Button btn_color_red;
    Button btn_color_green;
    Button btn_color_blue;
    TextView txt_debug;

    ArrayAdapter<BluetoothDevice> pairedDeviceAdapter;
    private UUID myUUID;
    private final String UUID_STRING_WELL_KNOWN_SPP =
            "00001101-0000-1000-8000-00805F9B34FB";

    ThreadConnectBTdevice myThreadConnectBTdevice;
    ThreadConnected myThreadConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textInfo = (TextView)findViewById(R.id.info);
        textStatus = (TextView)findViewById(R.id.status);
        listViewPairedDevice = (ListView)findViewById(R.id.pairedlist);

        starttime = SystemClock.uptimeMillis();

        /*inputPane = (LinearLayout)findViewById(R.id.inputpane);
        inputField = (EditText)findViewById(R.id.input);
        btnSend = (Button)findViewById(R.id.send);
        //btn_changecolor = (Button) findViewById(R.id.btn_changecolor);
        btnSend.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(myThreadConnected!=null){
                    byte[] bytesToSend = inputField.getText().toString().getBytes();
                    myThreadConnected.write(bytesToSend);
                }
            }});*/

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
                textView.setTextColor(color);
                String r = String.valueOf(Color.red(color));
                String g = String.valueOf(Color.green(color));
                String b = String.valueOf(Color.blue(color));
                command = new StringBuilder("kSetLEDs,s,").append(r).append(",").append(g).append(",").append(b).append(",0").toString();
                sendCommmand(command);
            }
        });

        //colorSeekBar = (ColorSeekBar) findViewById(R.id.colorSlider);
        textView = (TextView)findViewById(R.id.colorText);
        /*txt_red = (TextView)findViewById(R.id.txt_red);
        txt_green = (TextView)findViewById(R.id.txt_green);
        txt_blue = (TextView)findViewById(R.id.txt_blue);*/
        btn_color_white = (Button)findViewById(R.id.btn_color_white);
        btn_color_red = (Button)findViewById(R.id.btn_color_red);
        btn_color_green = (Button)findViewById(R.id.btn_color_green);
        btn_color_blue = (Button)findViewById(R.id.btn_color_blue);
        txt_debug = (TextView)findViewById(R.id.txt_debug);

        /*colorSeekBar.setMaxValue(1000);
        colorSeekBar.setColors(R.array.material_colors); // material_colors is defalut included in res/color,just use it.
        colorSeekBar.setColorBarValue(0); //0 - maxValue
        colorSeekBar.setAlphaBarValue(0); //0-255
        colorSeekBar.setShowAlphaBar(false);
        colorSeekBar.setBarHeight(5); //5dpi
        colorSeekBar.setThumbHeight(30); //30dpi
        colorSeekBar.setBarMargin(10); //set the margin between colorBar and alphaBar 10dpi*/
        //textView.setTextColor(colorSeekBar.getColor());
        textView.setTextColor(picker.getColor());
        /*colorSeekBar.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
            @Override
            public void onColorChangeListener(int colorBarValue, int alphaBarValue, int color) {
                Log.i("ColorSeekBar","colorPosition:"+ colorBarValue +"-alphaPosition:"+ alphaBarValue);
                Log.i("ColorSeekBar","color:" + color);
                String command = "";
                textView.setTextColor(color);
                String r = String.valueOf(Color.red(color));
                String g = String.valueOf(Color.green(color));
                String b = String.valueOf(Color.blue(color));
                txt_red.setText(r);
                txt_green.setText(g);
                txt_blue.setText(b);
                try
                {
                    command = new StringBuilder("kSetLEDs,s,").append(r).append(",").append(g).append(",").append(b).toString();
                }
                catch (Exception e)
                {
                    txt_debug.setText("Exception Occurred building string: " + e.getMessage());
                    e.printStackTrace();
                }

                try
                {
                    sendCommmand(command);
                }
                catch (Exception e)
                {
                    txt_debug.setText("Exception Occurred sending command: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });*/

        btn_color_white.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String command = "kSetLEDs,s,255,255,255,25";
                sendCommmand(command);
            }});

        btn_color_red.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String command = "kSetLEDs,s,255,0,0,25";
                sendCommmand(command);
            }});

        btn_color_green.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String command = "kSetLEDs,s,0,255,0,25";
                sendCommmand(command);
            }});

        btn_color_blue.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String command = "kSetLEDs,s,0,0,255,25";
                sendCommmand(command);
            }});

        /*btn_changecolor.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(MainActivity.this, Main2Activity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }});*/

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

        setup();
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
                    myThreadConnectBTdevice = new ThreadConnectBTdevice(device);
                    myThreadConnectBTdevice.start();
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

                        listViewPairedDevice.setVisibility(View.GONE);
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
