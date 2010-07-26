package vp.model;

import java.util.Random;

import vp.dataassoc.AssocGenerator;
import vp.dataassoc.AssocMap;
import vp.dataassoc.AssocMapSet;
import vp.mapping.LandmarkObservationSet;
import vp.robot.RobotPath;
import vp.robot.RobotPose;

public class AssociationActionModel implements ActionModel {
	private static final RobotPose origin = new RobotPose(0, 0, 0);
	private final AssocMapSet set;
	private RobotPose action = origin;

	public ActionModel nextAction(RobotPath nextStep) {
		return new AssociationActionModel(nextStep.previousPath().getLandmarkObservations(), nextStep.getLandmarkObservations());
	}
	
	public AssociationActionModel(LandmarkObservationSet prev, LandmarkObservationSet curr) {
		if (prev != null && curr != null) {
			AssocGenerator generator = new AssocGenerator(curr, prev);
			generator.findPossibleAssociations(false);
			set = generator.createAssocMapSet();
			set.normalizeWeights();
		} else {
			set = null;
		}
	}

	public void selectActionHypothesis(Random rand) {
		if (set != null) {
			double value = rand.nextDouble();
			AssocMap map = set.selectMap(value);
			try {
				action = map.getRandomCurrentPose(origin);
			} catch (Error e) {
				action = new RobotPose(0, 0, 0);
			}
		}
	}

	public double getX() {
		return action.x;
	}

	public double getY() {
		return action.y;
	}

	public double getTheta() {
		return action.theta;
	}

	public double getElapsedTime() {
		return action.theta;
	}
}
