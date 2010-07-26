package vp.mapping;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;

import javax.swing.JPanel;

import vp.VPConstant;
import vp.model.ControlModel;
import vp.model.SimpleActionModel;
import vp.robot.RobotPath;
import vp.robot.RobotPose;

public class PathPanel extends JPanel {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -1770579805276838743L;
	private LinkedList<RobotPath> pathEstimate = new LinkedList<RobotPath>();
	private ListIterator currentStep ;
	private PathSet paths;
//	private LandmarkData lmd;
	private static final int pathCount = 1;
	private static final int displayPathHistory = 400;
	private static final int displayLandmarkHistory = 40;
	private static final float landmarkDecay = 0.90f;
	private static final float pathDecay = 0.98f;
	
	public PathPanel(RobotPath pathEstimate) {
		setOpaque(true);
		setBackground(Color.white);
		setPathEstimate(pathEstimate);
	}
	
	public RobotPath getPathEstimate() {
		return this.pathEstimate.getLast();
	}
	
	public void setPathEstimate(RobotPath pathEstimate) {
		this.pathEstimate.clear();
		while( pathEstimate != null ) {
			this.pathEstimate.addFirst(pathEstimate);
			pathEstimate = pathEstimate.previousPath();
		}
		currentStep = this.pathEstimate.listIterator(0);
		RobotPath nextStep = (RobotPath) currentStep.next();
		paths = new PathSet(nextStep.getGlobalLandmarkSet(), pathCount);
	}

	public synchronized boolean update(Random rand, ControlModel control) {
		if( currentStep.hasNext() ) {
			RobotPath nextStep = (RobotPath) currentStep.next();
			SimpleActionModel action = nextStep.getPreviousAction();
			LandmarkObservationSet landmarks = nextStep.getLandmarkObservations();
			paths = paths.update(rand, action, control, landmarks);
//			paths.error();
			return true;
		} else {
			return false;
		} 
//		System.out.println(paths.get(0).getError());
//		repaint();
	}
	
	public synchronized void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		AffineTransform lastTransform = g2.getTransform();
//		try {
//			g2.transform(paths.get(0).getLatestPose().getTransform().createInverse());
//		} catch (NoninvertibleTransformException e) {
//			e.printStackTrace();
//		}
		RobotPath currentBestPath = paths.get(0);
		RobotPose localSpace = currentBestPath.getLatestPose();

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		g2.translate(getWidth()/2, getHeight()/2);
		g2.scale(1.0/VPConstant.scaleDown, 1.0/VPConstant.scaleDown);
		g2.translate(-localSpace.getX(), -localSpace.getY());
//		for (int i = 0; i < 1/*paths.size()*//*Math.min(paths.size(), 10)*/; i++) {
//			paths.get(i).paint(g2);
//		}
//		System.out.println(paths.get(0).getError());
		currentBestPath.paintLandmarks(g2, displayLandmarkHistory, Color.orange, Color.black, 1.0f, landmarkDecay);
		currentBestPath.paintPath(g2, displayPathHistory, pathDecay);
		currentBestPath.paint(g2);
//		System.out.println();
		g2.setTransform(lastTransform);
	}
	
	public void updateEstimate() {
		setPathEstimate(paths.get(0));
	}
}
