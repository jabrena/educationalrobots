import javax.microedition.location.*;
import java.util.Date;
/**
 * This class has been designed to exchange data between the threads.
 * 
 * @author Juan Antonio Brenha Moral
 *
 */
public class DataBridge {

	//Bluetooth Command
	private int CMD = 0;

	//Bluetooth Thread
	private String BT_MSG = "";
	private boolean CONN_STATUS = false;
	private int BT_SIGNAL_LEVEL = 0;
	
	private boolean AUTONOMOUS_MODE = false;

	//Compass Sensor
	private int heading = 0;
	
	//USGrid
	private boolean USGridEnabled = false;
	private int northDistance = 0;
	private int southDistance = 0;
	private int eastDistance = 0;
	private int westDistance = 0;
	
	//System Thread
	private int battery = 0;
	private int memory = 0;
	
	/**
	 * Constructor
	 */
	public DataBridge(){
		
	}

	/**
	 * Methods used between Master-Slave
	 */

	public void setCMD(int CMD){
		this.CMD = CMD;
	}
	
	public int getCMD(){
		return CMD;
	}
	
	public void setBTMSG(String MSG){
		BT_MSG = MSG;
	}
	
	public String getBTMSG(){
		return BT_MSG;
	}
	
	public void setBTStatus(boolean STATUS){
		this.CONN_STATUS = STATUS;
	}
	
	public boolean getBTStatus(){
		return CONN_STATUS;
	}
	
	public void setBTSignalLevel(int BTSIGNALLEVEL){
		BT_SIGNAL_LEVEL = BTSIGNALLEVEL;
	}
	
	public int getBTSignalLevel(){
		return BT_SIGNAL_LEVEL;
	}
	
	public void reset(){
		BT_SIGNAL_LEVEL = 0;
		CMD = 0;
		CONN_STATUS = false;
	}
	
	/*
	 * Autonomous Mode
	 */
	
	public void setAutonomousMode(boolean flag){
		AUTONOMOUS_MODE = flag;
	}
	
	public boolean getAutonomousMode(){
		return AUTONOMOUS_MODE;
	}
		
	

	/* Eyes Thread Methods */
	public boolean getUSGridEnabled(){
		return USGridEnabled;
	}
	
	public void setUSGridEnabled(boolean status){
		USGridEnabled = status;
	}

	public int getNorthDistance(){
		return northDistance;
	}
	
	public void setNorthDistance(int distance){
		northDistance = distance;
	}

	public int getSouthDistance(){
		return southDistance;
	}
	
	public void setSouthDistance(int distance){
		southDistance = distance;
	}
	
	public int getEastDistance(){
		return eastDistance;
	}
	
	public void setEastDistance(int distance){
		eastDistance = distance;
	}
	
	public int getWestDistance(){
		return westDistance;
	}
	
	public void setWestDistance(int distance){
		westDistance = distance;
	}	
	
	/* System */
	
	public void setBattery(int b){
		battery = b;
	}
	
	public int getBattery(){
		return battery;
	}

	public void setMemory(int m){
		memory = m;
	}
	
	public int getMemory(){
		return memory;
	}
}
