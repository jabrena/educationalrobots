package vp.ui;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;
import vp.VPConstant;
import vp.fastslam.LandmarkMap;
import vp.fastslam.ParticleFilterManager;
import vp.fastslam.SensorErrorModel;
import vp.model.ActionModel;
import vp.robot.RobotPath;
import vp.robot.RobotPose;
import vp.sim.SensorImageGenerator;

public class ParticleFilterPanel extends JPanel {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -2800966399530298889L;
	// Best estimate contains the path that is used to generate the actions and sensors
	private ParticleFilterManager man;
	private Rectangle2D initialMapBounds;
	
	final private SensorImageGenerator sensorImageGen;
	
	private Object paintNotifier = new Object();

	private static final int pathDisplayAge = 400;
	private static final int landmarkDisplayAge = 40;
	private static final float landmarkAgeDecay = 0.90f;
	private static final float pathAgeDecay = 0.98f;
	private static final Color halfBlack = new Color(0f,0f,0f,0.25f);
	private static final Color halfBlue = new Color(0f,0f,1f,0.5f);

	private int displayIndex = 0;

	public ParticleFilterPanel(RobotPath pathEstimate, int particleCount, SensorErrorModel sensorErrorModel, double[] processError, Rectangle2D mapBounds, SensorImageGenerator sensorImageGen, ActionModel action ) {
		this.initialMapBounds = mapBounds;
		setOpaque(true);
		setBackground(Color.white);
		man = new ParticleFilterManager(particleCount, sensorErrorModel, processError, initialMapBounds, sensorImageGen, action);
		this.sensorImageGen = sensorImageGen;
		man.setPathEstimate(pathEstimate);
	}
	
	public synchronized void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		AffineTransform lastTransform = g2.getTransform();

		LandmarkMap displayMap = getDisplayMap();
		RobotPose localSpace = displayMap.getLatestPose();

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		g2.translate(getWidth()/2, getHeight()/2);
//		Rectangle2D displayBounds1 = g2.getClipBounds();
		g2.scale(1.0/VPConstant.scaleDown, -1.0/VPConstant.scaleDown);
//		Rectangle2D displayBounds2 = g2.getClipBounds();
		g2.translate(-localSpace.getX(), -localSpace.getY());		
		
		Rectangle2D displayBounds = g2.getClipBounds();
		displayMap.paintLandmarks(g2, Color.orange, Color.black, displayBounds);

		// paint the last sensor image in blue
		if( man.isLastDataAvailable() ) {
			// the last sensor image may have already been transformed...
//			lastSensorImage.transform(lastPose.getTransform()).paint(g2, halfBlue, halfBlue);
			AffineTransform xform = new AffineTransform();
			xform.translate(localSpace.x, localSpace.y);
			xform.rotate(localSpace.theta);
			man.getLastObservations().paint(g2, halfBlue, halfBlue, xform);
		}		

//		currentBestMap.paintPath(g2, Color.black, pathDisplayAge, pathAgeDecay);
		g2.setColor(Color.black);
		displayMap.paintRobot(g2);
		g2.setColor(halfBlack);
//		man.getFilter().paintParticles(g2, displayMap);
		
		// paint the next 5 best paths
//		for( int i = 1; i < 6 && i<filter.size(); i++ ) {
//			filter.getMap(i).paintPath(g2, halfBlack, pathDisplayAge/2, 1-(2*(1-pathAgeDecay)));
//		}
		displayMap.paintPath(g2, halfBlack, man.getCurrentStep(), 1.0f);

		g2.setTransform(lastTransform);
		synchronized(paintNotifier) {
			paintNotifier.notifyAll();
		}
	}

	public synchronized void restart() {
		RobotPath bestPath = man.getFilter().getBestMap().getPath();
		bestPath.removeDataAssociation();
		man = new ParticleFilterManager(man, initialMapBounds, sensorImageGen);
		man.setPathEstimate(bestPath);
	}

	public synchronized void waitForPaint() {		
		synchronized(paintNotifier) {
			try {
				paintNotifier.wait(500);
			} catch (InterruptedException e) {
				System.out.println("Received notify.");
			}
		}
	}
	
	public ParticleFilterManager getFilterManager() {
		return man;
	}

	public int getDisplayIndex() {
		return displayIndex;
	}

	public LandmarkMap getDisplayMap() {
		return man.getFilter().getSortedMap(displayIndex);
	}

	public void setDisplayIndex(int i) {
		displayIndex = (i+man.getFilter().size())%man.getFilter().size();
		repaint();
	}
}
