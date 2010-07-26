import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import oursland.RandomSingleton;
import oursland.naming.UniqueNameGenerator;
import vp.VPConstant;
import vp.mapping.LandmarkObservation;
import vp.mapping.LandmarkObservationSet;
import vp.robot.RobotPose;
import vp.sim.RobotSimulation;
import vp.sim.SensorImageGenerator;
import vp.sim.SensorImageGenerator180;

public class RobotSimulationPanel extends JPanel {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -5797606943227795139L;
	private RobotSimulation sim;
	private boolean telemetry = true;
	public KeyController keyListener = new KeyController();
	
	public RobotSimulationPanel(LandmarkObservationSet lms, SensorImageGenerator sensorImageGen) {
		sim = new RobotSimulation(lms, sensorImageGen);
		setOpaque(true);
		setBackground(Color.white);
		setFocusable(true);
		addKeyListener(keyListener);
		
		sim.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				System.out.println(sim.getPose());
				repaint();
			}
		});
		
//		Graphics2D g2 = (Graphics2D) getGraphics();
		setFocusable(true);
	}
	
	public void addActionListener(ActionListener l) {
		sim.addActionListener(l);
	}
	
	public void removeActionListener(ActionListener l) {
		sim.removeActionListener(l);
	}
		
	public void cleanup() {
		sim.pause();
		sim = null;
	}
	
	public synchronized void paint(Graphics g) {
		synchronized(sim) {
			super.paint(g);
			Graphics2D g2 = (Graphics2D) g;
			AffineTransform lastTransform = setupTransform(g2);
		
			sim.paint(g2);
	
			g2.setTransform(lastTransform);
		}
	}
	
	protected AffineTransform setupTransform(Graphics2D g2) {
		AffineTransform lastTransform = g2.getTransform();
	
		RobotPose robot = sim.getPose();
		double xOffset = robot.getX()-getWidth()/2;
		double yOffset = robot.getY()-getHeight()/2;
//		System.out.println(robot.getX() + "\t" + robot.getY());
//		double xOffset = localSpace.getX();
//		double yOffset = localSpace.getY();
				
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		g2.translate(getWidth()/2, getHeight()/2);
		g2.scale(1.0/VPConstant.scaleDown, -1.0/VPConstant.scaleDown);
		g2.translate(-xOffset, -yOffset);
		
		return lastTransform;		
	}

	private class KeyController implements KeyListener {
		public void keyTyped(KeyEvent e) {
			switch(e.getKeyChar()) {
				case KeyEvent.VK_SPACE: {
					if(sim.isRunning()) {
						System.out.println("pause");
						sim.pause();
					} else {
						System.out.println("run");
						sim.run();
					}
				} break;
				case KeyEvent.VK_W: {
					sim.pause();
					System.out.println("Writing path to file \"simpath.out\".");
					try {
						PrintWriter out = new PrintWriter(new FileOutputStream("simpath.out"));
						sim.write(out, telemetry);
						out.close();					
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} break;
				case KeyEvent.VK_T: {
					telemetry = !telemetry;
					if(telemetry) {
						System.out.println("Turning telemetry output ON.");
					} else {
						System.out.println("Turning telemetry output OFF.");
					}
				} break;
				case KeyEvent.VK_A: {
					System.out.println("Removing data association.");
					sim.removeDataAssociation();
				} break;
				case KeyEvent.VK_U: {
					System.out.println("Adding cumulative error to telemetry.");
					sim.addNoiseToTelemetry(1, 0.1, 0.001);
				} break;
				case KeyEvent.VK_O: {
					System.out.println("Adding error to observations");
					sim.addNoiseToObservations(5);
				} break;
			}
		}

		public void keyPressed(KeyEvent e) {
			switch( e.getKeyCode() ) {
				case KeyEvent.VK_UP:	sim.accelerate();	break;
				case KeyEvent.VK_LEFT:	sim.turnLeft();		break;
				case KeyEvent.VK_RIGHT:	sim.turnRight();	break;
			}
		}

		public void keyReleased(KeyEvent e) {
			switch( e.getKeyCode() ) {
				case KeyEvent.VK_UP:	sim.decelerate();	break;
				case KeyEvent.VK_LEFT:	sim.goStraight();	break;
				case KeyEvent.VK_RIGHT:	sim.goStraight();	break;
			}
		}			
	}
	
	protected RobotSimulation getSim() {
		return sim;
	}
	
	public static void main(String[] args) {
		UniqueNameGenerator lmGen = new UniqueNameGenerator("lm");
		Random rand = RandomSingleton.instance;
		JFrame f = new JFrame("Robot Simulation");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().setLayout(new BorderLayout());
		LandmarkObservation[] lma = new LandmarkObservation[400]; 
		for( int i = 0; i < lma.length; i++ ) {
			lma[i] = new LandmarkObservation(
				getPointSample(rand, -10000, 10000), 
				getPointSample(rand, -10000, 10000), 
				lmGen.create());
		}
		LandmarkObservationSet landmarks = new LandmarkObservationSet(lma, 0.0);
//		try {
//			BufferedReader lmIn = new BufferedReader(new FileReader("landmarks1.lms"));
//			landmarks.read(lmIn);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		f.getContentPane().add(new RobotSimulationPanel(landmarks, new SensorImageGenerator180(2500)));
		f.setSize(500,500);
		f.setVisible(true);
	}
	
	public static double getPointSample(Random rand, double min, double max) {
		double delta = max - min;
		return delta*rand.nextDouble() + min;
	}
}
