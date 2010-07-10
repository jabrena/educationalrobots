package jab.android.lejos;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BTSend extends Activity implements Runnable{
	
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
	
	static final String BT_SEND = "BTSend";
	
	static String btStatus = "";
	static String inputData = "";
	static String outputData = "";
	
	static String leftUS = "";
	static String rightUS = "";
	
	static DataExchange de;
	private TextView btstatus;
	private TextView input;
	private TextView output;
	private TextView tvLeftUS;
	private TextView tvRightUS;	
	
	boolean autonomousMode = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main2);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        //Configurate User Interface;
        configurateUI();
        
        boolean btRequirements = detectBluetooth();
        
        if(btRequirements){
			Button btConnect;
			btConnect = (Button) findViewById(R.id.connect);
			btConnect.setOnClickListener(new View.OnClickListener() {
	
				public void onClick(View arg0) {
					try {
						connect();
					} catch (Exception e) {
						Log.e(BT_SEND, "Failed to run:" + e.getMessage());
					}
	
				}
			});
			
			Button btDisconnect;
			btDisconnect = (Button) findViewById(R.id.disconnect);
			btDisconnect.setOnClickListener(new View.OnClickListener() {
	
				public void onClick(View arg0) {
					try {
						disconnect();
					} catch (Exception e) {
						Log.e(BT_SEND, "Failed to run:" + e.getMessage());
					}
	
				}
			});
			
			de = new DataExchange();
			
			Button btAuto;
			btAuto = (Button) findViewById(R.id.autonomous);
			btAuto.setOnClickListener(new View.OnClickListener() {
	
				public void onClick(View arg0) {
					try {
						autonomousMode = true;
						de.setAutonomousMode(autonomousMode);
					} catch (Exception e) {
						Log.e(BT_SEND, "Failed to run:" + e.getMessage());
					}
	
				}
			});
			

			
			btstatus = (TextView) findViewById(R.id.btstatus);
			input = (TextView) findViewById(R.id.input);
			output = (TextView) findViewById(R.id.output);

			tvLeftUS = (TextView) findViewById(R.id.leftus);
			tvRightUS = (TextView) findViewById(R.id.rightus);
			
			Thread thread = new Thread(this);
			thread.start();
        }
    }
    
    private void configurateUI(){
    	
    	//Button 1
		Button btOne;
		btOne = (Button) findViewById(R.id.one);
		btOne.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				try {
					de.setCommandData(1);
				} catch (Exception e) {
					Log.e(BT_SEND, "Failed to run:" + e.getMessage());
				}

			}
		});
		
    	//Button 2
		Button btTwo;
		btTwo = (Button) findViewById(R.id.two);
		btTwo.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				try {
					de.setCommandData(2);
				} catch (Exception e) {
					Log.e(BT_SEND, "Failed to run:" + e.getMessage());
				}

			}
		});
		
    	//Button 3
		Button btThree;
		btThree = (Button) findViewById(R.id.three);
		btThree.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				try {
					de.setCommandData(3);
				} catch (Exception e) {
					Log.e(BT_SEND, "Failed to run:" + e.getMessage());
				}

			}
		});
		
    	//Button 4
		Button btFour;
		btFour = (Button) findViewById(R.id.four);
		btFour.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				try {
					de.setCommandData(4);
				} catch (Exception e) {
					Log.e(BT_SEND, "Failed to run:" + e.getMessage());
				}

			}
		});
		
    	//Button 5
		Button btFive;
		btFive = (Button) findViewById(R.id.five);
		btFive.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				try {
					de.setCommandData(5);
				} catch (Exception e) {
					Log.e(BT_SEND, "Failed to run:" + e.getMessage());
				}

			}
		});	
		
    	//Button 6
		Button btSix;
		btSix = (Button) findViewById(R.id.six);
		btSix.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				try {
					de.setCommandData(6);
				} catch (Exception e) {
					Log.e(BT_SEND, "Failed to run:" + e.getMessage());
				}

			}
		});		
		
    	//Button 7
		Button btSeven;
		btSeven = (Button) findViewById(R.id.seven);
		btSeven.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				try {
					de.setCommandData(7);
				} catch (Exception e) {
					Log.e(BT_SEND, "Failed to run:" + e.getMessage());
				}

			}
		});	
		
    	//Button 8
		Button btEight;
		btEight = (Button) findViewById(R.id.eight);
		btEight.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				try {
					de.setCommandData(8);
				} catch (Exception e) {
					Log.e(BT_SEND, "Failed to run:" + e.getMessage());
				}

			}
		});	
		
    	//Button 9
		Button btNine;
		btNine = (Button) findViewById(R.id.nine);
		btNine.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				try {
					de.setCommandData(9);
				} catch (Exception e) {
					Log.e(BT_SEND, "Failed to run:" + e.getMessage());
				}

			}
		});	
    }
    
    private boolean detectBluetooth(){
    	
    	boolean btRequirements = false;
    	
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            //return;
            btRequirements = false;
        }else{
            // If BT is not on, request that it be enabled.
            // setupChat() will then be called during onActivityResult
            if (!mBluetoothAdapter.isEnabled()) {
                Toast.makeText(this, "Bluetooth is not enabled", Toast.LENGTH_LONG).show();
                finish();
                //return;
                btRequirements = false;
            // BT Enabled
            } else {
            	//Continue
            	btRequirements = true;
            }
        }
        
        return btRequirements;
    }
    
	public void run() {
		while(true){
			btStatus = de.getFMBTStatus();
			inputData = "" + de.getInputData();
			outputData = "" + de.getOutputData();
			leftUS = "" + de.getLeftUS();
			rightUS = "" + de.getRightUS();
			
			handler.sendEmptyMessage(0);
		}
	}
    
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			btstatus.setText("Status: " + btStatus);
			input.setText("Input: " + inputData);
			output.setText("Output: " + outputData);
			tvLeftUS.setText("Left: " + leftUS);
			tvRightUS.setText("Right: " + rightUS);			
		}
	};
	
	protected void connect() throws Exception {		
		
		BTConnection btConn = new BTConnection(de);
		btConn.start();
		
	}
	
	private void disconnect(){
		de.setCommandData(9999);
	}
	
}
