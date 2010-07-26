package vp.sim;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.Timer;

import oursland.RandomSingleton;
import vp.VPConstant;
import vp.mapping.LandmarkObservation;
import vp.mapping.LandmarkObservationSet;
import vp.model.ControlModel;
import vp.model.SimpleControlModel;
import vp.robot.RobotPath;
import vp.robot.RobotPose;

public class RobotSimulation {
	private static final Random rand = RandomSingleton.instance;
	private final Timer timer = new Timer(50, new TimerListener());
	private final LandmarkObservationSet lms;
	private RobotPose robot;
	private ControlModel control = new SimpleControlModel(0,0,0);
	private final double maxVelX = 100.0;	// per second
	private final double maxVelY =   0.0;	// per second
	private final double maxVelA = 0.4;		// per second
	private final double accelRateLin = 0.5;
	private final double accelRateAng = 0.9;
	private double velX = 0.0;
	private double velY = 0.0;
	private double velA = 0.0;
	private boolean accelerate = false;
	private boolean turnLeft = false;
	private boolean turnRight = false;
	private double time = 0.0;
	private LandmarkObservationSet localImage = new LandmarkObservationSet();
	private RobotPath path = null;
	private final SensorImageGenerator sensorImageGen;
	
	public RobotSimulation(LandmarkObservationSet lms, SensorImageGenerator sensorImageGen) {
		this.lms = lms;
		this.sensorImageGen = sensorImageGen;
		this.robot = new RobotPose(0,0,0);
		this.timer.setCoalesce(true);
	}
	
	public RobotPose getPose() {
		return robot;
	}
	
	public void write(PrintWriter out, boolean outputTelemetry) throws IOException {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(3);
		df.setMinimumFractionDigits(3);
		df.setGroupingUsed(false);
		if(path != null ) {
			path.write(out, df, outputTelemetry);
		}
	}
	
	private static final double TODEGREE = 180.0/Math.PI;
	private static final Color halfblue = new Color(0.0f, 0.0f, 1.0f, 0.5f);
	public void paint(Graphics2D g) {
		lms.paint(g, Color.ORANGE, Color.BLACK);
		robot.paint(g, VPConstant.scaleDown);

		g.setColor(Color.BLACK);
		int size = 5000;
//		g.drawArc((int)(robot.x-size/2), (int)(robot.y-size/2), size, size, (int) (TODEGREE*robot.theta-90), (int) (TODEGREE*robot.theta+90));
		g.drawOval((int)(robot.x-size/2), (int)(robot.y-size/2), size, size);
		
		AffineTransform xform = new AffineTransform();
		xform.translate(robot.x, robot.y);
		xform.rotate(robot.theta);
		localImage.paint(g, halfblue, halfblue, xform);		
	}
	
	public void setUpdate(int delay) {
		timer.setDelay(delay);
	}
	
	public void run() {
		timer.restart();
	}
	
	public void pause() {
		timer.stop();
	}
	
	public boolean isRunning() {
		return timer.isRunning();
	}
	
	public void accelerate() {
		accelerate = true;
	}
	
	public void decelerate() {
		accelerate = false;
	}
	
	public void turnLeft() {
		turnLeft = true;
		turnRight = false;
	}
	
	public void turnRight() {
		turnLeft = false;
		turnRight = true;
	}
	
	public void goStraight() {
		turnLeft = false;
		turnRight = false;
	}

	private void updateXVelocity(double dt) {
		double rate = dt*accelRateLin;
		if(accelerate) {
			velX = (1.0-rate)*velX + (rate)*maxVelX;
		} else {
			velX = (1.0-rate)*velX + (rate)*0;
		}		
	}
	
	private void updateAngleVelocity(double dt) {
		double rate = dt*accelRateAng;
		if(turnLeft) {
			velA = (1-rate)*velA + (rate)*maxVelA;		
		} else if(turnRight) {
			velA = (1-rate)*velA - (rate)*maxVelA;
		} else {
			velA = (1-rate)*velA + (rate)*0;		
		}		
	}
	
