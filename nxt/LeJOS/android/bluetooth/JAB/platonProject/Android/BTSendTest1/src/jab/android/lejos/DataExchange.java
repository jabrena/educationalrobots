package jab.android.lejos;

import lejos.pc.comm.NXTComm;
import android.util.Log;

public class DataExchange{
	
	private int BT_STATUS = 0;
	private int INPUT_DATA  = 0;
	private int OUTPUT_DATA  = 0;
	private int COMMAND_DATA = 0;
	
	public final int BT_STATUS_INACTIVE = 0;
	public final int BT_STATUS_CONNECTING = 1;
	public final int BT_STATUS_CONNECTED = 2;
	
	private boolean AutonomousMode = false;
	
	private int leftUS = 0;
	private int rightUS = 0;
	
	public DataExchange(){
		
	}
	
	public synchronized void setBTStatus(int btStatus){
		BT_STATUS = btStatus;
	}
	
	public synchronized int  getBTStatus(){
		return BT_STATUS;
	}
	
	public String getFMBTStatus(){
		
		String friendlyMessage = "";
		
		switch (BT_STATUS) {
		case 0:
			friendlyMessage  = "Inactive";
			break;
		case 1:
			friendlyMessage  = "Connecting";
			break;
		case 2:
			friendlyMessage  = "Connected";
			break;
		}
			
		return friendlyMessage;
	}
	
	public synchronized void setInputData(int idata){
		INPUT_DATA = idata;
		
		//Determinate the value
		if(INPUT_DATA < 2000){
			int sensorData = INPUT_DATA - 1000;
			this.setLeftUS(sensorData);
		}else if(INPUT_DATA < 3000){
			int sensorData = INPUT_DATA - 2000;
			this.setRightUS(sensorData);
		}
	}
	
	public synchronized int  getInputData(){
		return INPUT_DATA;
	}
	
	public synchronized void setOutputData(int odata){
		OUTPUT_DATA = odata;
	}
	
	public synchronized int  getOutputData(){
		return OUTPUT_DATA;
	}
	
	/**
	 * Command Area
	 */
	
	public synchronized void setCommandData(int cmdData){
		COMMAND_DATA = cmdData;
	}
	
	public synchronized int  getCommandData(){
		return COMMAND_DATA;
	}
	
	/**
	 * Autonomous Mode
	 */
	
	public synchronized void setAutonomousMode(boolean mode){
		AutonomousMode = mode;
	}
	
	public synchronized boolean  getAutonomousMode(){
		return AutonomousMode;
	}
	
	/**
	 * Ultrasonic Sensors
	 */
	
	public synchronized void setLeftUS(int value){
		leftUS = value;
	}
	
	public synchronized int  getLeftUS(){
		return leftUS;
	}
	
	public synchronized void setRightUS(int value){
		rightUS = value;
	}
	
	public synchronized int  getRightUS(){
		return rightUS;
	}
}
