import java.awt.BorderLayout;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import javax.swing.JApplet;
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

public class FastSLAMApplet extends JApplet {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 4976128983321251855L;
	Thread runThread = null;
	boolean exit = false;
	boolean pause = true;
	
	@Override
	public void init() {
		super.init();
		System.out.println("init");
		final UniqueNameGenerator lmGen = new UniqueNameGenerator("lm");
		Rectangle2D mapBounds = new Rectangle2D.Double(-40000, -40000, 80000, 80000);
		RobotPath pathEstimate = null;
		Random rand = RandomSingleton.instance;
		try {
			System.out.println(getCodeBase());
			System.out.println(getDocumentBase());
			URL url = new URL(getDocumentBase(), "../fastslam-data/path2.out");
			BufferedReader pathFile = new BufferedReader(new FileReader(url.getFile()));
			boolean reverseDirection = true;
			pathEstimate = RobotPath.read(pathFile, lmGen, reverseDirection);
			pathFile.close();

			this.getContentPane().setLayout(new BorderLayout());
			
			int filterSize = 500;
			final ControlModel control = new SimpleControlModel(250,50,0.3); // 350, 150, 0.3
			SensorErrorModel sensorError = new DistanceSensorErrorModel(new double[] {100, 0, 0, 1000}, 8);
			ActionModel action = new SimpleActionModel(0,0,0,0);
			double[] processError = new double[] {0, 0, 0, 0};
			final SensorImageGenerator sensorImageGen = new SensorImageGenerator180(2500);
			final ParticleFilterPanel panel = new ParticleFilterPanel(pathEstimate, filterSize, sensorError, processError, mapBounds, sensorImageGen, action);
			getContentPane().add(panel, BorderLayout.CENTER);
			final SimulationControlPanel simulationControlPanel = new SimulationControlPanel(panel);
			this.getContentPane().add(simulationControlPanel, BorderLayout.EAST);
			panel.setDoubleBuffered(true);
			
			runThread = new Thread( new Runnable() {
				public void run() {
					int step = 0;
					Random rand = RandomSingleton.instance;
					while (true) {
						final ParticleFilterManager filterManager = panel.getFilterManager();
						try {
							while( filterManager.update(rand, control, lmGen) ) {
								panel.repaint();
								System.out.println(step++);
								simulationControlPanel.testRunning();
								if(exit) return;
								synchronized(runThread) {
									if(pause) this.wait();
								}
							}
							panel.repaint();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						LandmarkMap bestLandmarkMap = filterManager.getFilter().getBestMap();
						panel.restart();
						step = 0;
					}
				}
			});
			runThread.start();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(MalformedURLException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		exit = true;
	}

	@Override
	public void start() {
		super.start();
		if( runThread != null ) {
			synchronized(runThread) {
				pause = false;
				runThread.notify();
			}
		}
	}

	@Override
	public void stop() {
		super.stop();
		if( runThread != null ) {
			synchronized(runThread) {
				pause = true;
			}
		}
	}
}