	public LandmarkObservationSet getSensorImage() {
		ArrayList<LandmarkObservation> lmal = new ArrayList<LandmarkObservation>();
		AffineTransform xform = new AffineTransform();
//		double c = Math.cos(robot.theta);
//		double s = Math.sin(robot.theta);
//		AffineTransform rotate = new AffineTransform(c, -s, 0, s, c, 0);
		xform.rotate(-robot.theta);
		xform.translate(-robot.x, -robot.y);
		
		for (Iterator iter = lms.iterator(); iter.hasNext();) {
			LandmarkObservation lm = (LandmarkObservation) iter.next();
			Point2D local = xform.transform( new Point2D.Double(lm.getX(), lm.getY()), null);
//			System.out.println(local);
			if( sensorImageGen.isSensed(local.getX(), local.getY()) ) {
//				System.out.println(local.distance(0,0));
				lmal.add(new LandmarkObservation(local, lm.getAssociationName()));
//				lmal.add(lm);
			}
		}
		LandmarkObservation[] lma = new LandmarkObservation[lmal.size()];
		lmal.toArray(lma);
		LandmarkObservationSet rc = new LandmarkObservationSet(lma, time);
		return rc;
	}

	private synchronized void simpleUpdate(double dt) {
//		RobotPose previousPose = robot;
		
		updateXVelocity(dt);
		updateAngleVelocity(dt);
//		System.out.println(velX + "\t" + velY + "\t" + velA + "\t" + dt);
		double c = Math.cos(robot.getTheta());
		double s = Math.sin(robot.getTheta());
		double newX = robot.getX() + dt*(c*velX) + dt*(s*velY);
		double newY = robot.getY() + dt*(s*velX) + dt*(c*velY);
		double newA = robot.getTheta() + dt*(velA);
		robot = new RobotPose(newX, newY, newA);
		time += dt;
		localImage = getSensorImage();
		
//		RobotPose currentPose = robot;
//		RobotActionModel action = currentPose.getActionFrom(previousPose,0.1);
//		ControlModel control = new ControlModel(0,0,0);
//		RobotPose predicted = control.nextPose(RandomSingleton.instance, previousPose, action);
//		System.out.println(currentPose.getActionFrom(predicted, dt));
	}
	
	private class TimerListener implements ActionListener {
		public synchronized void actionPerformed(ActionEvent e) {
			simpleUpdate(0.2);
			path = new RobotPath(localImage, robot, path);
			fireActionEvent();
		}
	}
	
	ActionListener listener = null;
	ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
	public void addActionListener(ActionListener l) {
		listeners.add(l);
	}

	public void removeActionListener(ActionListener l) {
		listeners.remove(l);
	}
	
	public void fireActionEvent() {
		ActionEvent event = new ActionEvent(this, 0, "timestep");
		ActionListener[] l = listeners.toArray(new ActionListener[listeners.size()]);
		for (int i = 0; i < l.length; i++) {
			l[i].actionPerformed(event);
		}
	}

	public void removeDataAssociation() {
		path.removeDataAssociation();
	}

	public void addNoiseToTelemetry(double xVar, double yVar, double thetaVar) {
		LinkedList<RobotPath> list = new LinkedList<RobotPath>();
		RobotPath temp = path;
		while(temp != null) {
			list.addFirst(temp);
			temp = temp.previousPath();
		}
		double dx = 0;
		double dy = 0;
		double dt = 0;
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			RobotPath path = (RobotPath) iter.next();
			path.adjustPose(dx, dy, dt);
			dx += xVar*rand.nextGaussian();
			dy += yVar*rand.nextGaussian();
			dt += thetaVar*rand.nextGaussian();
		}
	}

	public void addNoiseToObservations(int var) {
		RobotPath temp = path;
		while(temp != null) {
			temp.addNoiseToObservations(var);
			temp = temp.previousPath();
		}
	}
}
