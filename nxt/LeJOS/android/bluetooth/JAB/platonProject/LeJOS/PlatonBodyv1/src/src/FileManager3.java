import java.io.*;
import lejos.nxt.LCD;

public class FileManager3{
	private String fileName;
	private FileOutputStream fos;
	private File f;
	private boolean overwriteFile = false;

	//CONSTRUCTORS
	
	/**
	 * FileManager constructor
	 * @param _fileName
	 */
	public FileManager3(String fileName){
		this.fileName = fileName;
	}

	public FileManager3(){
		//Empty
	}

	public FileManager3(String fileName,boolean overwrite){
		this.fileName = fileName;
		this.overwriteFile = overwrite;
	}
	
	//GETTER AND SETTERS METHODS
	
	public void setFileName(String fileName){
		this.fileName = fileName;
	}
	
	//PUBLIC METHODS
	
	/**
	 * Open a File Connection
	 */
	public void open(){
		try{
			f = new File(fileName);
			if(!f.exists()){
				f.createNewFile();
				fos = new  FileOutputStream(f);
			}else{
				if(overwriteFile){
					fos = new  FileOutputStream(f);
				}else{
					fos = new  FileOutputStream(f,true);
				}
			}
		}catch(IOException e){
			LCD.drawString(e.getMessage(),0,7);
			LCD.refresh();
		}
	}

	/**
	 * Method to
	 * @param text
	 */
	public void add(String text){
		appendToFile(text);
	}

	public void close(){
		try{
			fos.close();
        }catch(IOException e){
            LCD.drawString(e.getMessage(),0,4);
        }
    }

	public void delete(){
		f = new File(fileName);
		if(f.exists()){
			f.delete();
		}
	}
	
	//PRIVATE METHDOS

	/**
	 * This method add data into a file
	 * 
	 * @param text to add
	 */
	private void appendToFile(String text){
		byte[] byteText;
		byteText = getBytes(text);

		try{
			//Critic to add a useless character into file
			//byteText.length-1
			for(int i=0;i<byteText.length-1;i++){
				fos.write((int) byteText[i]);
			}
		}catch(IOException e){
			LCD.drawString(e.getMessage(),0,7);
			LCD.refresh();
		}
	}

	/**
	 * This method convert any String into an Array of bytes
	 * 
	 * @param text to convert
	 * @return An Array of bytes.
	 */ 
	private byte[] getBytes(String inputText){
		//Debug Point
		byte[] nameBytes = new byte[inputText.length()+1];

		for(int i=0;i<inputText.length();i++){
			nameBytes[i] = (byte) inputText.charAt(i);
		}
		nameBytes[inputText.length()] = 0;

		return nameBytes;
	}
}
