package com.blackboxembedded.WunderLINQ;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.nio.charset.StandardCharsets;

public class DebugActivity extends AppCompatActivity {

    private final static String TAG = "FWConfigActvity";

    BluetoothGattCharacteristic characteristic;

    private ActionBar actionBar;
    private ImageButton backButton;
    private TextView navbarTitle;
    private TextView output;
    private Spinner commandSpinner;
    private EditText customCommand;
    private Button writeBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        output = (TextView) findViewById(R.id.tvOutput);
        commandSpinner = (Spinner) findViewById(R.id.debug_commands_spinner);
        commandSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                Log.d(TAG,"Item Selected: " + adapterView.getItemAtPosition(pos).toString());
                if (pos == 13){
                    customCommand.setVisibility(View.VISIBLE);
                } else {
                    customCommand.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        customCommand = (EditText) findViewById(R.id.etCustomCommand);
        writeBtn = (Button) findViewById(R.id.writeBtn);
        writeBtn.setOnClickListener(mClickListener);

        showActionBar();

        characteristic = MainActivity.gattCommandCharacteristic;
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "In onResume");
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG,"In onDestroy");
        super.onDestroy();
        try {
            unregisterReceiver(mGattUpdateReceiver);
        } catch (IllegalArgumentException e) {

        }
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.writeBtn:
                    switch(commandSpinner.getSelectedItemPosition()){
                        case 0: //Read Serial Flash
                            byte[] readSerialFlashCmd = {0x57,0x52,0x53};
                            characteristic.setValue(readSerialFlashCmd);
                            break;
                        case 1: //Read Config Flash
                            byte[] readConfigFlashCmd = {0x57,0x52,0x43};
                            characteristic.setValue(readConfigFlashCmd);
                            break;
                        case 2: //Read WLQ Flash
                            byte[] readWLQFlashCmd = {0x57,0x52,0x57};
                            characteristic.setValue(readWLQFlashCmd);
                            break;
                        case 3: //Reboot into Bootloader
                            byte[] rebootBootloaderCmd = {0x57,0x57,0x42,0x42};
                            characteristic.setValue(rebootBootloaderCmd);
                            break;
                        case 4: //Turn off 3V switch
                            byte[] off3VswitchCmd = {0x57,0x57,0x48,0x53,0x30};
                            characteristic.setValue(off3VswitchCmd);
                            break;
                        case 5: //Turn on 3V switch
                            byte[] on3VswitchCmd = {0x57,0x57,0x48,0x53,0x31};
                            characteristic.setValue(on3VswitchCmd);
                            break;
                        case 6: //Toggle 3V switch
                            byte[] toggle3VswitchCmd = {0x57,0x57,0x48,0x53,0x54};
                            characteristic.setValue(toggle3VswitchCmd);
                            break;
                        case 7: //Turn off blue LED
                            byte[] offBlueLEDCmd = {0x57,0x57,0x48,0x4C,0x42,0x31};
                            characteristic.setValue(offBlueLEDCmd);
                            break;
                        case 8: //Turn on blue LED
                            byte[] onBlueLEDCmd = {0x57,0x57,0x48,0x4C,0x42,0x30};
                            characteristic.setValue(onBlueLEDCmd);
                            break;
                        case 9: //Flash blue LED
                            byte[] blinkBlueLEDCmd = {0x57,0x57,0x48,0x4C,0x42,0x42};
                            characteristic.setValue(blinkBlueLEDCmd);
                            break;
                        case 10: //Turn off green LED
                            byte[] offGreenLEDCmd = {0x57,0x57,0x48,0x4C,0x47,0x31};
                            characteristic.setValue(offGreenLEDCmd);
                            break;
                        case 11: //Turn on green LED
                            byte[] onGreenLEDCmd = {0x57,0x57,0x48,0x4C,0x47,0x30};
                            characteristic.setValue(onGreenLEDCmd);
                            break;
                        case 12: //Flash green LED
                            byte[] blinkGreenLEDCmd = {0x57,0x57,0x48,0x4C,0x47,0x42};
                            characteristic.setValue(blinkGreenLEDCmd);
                            break;
                        case 13: // Custom Command
                            String customCommandString = customCommand.getText().toString() + "\\r\\n";
                            char[] customCommandChar = customCommandString.toCharArray();
                            byte[] customCmd = new String(customCommandChar).getBytes(StandardCharsets.UTF_8);
                            characteristic.setValue(customCmd);
                            break;
                        default:
                            break;
                    }
                    BluetoothLeService.writeCharacteristic(characteristic);

                    break;
                case R.id.action_back:
                    // Go back
                    Intent backIntent = new Intent(DebugActivity.this, MainActivity.class);
                    startActivity(backIntent);
                    break;
            }
        }
    };

    private void showActionBar(){
        LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.actionbar_nav, null);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(v);

        navbarTitle = (TextView) findViewById(R.id.action_title);
        navbarTitle.setText(R.string.debug_title);

        backButton = (ImageButton) findViewById(R.id.action_back);
        backButton.setOnClickListener(mClickListener);

        ImageButton forwardButton = (ImageButton) findViewById(R.id.action_forward);
        forwardButton.setVisibility(View.INVISIBLE);
    }

    // Handles various events fired by the Service.
    // ACTION_WRITE_SUCCESS: received when write is successful
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Bundle bd = intent.getExtras();
                if(bd != null){
                    if(bd.getString(BluetoothLeService.EXTRA_BYTE_UUID_VALUE).contains(GattAttributes.WUNDERLINQ_COMMAND_CHARACTERISTIC)){
                        byte [] data = bd.getByteArray(BluetoothLeService.EXTRA_BYTE_VALUE);
                        String characteristicValue = Utils.ByteArraytoHex(data) + " ";
                        Log.d(TAG,"UUID: "+ bd.getString(BluetoothLeService.EXTRA_BYTE_UUID_VALUE) + " DATA: "+ characteristicValue);
                        output.setText(characteristicValue);
                        output.append("\n");
                    }

                }
            } else if(BluetoothLeService.ACTION_WRITE_SUCCESS.equals(action)){
                Log.d(TAG,"Write Success Received");
                BluetoothLeService.readCharacteristic(characteristic);
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_WRITE_SUCCESS);
        return intentFilter;
    }
}