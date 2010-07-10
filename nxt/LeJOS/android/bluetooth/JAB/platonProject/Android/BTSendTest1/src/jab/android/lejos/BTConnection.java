package jab.android.lejos;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTConnector;
import android.util.Log;
//import android.widget.TextView;

public class BTConnection extends Thread {
	
	public static enum CONN_TYPE {
		LEJOS, LEGO
	}
	
	private final String BT_SEND = "BTSend";
	private final String YOUR_TURN = "YourTurn";
	private final String CONNECTING = "Connecting...";

	private DataExchange de;
	
	//Propiedades de la configuracion
	private int distanceThreshold = 20;
	private static int minimumObstacleDistance = 40;
	
	public BTConnection(DataExchange _de){
		de = _de;
	}
	
	public static NXTConnector connect(final String source,
			final CONN_TYPE connection_type) {

		NXTConnector conn;
		conn = new NXTConnector();

		conn.addLogListener(new NXTCommLogListener() {

			public void logEvent(String arg0) {
				Log.e(source + " NXJ log:", arg0);
			}

			public void logEvent(Throwable arg0) {
				Log.e(source + " NXJ log:", arg0.getMessage(), arg0);

			}

		});

		conn.setDebug(true);

		switch (connection_type) {
		case LEGO:
			Log.e(source, " about to attempt LEGO connection ");
			conn.connectTo("btspp://", NXTComm.LCP);
			break;
		case LEJOS:
			Log.e(source, " about to attempt LEJOS connection ");
			conn.connectTo("btspp://");
			break;
		}

		return conn;
	}	
	
	public int getMinimumDistance(int leftDistance,int rightDistance){
		int minimumDistance = 0;
		
		if(leftDistance > rightDistance){
			minimumDistance = rightDistance;
		}else{
			minimumDistance = leftDistance;
		}
		
		return minimumDistance;
	}
	
	@Override
	public void run() {
		
		de.setBTStatus(de.BT_STATUS_CONNECTING);
		
		// we are going to talk to the LeJOS firmware so use LEJOS
		NXTConnector conn = connect(BT_SEND, CONN_TYPE.LEJOS);
		
		de.setBTStatus(de.BT_STATUS_CONNECTED);
		Log.e(BTSend.BT_SEND, "BTStatus:" + de.getBTStatus());		
		
		DataOutputStream dos = conn.getDataOut();
		DataInputStream dis = conn.getDataIn();
		
		int inputData = 0;
		int outputData = 0;
		int commandData = 0;
		
		//for (int i = 0; i < 100; i++) {
		while(de.getCommandData() != 9999){
			try {
				outputData = de.getCommandData();//(i * 30000);
				
				//Girar
				if(de.getAutonomousMode()){
					boolean rule2 = false;
					if(getMinimumDistance(de.getLeftUS(),de.getRightUS()) > minimumObstacleDistance){
						rule2 = true;
					}
					
					if(rule2){
						outputData = 2;
					}else{
						outputData = 20;
					}
				}
				
				dos.writeInt(outputData);
				dos.flush();
				
				Log.e(BT_SEND, "Sending " + outputData);
				de.setOutputData(outputData);
				
			} catch (IOException ioe) {
				Log.e(BT_SEND, "IO Exception writing bytes:");
				Log.e(BT_SEND, ioe.getMessage());
				break;
			}

			try {
				inputData = dis.readInt();

				Log.e(BT_SEND, "Received " + inputData);
				de.setInputData(inputData);
			} catch (IOException ioe) {
				Log.e(BT_SEND, "IO Exception reading bytes:");
				Log.e(BT_SEND, ioe.getMessage());
				break;
			}
		}

		try {
			try {
				dis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				dos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				conn.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} finally {
			dis = null;
			dos = null;
			conn = null;
		}
		
		de.setBTStatus(de.BT_STATUS_INACTIVE);
		de.setInputData(0);
		de.setOutputData(0);
		de.setCommandData(0);
		Log.e(BTSend.BT_SEND, "BTStatus:" + de.getBTStatus());
	}
}
