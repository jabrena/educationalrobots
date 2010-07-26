import java.awt.BorderLayout;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Random;
import javax.swing.JFrame;
import oursland.RandomSingleton;
import oursland.naming.UniqueNameGenerator;
import vp.fastslam.DistanceSensorErrorModel;
import vp.fastslam.LandmarkMap;
import vp.fastslam.ParticleFilterManager;
import vp.fastslam.SensorErrorModel;
import vp.model.ActionModel;
import vp.model.ControlModel;
import vp.model.SimpleActionModel;
import vp.model.SimpleControlModel;
import vp.robot.RobotPath;
import vp.sim.SensorImageGenerator;
import vp.sim.SensorImageGenerator180;
import vp.ui.ParticleFilterPanel;
import vp.ui.SimulationControlPanel;

public class FastSLAM {
	public static void main(String[] args) {
		UniqueNameGenerator lmGen = new UniqueNameGenerator("lm");
		try {
//			PrintWriter out1 = new PrintWriter(new FileOutputStream("simple-test.out"));
//			final int firstSection = 100;
//			for(int i = 0; i < firstSection; i++ ) {
//				out1.println((i/10.0)+" 0.0 0.0 0.0 3 0 1000 500 500 -500 1200");
//			}
//			for(int i = firstSection; i < 2000; i++ ) {
//				// offset landmark
////				out1.println((i/10.0)+" 0.0 0.0 0.0 3 0 1000 500 500 -510 1210");
//				// moving landmark
////				out1.println((i/10.0)+" 0.0 0.0 0.0 3 0 1000 500 500 " + (-500-1*(i-firstSection)) + " " + (1200+1*(i-firstSection)));
//				// fast moving landmark generates lots of landmarks
//				out1.println((i/10.0)+" 0.0 0.0 0.0 3 0 1000 500 500 " + (-500-4*(i-firstSection)) + " " + (1200+4*(i-firstSection)));
//				// disappearing landmark
////				out1.println((i/10.0)+" 0.0 0.0 0.0 2 0 1000 500 500");				
//			}
//			out1.close();
			
			Rectangle2D mapBounds = new Rectangle2D.Double(-40000, -40000, 80000, 80000);
			
			RobotPath pathEstimate;
			{ // clean up the old data once we have an initial path
				boolean reverseDirection = true;
				BufferedReader pathFile = new BufferedReader(new FileReader("sim-nopath.out"));
//				BufferedReader pathFile = new BufferedReader(new FileReader("short-vp.out"));
//				BufferedReader pathFile = new BufferedReader(new FileReader("path.out")); reverseDirection = false;
//				BufferedReader pathFile = new BufferedReader(new FileReader("vp-simple2.out"));
//				BufferedReader pathFile = new BufferedReader(new FileReader("vp-subset.out"));
//				BufferedReader pathFile = new BufferedReader(new FileReader("vp-simple.out"));
//				BufferedReader pathFile = new BufferedReader(new FileReader("vp-simple-700.out"));
//				BufferedReader pathFile = new BufferedReader(new FileReader("vp-simple-2000.out"));
//				BufferedReader pathFile = new BufferedReader(new FileReader("simpath.out")); reverseDirection = false;
				pathEstimate = RobotPath.read(pathFile, lmGen, reverseDirection);
				pathFile.close();
			}

			JFrame f = new JFrame("FastSLAM");
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.getContentPane().setLayout(new BorderLayout());

			// uncomment for simple viewer
//			int filterSize = 1;
//			ControlModel control = new SimpleControlModel(0, 0, 0);
			
//			int filterSize = 1000;									 	// 1000 from D. Fox
//			ControlModel control = new SimpleControlModel(500,300,0.5); 	// 500, 300, 0.5 from D. Fox
//			double[] sensorError = new double[] {5000, 0, 0, 5000}; 		// 5000 from D. Fox
			
			int filterSize = 200;
			ControlModel control = new SimpleControlModel(250,50,0.3); // 350, 150, 0.3
			SensorErrorModel sensorError = new DistanceSensorErrorModel(new double[] {100, 0, 0, 1000}, 8);
			
//			ControlModel control = new AssociationActionModel();
			ActionModel action = new SimpleActionModel(0,0,0,0);
//			ActionModel action = new AssociationActionModel(null, null);
			// TO DO: Choose good values for sensor and process errors.
			double[] processError = new double[] {0, 0, 0, 0};
			final SensorImageGenerator sensorImageGen = new SensorImageGenerator180(2500);
			final ParticleFilterPanel panel = new ParticleFilterPanel(pathEstimate, filterSize, sensorError, processError, mapBounds, sensorImageGen, action);
			f.getContentPane().add(panel, BorderLayout.CENTER);

			f.setSize(600, 600);
			f.setVisible(true);
			
			JFrame f2 = new JFrame("Control");
			f2.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			f2.getContentPane().setLayout(new BorderLayout());
			SimulationControlPanel simulationControlPanel = new SimulationControlPanel(panel);
			f2.getContentPane().add(simulationControlPanel, BorderLayout.CENTER);
			f2.setBounds(600, 0, 300, 600);
			f2.pack();
			f2.setVisible(true);

			panel.setDoubleBuffered(true);
			int step = 0;
			Random rand = RandomSingleton.instance;
			while (true) {
				final ParticleFilterManager filterManager = panel.getFilterManager();
				try {
//					Thread.sleep(2000);
					while( filterManager.update(rand, control, lmGen) ) {
						panel.repaint();
//						System.out.println(panel.getFilter().getBestMap().getLandmarkCount());
//						System.out.println("Best Pose: " + panel.getFilter().getBestMap().getLatestPose());
						System.out.println(step++);
//						System.gc();
//						panel.waitForPaint();
						simulationControlPanel.testRunning();
					}
					panel.repaint();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				LandmarkMap bestLandmarkMap = filterManager.getFilter().getBestMap();
				writePathToFile(bestLandmarkMap);
				System.out.println("Found " + bestLandmarkMap.getLandmarkCount() + " landmarks.");
				System.out.println("Restarting path estimation.");
				System.out.println("Removing data associations.");
				panel.restart();
				step = 0;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writePathToFile(LandmarkMap bestLandmarkMap) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(new FileOutputStream("path.out"));
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(3);
		df.setMinimumFractionDigits(3);
		df.setGroupingUsed(false);
		bestLandmarkMap.getPath().write(out, df, true);
		out.close();
	}
}
