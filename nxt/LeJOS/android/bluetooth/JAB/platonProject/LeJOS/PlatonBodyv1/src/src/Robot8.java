import lejos.nxt.*;
import lejos.nxt.addon.*;
import lejos.robotics.navigation.*;

/**
 * 
 * @author Juan Antonio Brenha Moral
 *
 */
public class Robot8 extends Thread{

	private TLBDataBridge TLBDB;
	
	/**
	 * Area de propiedades del robot
	 */
	
	//Actuadores
	private Motor leftMotor;
	private Motor rightMotor;
	private CompassPilot pilot;
	
	//Sensores
	private CompassSensor compass;
	private UltrasonicSensor us1;
	private UltrasonicSensor us2;
	
	//Propiedades de la configuracion
	private int distanceThreshold = 20;
	private static int minimumObstacleDistance = 40;
	
	/**
	 * Constructor
	 */
	public Robot8(TLBDataBridge _TLBDB){
		TLBDB = _TLBDB;
		
		leftMotor = Motor.B;
		rightMotor = Motor.A;
		//float wheelDiameter, float trackWidth
		compass = new CompassSensor(SensorPort.S1);	
		
		pilot = new CompassPilot(compass, 2.25f, 4.8f, leftMotor, rightMotor);
		pilot.getRight().setSpeed(900);
		pilot.getLeft().setSpeed(900);
		
		us1 = new UltrasonicSensor(SensorPort.S2);
		us2 = new UltrasonicSensor(SensorPort.S3);		
	}
	
	/**
	 * 
	 * @return
	 */
	public int getLeftDistance(){
		return us1.getDistance();
	}
	
	/**
	 * 
	 * @return
	 */
	public int getRightDistance(){
		return us2.getDistance();
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean hasSimilarDistances(int _leftDistance,int _rightDistance){
		
		int leftDistance = _leftDistance;
		int rightDistance = _rightDistance;
		
		boolean isSimilar = false;
		
		int absoluteDifference = Math.abs(leftDistance - rightDistance);
		
		if(absoluteDifference >= distanceThreshold){
			isSimilar = false;
		}else{
			isSimilar = true;
		}
		
		return isSimilar;
	}
	
	/**
	 * 
	 * @param leftDistance
	 * @param rightDistance
	 * @return
	 */
	public static int getMinimumDistance(int leftDistance,int rightDistance){
		int minimumDistance = 0;
		
		if(leftDistance > rightDistance){
			minimumDistance = rightDistance;
		}else{
			minimumDistance = leftDistance;
		}
		
		return minimumDistance;
	}
	
	/**
	 * 
	 */
	public void calibrate(){
		pilot.calibrate();
	}
	
	/**
	 * 
	 * @param distance
	 */
	public void travel(float distance){
		pilot.travel(distance,true);
	}

	public void forward(int miliseconds){
		pilot.forward();
		this.pause(miliseconds);
		pilot.stop();
	}
	
	/**
	 * 
	 */
	public void stop(){
		pilot.stop();
	}

	/**
	 * 
	 * @param angle
	 */
	public void rotate(int angle){
		pilot.rotate(angle);
	}

	public CompassPilot getPilot(){
		return pilot;
	}
	
	public CompassSensor getCompass(){
		return compass;
	}
	
    public void turnLeft(int miliseconds){
        leftMotor.stop();
        rightMotor.forward();
        try {Thread.sleep(miliseconds);} catch (Exception e) {}
    }
 
    public void turnRight(int miliseconds){
        leftMotor.forward();
        rightMotor.stop();
        try {Thread.sleep(miliseconds);} catch (Exception e) {}
    }  
	
    public void backward(){
        pilot.backward();
    }
    
	/**
	 * @param args
	 */
	/*
	public static void main(String[] args) {
		//Instancia de la clase Robot7
		Robot8 robot = new Robot8();
		
		//Inicio
		robot.calibrate();
		robot.getCompass().resetCartesianZero();
		robot.getPilot().setHeading(0);

		int leftDistance = 0;
		int rightDistance = 0;
			
		//Bucle de control
		while(!Button.ESCAPE.isPressed()){
			leftDistance = robot.getLeftDistance();
			rightDistance = robot.getRightDistance();
			
			LCD.clear();
			LCD.drawString("Left:" + leftDistance, 0, 0);
			LCD.drawString("Right:" + rightDistance, 0, 1);
			LCD.drawString("Similar" + robot.hasSimilarDistances(leftDistance,rightDistance), 0,2);
			
			
			//Si la distancia es igual
			boolean rule1 = robot.hasSimilarDistances(leftDistance,rightDistance);
			boolean rule2 = false;
			if(getMinimumDistance(leftDistance,rightDistance) > minimumObstacleDistance){
				rule2 = true;
			}
			
			if(rule2){
				robot.forward();
			}else{
				Sound.beepSequence();
				if(leftDistance > rightDistance){
					robot.stop();
					//robot.travel(-5);
					robot.rotateTo(45);
				}else{
					robot.stop();
					//robot.travel(-5);					
					robot.rotateTo(-90);
				}
			}
		}
		*/
	
	public void run(){
		pilot.calibrate();
		pilot.getCompass().resetCartesianZero();
		//pilot.getPilot().setHeading(0);
		
		while(true){
			int command = TLBDB.getCMD();
			
			if(!TLBDB.getExecutingAction()){
				
				switch(command){
					case 1:
						break;
					case 2:
						TLBDB.setExecutingAction(true);
						this.forward(1000);
						TLBDB.setExecutingAction(false);
						break;
					case 3:
						break;
					case 4:
						TLBDB.setExecutingAction(true);
						this.turnLeft(90);
						TLBDB.setExecutingAction(false);
						break;
					case 5:
						this.stop();
						break;
					case 6:
						TLBDB.setExecutingAction(true);
						this.turnRight(90);
						TLBDB.setExecutingAction(false);
						break;
					case 7:
					case 8:
						this.backward();
						break;
					case 9:
						break;
					case 20:
						TLBDB.setExecutingAction(true);
						this.stop();
						this.backward();
						this.pause(1000);
						this.stop();
						this.rotate(45);
						TLBDB.setExecutingAction(false);
						break;
					default:
						break;
				}
			}
		}
	}

	private void pause(int miliseconds){
		try {Thread.sleep(miliseconds);} catch (Exception e) {}
	}
}
