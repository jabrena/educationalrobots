import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.SensorPort;
import lejos.nxt.addon.CompassSensor;


public class PlatonBody {
	private static TLBDataBridge TLBDB;
	private static TLBListener4 TLBL;
	private static StereoStaticUSEyes SUSEObj;
	private static TLBGUI2 TLBG;
	private static Robot8 robot;
	private static BatteryMonitor bm;
	private static SystemLogger sl;

	public static void main(String[] args) {
		try{
			//Class used to exchange data among Threads
			TLBDB = new TLBDataBridge();
			//Thread used to listen BT Communications from RC Controller
			TLBL = new TLBListener4(TLBDB);
			//Thread used to read compass sensor
			CompassSensor compass = new CompassSensor(SensorPort.S3);
			//Thread used to measure distances
			SUSEObj = new StereoStaticUSEyes(TLBDB);
			
			robot = new Robot8(TLBDB);
			
			//Thread used to display information
			TLBG = new TLBGUI2(TLBDB);
			
			bm = new BatteryMonitor(TLBDB);
			sl = new SystemLogger(TLBDB);
		}catch(Exception e){
			//Empty
		}

		try{
			TLBG.start();
			TLBL.start();
			SUSEObj.start();
			//SUSEObj.setPriority(Thread.MAX_PRIORITY);
			robot.start();
			bm.start();
			sl.start();
		}catch(Exception e){
			//Empty
		}

		while(!Button.ESCAPE.isPressed()){

		}

		sl.close();
		//Send command to Android
		TLBDB.setCMD(9999);
		
		LCD.clear();
		LCD.drawString("Program finished", 0, 7);
		System.exit(0);
	}
}
