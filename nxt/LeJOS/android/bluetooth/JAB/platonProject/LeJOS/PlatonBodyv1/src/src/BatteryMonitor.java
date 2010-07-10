import java.io.File;

import lejos.nxt.*;

public class BatteryMonitor extends Thread {
	private TLBDataBridge TLBDB;
	
	private int battery = 0;
	private int memory = 0;
	
	public BatteryMonitor(TLBDataBridge _TLBDB){
		TLBDB = _TLBDB;
	}
	
	public void run(){
		while(true){
			
			battery = Battery.getVoltageMilliVolt();
			TLBDB.setBattery(battery);
			memory = File.freeMemory();
			TLBDB.setMemory(memory);
		}		
	}
}
