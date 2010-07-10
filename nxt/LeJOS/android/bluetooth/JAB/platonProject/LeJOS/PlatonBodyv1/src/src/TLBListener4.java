import lejos.nxt.*;
import lejos.nxt.comm.*;
import java.io.*;

/**
 * TLB Listener is a Thread designed to receive commands by bluetooth.
 * 
 * @author Juan Antonio Brenha Moral
 *
 */
public class TLBListener4 extends Thread{

	//Bluetooth
	private BTConnection btc;

	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;

	//Bluetooth Messages
	private String MESSAGE_AWAITING = "Awaiting...";
	private String MESSAGE_CONNECTED = "Connected.";
	private String MESSAGE_CLOSING = "Closing...";

	//Message received by a Bluetooth communication
	private int CMD = 0;
	
	//BT Strength signal
	private int BTSIGNALLEVEL = 0;

	//Object designed to exchange data between Threads
	private TLBDataBridge DB;
	
	public TLBListener4(TLBDataBridge TLBDB) throws Exception{
		this.DB = TLBDB;
		DB.setCMD(CMD);
	}

	private void reset(){
		//LCD.clear();
		CMD = 0;
		BTSIGNALLEVEL = 0;
		DB.reset();
	}
	
	public void run(){
		while(true){
			//Reset
			this.reset();

			DB.setBTMSG(MESSAGE_AWAITING);
			DB.setBTStatus(false);

			BTConnection btc = Bluetooth.waitForConnection();
			Sound.beep();

			DB.setBTMSG(MESSAGE_CONNECTED);
			DB.setBTStatus(true);

			DataInputStream dis = btc.openDataInputStream();
			DataOutputStream dos = btc.openDataOutputStream();

			int ec = 0;
			int internalData = 0;
			int us1 = 1000;
			int us2 = 2000;
			//int battery = 3000;
			
			try{
				while(true){
					//Receive the command
					try{
						CMD = dis.readInt();
					}catch(IOException ioe){
						DB.setCMD(9999);
						break;
					}
					
					//Send ACK
					try{
						
						//Envio de datos a Android.
						ec++;
						
						if(ec == 1){
							internalData = us1 + DB.getLeftEyedistance();
						}else if(ec == 2){
							internalData = us2 + DB.getRightEyedistance();

							//Reset
							ec = 0;
						}
						
						//CMD = internalData;
						
						dos.writeInt(internalData);
						dos.flush();
					}catch(IOException ioe){
						DB.setCMD(9999);
						break;
					}

					//BTSIGNALLEVEL = btc.getSignalStrength();
					//Exchange data
					DB.setCMD(CMD);
				}
			}catch(Exception e){
				//DB.setCMD(9999);
			}

			//Close connection
			try{
				dis.close();
				dos.close();
				Thread.sleep(100); // wait for data to drain
				btc.close();

				DB.setBTMSG(MESSAGE_CLOSING);
				DB.setBTStatus(false);
			}catch(Exception e){
				DB.setBTMSG(MESSAGE_CLOSING);
				DB.setBTStatus(false);
			}
			Sound.beepSequenceUp();
		}
	}
}
