import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import javax.swing.JFrame;

import oursland.RandomSingleton;
import oursland.naming.UniqueNameGenerator;
import vp.fastslam.ConstantSensorErrorModel;
import vp.fastslam.SensorErrorModel;
import vp.mapping.LandmarkObservation;
import vp.mapping.LandmarkObservationSet;
import vp.model.ActionModel;
import vp.model.ControlModel;
import vp.model.SimpleActionModel;
import vp.model.SimpleControlModel;
import vp.robot.RobotPath;
import vp.robot.RobotPose;
import vp.sim.RobotSimulation;
import vp.sim.SensorImageGenerator;
import vp.sim.SensorImageGenerator180;
import vp.ui.ParticleFilterPanel;

public class SimulateFastSLAM {

	public static void main(String[] args) {
		final Rectangle2D mapBounds = new Rectangle2D.Double(-6000, -6000, 12000, 12000);
		final Random rand = RandomSingleton.instance;
		final UniqueNameGenerator lmGen = new UniqueNameGenerator("lm");

		LandmarkObservation[] lma = new LandmarkObservation[75];
		for (int i = 0; i < lma.length; i++) {
			lma[i] =
				new LandmarkObservation(
					RobotSimulationPanel.getPointSample(rand, -5000, 5000),
					RobotSimulationPanel.getPointSample(rand, -5000, 5000),
					lmGen.create());
		}
		LandmarkObservationSet simulationMap = new LandmarkObservationSet(lma, 0.0);

		//		int filterSize = 300;
		//		ControlModel control = new ControlModel(100,25,0.2);
		int filterSize = 20;
		final ControlModel control = new SimpleControlModel(150, 10, 0.0);
//		int filterSize = 1;
//		final ControlModel control = new ControlModel(0,0,0);
		SensorErrorModel sensorError = new ConstantSensorErrorModel(new double[] { 150, 0, 0, 150 });
		double[] processError = new double[] { 1, 0, 0, 1 };

		final SensorImageGenerator sensorImageGen = new SensorImageGenerator180(2500);
		final RobotSimulationPanel simulationPanel = new RobotSimulationPanel(simulationMap, sensorImageGen);
		final RobotSimulation sim = simulationPanel.getSim();
		final RobotPath initialPath = new RobotPath(sim.getSensorImage(), sim.getPose());
		final ActionModel action = new SimpleActionModel(0,0,0,0);
		final ParticleFilterPanel filterPanel =
			new ParticleFilterPanel(initialPath, filterSize, sensorError, processError, mapBounds, sensorImageGen, action);
		filterPanel.getFilterManager().getFilter().setUseKnownDataAssociations(false);

		sim.addActionListener(new ActionListener() {
			private RobotPose lastPose = sim.getPose();
			private ControlModel control = new SimpleControlModel(0,0,0);
			public synchronized void actionPerformed(ActionEvent e) {
				RobotPose nextPose = sim.getPose();
				SimpleActionModel action = nextPose.getActionFrom(lastPose, 0.1);
//				System.out.println(action);
				lastPose = nextPose;
				RobotPose pathPose = filterPanel.getFilterManager().getLatestPose();
//				control.setObservations(prevObservations, currObservations);
				RobotPose newPathPose = control.nextPose(RandomSingleton.instance, pathPose, action);
				filterPanel.getFilterManager().addToPathEstimate(
					sim.getSensorImage().getNoisySensors(5),
					newPathPose.getNoisyPose(5, 1, 0.005));
				filterPanel.getFilterManager().update(rand, control, lmGen);
				filterPanel.repaint();
			}
		});

		JFrame filterFrame = new JFrame("FastSLAM");
		filterFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		filterFrame.getContentPane().setLayout(new BorderLayout());
		filterFrame.getContentPane().add(filterPanel, BorderLayout.CENTER);
		filterFrame.setLocation(600, 0);
		filterFrame.setSize(600, 600);
		filterFrame.setVisible(true);

		JFrame simFrame = new JFrame("Robot Simulator");
		simFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		simFrame.getContentPane().setLayout(new BorderLayout());
		simFrame.getContentPane().add(simulationPanel);
		simFrame.setSize(600, 600);
		simFrame.setVisible(true);

	}
}
