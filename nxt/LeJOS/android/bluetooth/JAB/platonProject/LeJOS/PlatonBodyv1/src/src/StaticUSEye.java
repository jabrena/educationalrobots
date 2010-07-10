import lejos.nxt.*;

public class StaticUSEye extends Thread{
	private UltrasonicSensor US;
	private boolean eyeSide = false;

	//private int oldDistances[] = {0,0,0,0};
	private float[] distances = {0,0,0,0};
	
	static final boolean LEFTSIDE = true;
	static final boolean RIGHTSIDE = false;
	
	private int delay = 250;
	
	public StaticUSEye(UltrasonicSensor usObj,boolean side){
		US = usObj;
		eyeSide = side;
	}
	
	public void run(){
		//It is a infinite task
		while(true){
			SICKMode();
		}
	}
	
	public void SICKMode(){
		distances[0] = US.getDistance();
		//sleep(delay);
		/*
		distances[1] = US.getDistance();
		sleep(delay);
		distances[2] = US.getDistance();
		sleep(delay);
		distances[3] = US.getDistance();
		sleep(delay);
		*/
	}
	
	public int getDistance(int index){
		float distance = 0;
		if((index >= 0) && (index <= distances.length)){
			distance = distances[index];
		}else{
			distance = -1;
		}
		return Math.round(distance);
	}
	
	public int getDistance(){
		return (int)distances[0];//Math.round(Statistics.mean(distances));
	}
	
	public void sleep(int miliseconds){
		try {Thread.sleep(miliseconds);} catch (Exception e) {}
	}
}
