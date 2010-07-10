import javax.microedition.location.*;
import java.util.Date;
/**
 * This class has been designed to exchange data between the threads.
 * 
 * @author Juan Antonio Brenha Moral
 *
 */
public class TLBDataBridge {

	//Bluetooth Command
	private int CMD = 0;

	//Bluetooth Thread
	private String BT_MSG = "";
	private boolean CONN_STATUS = false;
	private int BT_SIGNAL_LEVEL = 0;
	
	private boolean AUTONOMOUS_MODE = false;
	
	private boolean EXECUTING_ACTION = false;
	
	//Data to show in GUI
	private int speed = 0;
	private int steering = 0;
	
	
	//Compass Sensor
	private int compassDegrees = 0;
	
	//Eyes Thread
	private boolean EyesEnabled = false;
	private int leftDistance = 0;
	private int rightDistance = 0;
	
	//System Thread
	private int battery = 0;
	private int memory = 0;
	
	/**
	 * Constructor
	 */
	public TLBDataBridge(){
		
	}

	/**
	 * Methods used between NXT - RC-NXT
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
	
	public void setExecutingAction(boolean flag){
		EXECUTING_ACTION = flag;
	}
	
	public boolean getExecutingAction(){
		return EXECUTING_ACTION;
	}
	
		
	
	/*
	 * Methods for TLBGUI2
	 */
	public void setSpeed(int _speed){
		speed = _speed;
	}
	
	public int getSpeed(){
		return speed;		
	}
	
	public void setSteering(int _steering){
		steering = _steering;
	}
	
	public int getSteering(){
		return steering;
	}

	/* Eyes Thread Methods */
	public boolean getEyesEnabled(){
		return EyesEnabled;
	}
	
	public void setEyesEnabled(boolean status){
		EyesEnabled = status;
	}

	public int getLeftEyedistance(){
		return leftDistance;
	}
	
	public void setLeftEyedistance(int distance){
		leftDistance = distance;
	}

	public int getRightEyedistance(){
		return rightDistance;
	}
	
	public void setRightEyedistance(int distance){
		rightDistance = distance;
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
