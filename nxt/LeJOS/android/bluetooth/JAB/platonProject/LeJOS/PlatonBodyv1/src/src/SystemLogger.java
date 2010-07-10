import lejos.nxt.Sound;
import lejos.util.Stopwatch;

public class SystemLogger extends Thread{
	//Exchange Data Object
	private TLBDataBridge TLBDB;
	private Stopwatch sw;

	private final String CRLF = "\r\n";

	private FileManager3 fm;
	private String text  = "";

	int oneSecond = 1000;
	int seconds = 60;
	int delay = seconds * oneSecond;//Every 60 secons (1 Minute), the system store a waypoint;

	int i = 0;
	
	StringBuffer sb;
	
	public SystemLogger(TLBDataBridge _TLBDB){
		TLBDB = _TLBDB;
		
		sw = new Stopwatch();
		sw.reset();


		String fileName ="platon.xml";
		fm = new FileManager3(fileName);
		fm.delete();
	}
	
	public void run(){
		
		text = getLogHeader();
		updateFile(text);

		while(true){

			int counter = sw.elapsed();
			
			if(counter >= delay){
					Sound.beep();
					i++;
					
					text = getRegister(i);
					updateFile(text);
					
					sw.reset();
			}

		}
	}
	
	private String getLogHeader(){
		sb = new StringBuffer();
		sb.append("<platon>" + CRLF);
		sb.append("	<configuration>" + CRLF);
		sb.append("		<delay>");
		sb.append("" + delay );
		sb.append("</delay>" + CRLF);
		sb.append("	</configuration>" + CRLF);
		sb.append("	<log>" + CRLF);
		
		return sb.toString();
	}
	
	private String getRegister(int counter){
		sb = new StringBuffer();
		sb.append("		<register>"+ CRLF);
		sb.append("			<id>");
		sb.append("" + counter);
		sb.append("</id>" + CRLF);		
		sb.append("			<battery>");
		sb.append("" + TLBDB.getBattery());
		sb.append("</battery>" + CRLF);
		sb.append("			<memory>");
		sb.append("" + TLBDB.getMemory());
		sb.append("</memory>" + CRLF);
		sb.append("			<bluetooth>");
		sb.append("" + TLBDB.getBTStatus());
		sb.append("</bluetooth>" + CRLF);
		sb.append("		</register>" + CRLF);
		
		return sb.toString();
	}
	
	private String getLogFooter(){
		sb = new StringBuffer();
		sb.append("	</log>" + CRLF);
		sb.append("</platon>" + CRLF);
		
		return sb.toString();
	}	
	
	private void updateFile(String text){
		fm.open();
		fm.add(text);
		fm.close();
	}
	
	public void close(){
		text = getLogFooter();
		updateFile(text);
	}
}
