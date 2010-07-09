import lejos.nxt.*;

public class SSLTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SSL SSLObj = new SSL(SensorPort.S1);
		//SSLObj.setFilterON();
		
		while(!Button.ESCAPE.isPressed()){
			SSLObj.get3DVectors();
			LCD.drawString("SSL Test", 0, 0);
			LCD.drawString("Pitch:" + SSLObj.getPitch(), 0,2);
			LCD.drawString("Roll:" + SSLObj.getRoll(), 0,3);
			LCD.drawString("Gyro:" + SSLObj.getGyro(), 0,4);
			LCD.drawString("Yaw:" + SSLObj.getYaw(), 0,5);
		}
	}

}
