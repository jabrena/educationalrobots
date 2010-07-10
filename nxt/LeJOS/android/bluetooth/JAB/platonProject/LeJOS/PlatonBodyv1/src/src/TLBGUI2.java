import javax.microedition.location.*;
import java.util.*;
import lejos.nxt.*;

public class TLBGUI2 extends Thread{
	private TLBDataBridge TLBDB;
	
	//Protocol
	private int CMD;

	
	private final String appName = "RC Car";
	private final String version = "v1.1";
	
	public TLBGUI2(TLBDataBridge TLBDB){
		this.TLBDB = TLBDB;
	}
	
	public void run(){
		LCD.drawString(appName + " " + version, 0, 0);
		LCD.refresh();

		//Circular System
		int screens = 4;
		int currentScreen = 1;
		
		while (true){

			//Circular System
			if (Button.LEFT.isPressed()){
				if(currentScreen == 1){
					currentScreen = screens;
				}else{
					currentScreen--;
				}
			}

			if (Button.RIGHT.isPressed()){
				if(currentScreen == screens){
					currentScreen = 1;
				}else{
					currentScreen++;
				}
			}

			//Reset
			if (Button.ENTER.isPressed()){
				currentScreen  =1;
			}

			try{
				if(currentScreen == 1){
					showSubsystemsStatus();
				}else if(currentScreen == 2){
					showTLBData();
				}
			}catch(Exception e){
				
			}
			try {Thread.sleep(500);} catch (Exception e) {}
		}
	}

	private void showSubsystemsStatus(){
		refreshSomeLCDLines();
		LCD.drawString("Status",0,2);
		LCD.drawString("BT: " + TLBDB.getBTMSG(),0,3);
		LCD.refresh();
	}
	
	private void showTLBData(){
		try{

			CMD = TLBDB.getCMD();
		}catch(Exception e){
			
		}

		//Process CMD
		/*
		if(CMD != 0){
			if(CMD < 1000){
				SPEED = CMD;
			}else{
				STEERING = CMD;
			}
		}*/

		refreshSomeLCDLines();
		LCD.drawString("CMD:",0,2);
		LCD.drawString("    ",10,2);
		LCD.drawInt(CMD,10,2);
		LCD.drawString("Is Possible:",0,3);
		LCD.drawString("    ",10,3);
		LCD.drawString(""+ TLBDB.getExecutingAction(),10,3);
		LCD.refresh();
	}
	
	/**
	 * Clear some LCD lines
	 */
	private static void refreshSomeLCDLines(){
		LCD.drawString("                     ", 0, 2);
		LCD.drawString("                     ", 0, 3);
		LCD.drawString("                     ", 0, 4);
		LCD.drawString("                     ", 0, 5);
		LCD.drawString("                     ", 0, 6);
		LCD.drawString("                     ", 0, 7);
	}
}
