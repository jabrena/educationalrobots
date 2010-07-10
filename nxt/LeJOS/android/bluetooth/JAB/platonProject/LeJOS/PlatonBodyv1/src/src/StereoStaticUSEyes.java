import lejos.nxt.*;

public class StereoStaticUSEyes extends Thread{
	private TLBDataBridge TLBDB;
	
	private StaticUSEye leftEye;
	private StaticUSEye rightEye;   
	private int leftDistance;
	private int rightDistance;
	
	public StereoStaticUSEyes(TLBDataBridge _TLBDB){
		TLBDB = _TLBDB;
		
		UltrasonicSensor leftUS = new UltrasonicSensor(SensorPort.S1);
		UltrasonicSensor rightUS = new UltrasonicSensor(SensorPort.S2);
		leftEye = new StaticUSEye(leftUS,StaticUSEye.LEFTSIDE);
		rightEye = new StaticUSEye(rightUS,StaticUSEye.RIGHTSIDE);
		
	}
	
	public void run(){
		leftEye.start();
		rightEye.start();
		
		//Set Enabled the subsystem
		TLBDB.setEyesEnabled(true);
		
		while(true){

			leftDistance = leftEye.getDistance();
			rightDistance = rightEye.getDistance();
			TLBDB.setLeftEyedistance(leftDistance);
			TLBDB.setRightEyedistance(rightDistance);
			

			//for(int i=0; i< leftDistances.length;i++){
				//LCD.drawInt(angles[i], 0, i);
				LCD.drawString("   ", 4, 5);
				LCD.drawInt(leftDistance, 4, 5);
				LCD.drawString("   ", 10, 5);
				LCD.drawInt(rightDistance, 10, 5);
			//}
			LCD.refresh();
			//Exchange data with other subsystems
		}
	}
}
