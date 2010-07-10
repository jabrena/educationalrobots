import lejos.nxt.*;

/**
 * 
 * @author Sog Yang
 * @author Juan Antonio Brenha Moral
 *
 */
public class SSL extends I2CSensor{
	private static final byte SSL_ADDRESS = (byte)0x30 >> 1;
    private byte[] outBuf3DV = new byte[22];
    private int pitch = 0;
    private int roll = 0;
    private int gyro = 0;
    private int yaw =0;
	private int I2C_Response = 0;
	
	public SSL(SensorPort port){
		super(port);		
		this.setAddress(SSL_ADDRESS);
	}	
	

	public void get3DVectors(){

		int temp = 0;
		int mx_raw = 0;
		int my_raw = 0;
		int mz_raw = 0;
		int ax_raw = 0;
		int ay_raw = 0;
		int az_raw = 0;
		//Read All Data	
		I2C_Response = this.getData(0x00, outBuf3DV, 22);
		
		pitch = outBuf3DV[1];			//Parse High Byte				      
		pitch=(pitch<<8)|outBuf3DV[0]; 	//Parse Low Byte and merge with High Byte
		
		roll=outBuf3DV[3];			//Parse High Byte
		roll=(roll<<8)|outBuf3DV[2]; 	//Parse Low Byte and merge with High Byte

		yaw=outBuf3DV[5];			//Parse High Byte
		yaw=(yaw<<8)|outBuf3DV[4]; 	//Parse Low Byte and merge with High Byte
  
		temp=outBuf3DV[7];			//Parse High Byte
		temp=(temp<<8)|outBuf3DV[6]; 	//Parse Low Byte and merge with High Byte
      
		gyro=outBuf3DV[9];			//Parse High Byte
		gyro=(gyro<<8)|outBuf3DV[8]; 	//Parse Low Byte and merge with High Byte

		mx_raw=outBuf3DV[11];			//Parse High Byte
		mx_raw=(mx_raw<<8)|outBuf3DV[10]; 	//Parse Low Byte and merge with High Byte      

		my_raw=outBuf3DV[13];			//Parse High Byte
		my_raw=(my_raw<<8)|outBuf3DV[12]; 	//Parse Low Byte and merge with High Byte      

		mz_raw=outBuf3DV[15];			//Parse High Byte
		mz_raw=(mz_raw<<8)|outBuf3DV[14]; 	//Parse Low Byte and merge with High Byte      

		ax_raw=outBuf3DV[17];			//Parse High Byte
		ax_raw=(ax_raw<<8)|outBuf3DV[16]; 	//Parse Low Byte and merge with High Byte      

		ay_raw=outBuf3DV[19];			//Parse High Byte
		ay_raw=(ax_raw<<8)|outBuf3DV[18]; 	//Parse Low Byte and merge with High Byte      

		az_raw=outBuf3DV[21];			//Parse High Byte
		az_raw=(az_raw<<8)|outBuf3DV[20]; 	//Parse Low Byte and merge with High Byte      
      
	}	
	
  public void  setFilterON(){

	  I2C_Response = this.sendData(0x30, (byte)0xf0);
  }
  
  public void  setFilterOFF(){

	  I2C_Response = this.sendData(0x30, (byte)0xf1);
  }
	
  public int getPitch(){
	  return pitch;
  }
  
  public int getRoll(){
	  return roll;
  }
  
  public int getGyro(){
	  return gyro;
  }

  public int getYaw(){
	  return yaw;
  }
}
