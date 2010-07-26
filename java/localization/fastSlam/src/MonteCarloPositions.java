import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import javax.swing.JFrame;

import oursland.RandomSingleton;
import oursland.naming.UniqueNameGenerator;
import vp.mapping.PathPanel;
import vp.model.ControlModel;
import vp.model.SimpleControlModel;
import vp.robot.RobotPath;

public class MonteCarloPositions {

	public static void main(String[] args) {
		UniqueNameGenerator lmGen = new UniqueNameGenerator("lm");
		try {
			RobotPath pathEstimate;
			{	// clean up the old data once we have an initial path
				
				BufferedReader pathFile = new BufferedReader(new FileReader("path6.out"));
				pathEstimate = RobotPath.read(pathFile, lmGen);
				pathFile.close();
				}

			JFrame f = new JFrame("Local Sensor Map");
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.getContentPane().setLayout(new BorderLayout());
		
			ControlModel control = new SimpleControlModel();
			PathPanel panel = new PathPanel(pathEstimate);
			f.getContentPane().add(panel, BorderLayout.CENTER);
		
			f.setSize(600,600);
			f.setVisible(true);

			Random rand = RandomSingleton.instance;
			while(true) {
				try {
					Thread.sleep(5000);
					while( panel.update(rand, control) ) {
						panel.repaint();
						Thread.sleep(1);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				panel.updateEstimate();
				System.out.println("Restarting path estimation.");
				PrintWriter out = new PrintWriter(new FileOutputStream("path.out"));
				panel.getPathEstimate().write(out);
				out.close();
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
