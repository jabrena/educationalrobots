package vp.fastslam;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import oursland.naming.UniqueNameGenerator;
import vp.mapping.LandmarkObservationSet;
import vp.model.ActionModel;
import vp.model.ControlModel;
import vp.robot.RobotPath;
import vp.robot.RobotPose;
import vp.sim.SensorImageGenerator;

/**
 * @author oursland
 */
public class ParticleFilterManager {
	private ParticleFilter filter;

	private ArrayList<RobotPath> pathEstimate = new ArrayList<RobotPath>();
	private int currentStep = 0;
	private LandmarkObservationSet lastObservations = null;
	private RobotPose lastPose = null;
	private ActionModel action;

	public ParticleFilterManager(ParticleFilterManager man, Rectangle2D initialMapBounds, SensorImageGenerator sensorImageGen) {
		this(man.filter.size(), man.filter.getSensorErrorModel(), man.filter.getProcessError(), initialMapBounds, sensorImageGen, man.action);
	}

	public ParticleFilterManager(int particleCount, SensorErrorModel sensorErrorModel, double[] processError, Rectangle2D initialMapBounds, SensorImageGenerator sensorImageGen, ActionModel action) {
		this.filter = new ParticleFilter(particleCount, sensorErrorModel, processError, initialMapBounds, sensorImageGen);
		this.action = action;
	}

	public synchronized RobotPose getLatestPose() {
		int lastIndex = pathEstimate.size()-1;
		RobotPath path = pathEstimate.get(lastIndex);
		return path.getLatestPose();
	}
	
	public synchronized boolean update(Random rand, ControlModel control, UniqueNameGenerator lmGen) {
		if( currentStep < pathEstimate.size() ) {
			// generate new estimates for the current robot pose from the best maps
			RobotPath nextStep = pathEstimate.get(currentStep);
			currentStep++;
			// action is in global coordinate system
			// we might want to condition the action passed in with the odometry action
			action = action.nextAction(nextStep);
//			System.out.println(action);
			LandmarkObservationSet observations = nextStep.getLandmarkObservations();
			filter.update(observations, control, action, lmGen);
			lastObservations = observations;
			lastPose = filter.getBestMap().getLatestPose();
			return true;
		}
		return false;
	}
	
	public synchronized void addToPathEstimate(LandmarkObservationSet lms, RobotPose pose) {
		int lastIndex = pathEstimate.size()-1;
		RobotPath lastElement = pathEstimate.get(lastIndex);
		RobotPath nextOnPath = new RobotPath(lms, pose, lastElement);
		pathEstimate.add( nextOnPath );
	}
	
	public synchronized void setPathEstimate(RobotPath pathEstimate) {
		LinkedList<RobotPath> pathStack = new LinkedList<RobotPath>();
		while( pathEstimate != null ) {
			pathStack.addFirst(pathEstimate);
			pathEstimate = pathEstimate.previousPath();
		}
		this.currentStep = 0;
		this.pathEstimate.clear();
		this.pathEstimate.addAll(pathStack);
		lastObservations = null;
		lastPose = null;
	}

	public LandmarkObservationSet getLastObservations() {
		return lastObservations;
	}

	public synchronized ParticleFilter getFilter() {
		return filter;
	}

	public  boolean isLastDataAvailable() {
		return lastPose != null && lastObservations != null;
	}

	public int getCurrentStep() {
		return currentStep;
	}

}
