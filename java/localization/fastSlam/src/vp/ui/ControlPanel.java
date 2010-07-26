package vp.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

import oursland.RandomSingleton;
import vp.VPConstant;
import vp.data.GpsData;
import vp.data.GpsDatum;
import vp.data.LandmarkData;
import vp.data.LaserData;
import vp.data.LaserDatum;
import vp.mapping.Offset;

public class ControlPanel extends JPanel {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -4651756077445911741L;

	public void setEnabled( boolean b ) {
		super.setEnabled(b);
		startButton.setEnabled(b);
		update.setEnabled(b);
		next.setEnabled(b);
		localize.setEnabled(b);
		telemetry.setEnabled(b);
		write.setEnabled(b);
	}

	private JButton startButton = new JButton("Start/Stop");
	private JButton update = new JButton("Update");
	private JButton next = new JButton("Next");
	private JTextField delayField = new JTextField("100", 8);
	private JTextField timeField = new JTextField("0", 8);
	private JLabel maxTime = new JLabel();
	private JCheckBox showLastButton = new JCheckBox("Show previous timestep");
	private JTextField prevCountField = new JTextField("1", 5);
	private JTextField xOffsetField = new JTextField(5);
	private JTextField yOffsetField = new JTextField(5);
	private JTextField thetaField = new JTextField(5);
	private JButton localize = new JButton("Localize Sample");
	private JButton telemetry = new JButton("Create Telemetry");
	private JButton write = new JButton("Write Data File");

	private ActionListener l = new ControlActionListener();

	private boolean running = false;
	private boolean showLast = false;
	private int time = 0;
	private int laserIndex = 0;
	private int delay = 100;
	private int prevCount = 1;
	private double xOffset = 0.0;
	private double yOffset = 0.0;
	private double theta = 0.0;
	private Timer timer = new Timer(delay, l);

	private NumberFormat nf = NumberFormat.getInstance();

	private LaserData laser;
	private GpsData gps;
	private LandmarkData landmark;
	private ImagePanel imagePanel;
	private LandmarkPanel pointPanel;
	
	private BufferedImage currImage = new BufferedImage(
		VPConstant.imageWidth, 
		VPConstant.imageHeight, 
		BufferedImage.TYPE_INT_ARGB
	);
	private Graphics2D currG = currImage.createGraphics();
	private BufferedImage prevImage = new BufferedImage(
		VPConstant.imageWidth, 
		VPConstant.imageHeight, 
		BufferedImage.TYPE_INT_ARGB
	);
	private Graphics2D prevG = prevImage.createGraphics();

	public ControlPanel(ImagePanel imagePanel, LandmarkPanel pointPanel) {
		this.imagePanel = imagePanel;
		this.pointPanel = pointPanel;
		
		nf.setGroupingUsed(false);
		nf.setMaximumFractionDigits(4);

		setLayout( new BoxLayout(this, BoxLayout.Y_AXIS) );

		add(startButton);
		add(buildTextLabel("run delay", delayField));
		JPanel timePanel = buildTextLabel("Time ", timeField);
		timePanel.add(new JLabel(" of "));
		timePanel.add(maxTime);
		add(timePanel);
		add(update);
		add(next);
		add(showLastButton);
		add(buildTextLabel("previous count", prevCountField));
		add(buildTextLabel("x offset", xOffsetField));
		add(buildTextLabel("y offset", yOffsetField));
		add(buildTextLabel("theta", thetaField));
		add(localize);
		add(telemetry);
		add(write);

		timer.addActionListener(l);

		startButton.addActionListener(l);
		delayField.addActionListener(l);
		timeField.addActionListener(l);
		update.addActionListener(l);
		next.addActionListener(l);
		showLastButton.addActionListener(l);
		prevCountField.addActionListener(l);
		xOffsetField.addActionListener(l);
		yOffsetField.addActionListener(l);
		thetaField.addActionListener(l);
		localize.addActionListener(l);
		telemetry.addActionListener(l);
		write.addActionListener(l);
	}

	private static JPanel buildTextLabel(String label, JTextField text) {
		JPanel rc = new JPanel();
		rc.setLayout(new FlowLayout());
		rc.add(new JLabel(label));
		rc.add(text);
		return rc;
	}

	public void setData(LaserData laser, GpsData gps) {
		this.laser = laser;
		this.gps = gps;
		this.landmark = new LandmarkData(laser);

		stop();
		int ls = laser.size();
		maxTime.setText(nf.format(laser.getTS(ls - 1)));
		setTime(laser.getTS(0));
		setShowLast(false);
		update();
	}

	private void setTime(double t) {
		setTime( (int)(t*1000) );
//		timeField.setText(nf.format(t));
	}

	private void toggleStart() {
		if( running ) {
			timer.stop();
		} else {
			timer.start();
		}
		running = !running;
	}

	private void stop() {
		timer.stop();
		running = false;
	}

	private void setShowLast(boolean showLast) {
		if (showLastButton.isSelected() != showLast) {
			showLastButton.setSelected(showLast);
		} else {
			prevCountField.setEnabled(showLast);
			xOffsetField.setEnabled(showLast);
			yOffsetField.setEnabled(showLast);
			thetaField.setEnabled(showLast);
		}
		this.showLast = showLast;
		update();
	}

	private void setDelay(int delay) {
		if( !delayField.getText().equals(Integer.toString(delay))) {
			delayField.setText(Integer.toString(delay));
		}
		timer.setDelay(delay);
		this.delay = delay;
	}

	private void setTime(int time) {
		this.laserIndex = laser.getTimeIndex(time);
//		System.out.print(""+this.laserIndex+" "+time);
		time = (int) (1000*laser.getTS(this.laserIndex));
//		System.out.println(" "+time+" "+timeField.getText());
		String expected = Double.toString(time/1000.0);
		if( !timeField.getText().equals(expected)) {
			timeField.setText(expected);
		}
		this.time = time;
		// update from gps
		int gpsIndex = gps.getTimeIndex(time);
		if( gpsIndex < gps.size() ) {
			GpsDatum prev = gps.getSample(gpsIndex);
			GpsDatum curr = gps.estimateSample(time);
			Offset offset = curr.getOffset(prev);
//			System.out.println(
//				"From " + prev.x + " " + prev.y +
//				" to " + curr.x + " " + curr.y +
//				" is " + offset.x + " " + offset.y
//			);
			setXOffset(offset.x);
			setYOffset(offset.y);
			setTheta(0.0/*offset.t*/);
		}
	}

	private void setPrevCount(int prevCount) {
		if( !prevCountField.getText().equals(Integer.toString(prevCount))) {
			prevCountField.setText(Integer.toString(prevCount));
		}
		this.prevCount = prevCount;
	}


	private void setXOffset(double d) {
		if( !xOffsetField.getText().equals(Double.toString(d))) {
			xOffsetField.setText(nf.format(d));
		}
		this.xOffset = d;
	}

	private void setYOffset(double d) {
		if( !yOffsetField.getText().equals(Double.toString(d))) {
			yOffsetField.setText(nf.format(d));
		}
		this.yOffset = d;
	}

	private void setTheta(double d) {
		if( !thetaField.getText().equals(Double.toString(d))) {
			thetaField.setText(nf.format(d));
		}
		this.theta = d;
	}

	private void next() {
		setTime(laser.getTS(this.laserIndex+1));	// set time updates laser index
		update();
	}

	private static final Color lastColor = Color.lightGray;

	private void update() {
		if( showLast && laserIndex - prevCount >= 0 ) {
//			updateAtTimeIndex(prevImage, prevG, Color.green, this.laserIndex - prevCount);
			pointPanel.setPoints(lastColor, landmark.getSample(laserIndex-prevCount).getData());
		} else {
			pointPanel.setPoints(lastColor, null);
		}
//		updateAtTimeIndex(currImage, currG, Color.black, this.laserIndex);
//		paintImages();
		pointPanel.setPoints(Color.orange, landmark.getSample(laserIndex).getData());
	}
	
	private void paintImages() {
		BufferedImage image = imagePanel.getImage();
		Graphics2D g = imagePanel.getImageGraphics();
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
		g.setColor(Color.white);
		g.fillRect(0, 0, imageWidth, imageHeight);
//		Composite oldComp = g.getComposite();
//		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, 0.5f));
//		g.setComposite(AlphaComposite.Clear);
//		g.setComposite(AlphaComposite.Src);
		if( showLast ) {
			AffineTransform xform = new AffineTransform();
			// rotate around the robot
			xform.translate(VPConstant.robotImageX, VPConstant.robotImageY);
			xform.rotate(-theta);
			xform.translate(-VPConstant.robotImageX, -VPConstant.robotImageY);
			// move the robot (in the rotation direction)
			xform.translate(-xOffset/VPConstant.scaleDown, -yOffset/VPConstant.scaleDown);
			g.drawImage(prevImage, xform, imagePanel);
		}
		g.drawImage(currImage, 0, 0, imageWidth, imageHeight, imagePanel);
		g.setColor(Color.black);
		double ovalWidth = (100.0/VPConstant.scaleDown);
		double ovalHeight = (100.0/VPConstant.scaleDown);
		double ovalX = VPConstant.robotImageX - ovalWidth / 2;
		double ovalY = VPConstant.robotImageY - ovalHeight / 2;
		g.drawOval((int)ovalX, (int)ovalY, (int)ovalWidth, (int)ovalHeight);
		imagePanel.repaint();
//		g.setComposite(oldComp);
	}

	private void updateAtTimeIndex(BufferedImage image, Graphics2D g, Color color, int timeIndex ) {
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

//		g.setComposite(AlphaComposite.Src);
//		g.setColor(new Color(1.0f, 1.0f, 1.0f, 1.0f));
//		g.fillRect(0, 0, imageWidth, imageHeight);

		g.setComposite(AlphaComposite.Clear);
		g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.0f));
		g.fillRect(0, 0, imageWidth, imageHeight);

		if( timeIndex > 0 ) {
			drawLocalSensorMap(image, color, laser.getSample(timeIndex));
		}
	}

	private static void drawLocalSensorMap(BufferedImage image, Color color, LaserDatum ls) {
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
		for( int k = 0; k < ls.size(); k++ ) {
			Point2D p = ls.getReadingLocation(k);
			double dist = p.distance(0.0, 0.0);
			if( dist < 3000  && dist > 100 ) {
				int x = (int)Math.max(
					1,
					Math.min(
						imageWidth-1,
						Math.round(p.getX()/VPConstant.scaleDown + VPConstant.robotImageX)
					)
				);
				int y = (int)Math.max(
					1,
					Math.min(
						imageHeight-1,
						Math.round(p.getY()/VPConstant.scaleDown + VPConstant.robotImageY)
					)
				);
				drawBlock(x, y, color, image);
			}
//			image.setRGB(x, y, Color.black.getRGB());
		}
	}

	private static void drawBlock( int x, int y, Color color, BufferedImage image ) {
		int rgb = color.getRGB();
		image.setRGB(x, y, rgb);
		image.setRGB(x-1, y, rgb);
		image.setRGB(x, y-1, rgb);
		image.setRGB(x-1, y-1, rgb);
	}

	private class ControlActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if( src == timer ) {
				next();
			} else if( src == startButton ) {
				toggleStart();
			} else if( src == showLastButton ) {
				setShowLast(showLastButton.isSelected());
			} else if( src == update ) {
				update();
			} else if( src == next ) {
				next();
			} else if( src == write ) {
				writeDataFile();
			} else if( src == localize ) {
				new Thread( new Runnable() {
					public void run() {
						localize(laserIndex-prevCount, laserIndex);
					}
				}).start();
			} else if( src == telemetry ) {
				new Thread( new Runnable() {
					public void run() {
						telemetry();
					}
				}).start();
			} else if( src == delayField ) {
				try {
					setDelay(Integer.parseInt(delayField.getText()));
				} catch (NumberFormatException e1) {
					setDelay(delay);
				}
			} else if( src == timeField ) {
				try {
					setTime(Double.parseDouble(timeField.getText()));
				} catch (NumberFormatException e1) {
					setTime(time);
				}
			} else if( src == prevCountField ) {
				try {
					setPrevCount(Integer.parseInt(prevCountField.getText()));
				} catch (NumberFormatException e1) {
					setPrevCount(prevCount);
				}
				update();
			} else if( src == xOffsetField ) {
				try {
					setXOffset(Double.parseDouble(xOffsetField.getText()));
				} catch (NumberFormatException e1) {
					setXOffset(xOffset);
				}
				update();
			} else if( src == yOffsetField ) {
				try {
					setYOffset(Double.parseDouble(yOffsetField.getText()));
				} catch (NumberFormatException e1) {
					setYOffset(yOffset);
				}
				update();
			} else if( src == thetaField ) {
				try {
					setTheta(Double.parseDouble(thetaField.getText()));
				} catch (NumberFormatException e1) {
					setTheta(theta);
				}
				update();
			}
		}

		private void writeDataFile() {
			try {
				BufferedWriter vpOut = new BufferedWriter( new java.io.FileWriter("victoria_park.rtl") );
				ControlPanel.writeVictoriaPark(laser, gps, vpOut);
				vpOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void telemetry() {
		this.setEnabled(false);
		this.setShowLast(true);
		this.gps = new GpsData(laser.getTS(0));
		
		for( int k = 1; k < laser.size(); k++ ) {
			Offset offset = localize(k-1, k);
			this.gps.addNext(laser.getTS(k), offset);
		}
		
		JOptionPane.showMessageDialog(this, "Telemetry is complete.");
		
		this.setEnabled(true);
	}
	
	private Offset localize( int prev, int next ) {
		if( prev < 0 || next >= laser.size() ) {
			return new Offset();
		}

		this.setEnabled(false);
		setTime(laser.getTS(next));
		setPrevCount(next-prev);
		
		Random r = RandomSingleton.instance;
		Offset[] g = new Offset[30];
		for (int i = 0; i < g.length; i++) {
			g[i] = new Offset(r, 50, 400, 0.5);
		}

		LaserDatum l1 = laser.getSample(prev);
		LaserDatum l2 = laser.getSample(next);

		for( int k = 0; k < 60; k++ ) {
			// check fitness
			for (int i = 0; i < g.length; i++) {
				g[i].fit = l2.getError(l1, g[i].t, g[i].x, g[i].y);
			}
			Arrays.sort(g);
			setXOffset(g[0].x);
			setYOffset(g[0].y);
			setTheta(g[0].t);
//			System.out.println("localize " + k + "(" + g[0].fit + ", " + g[g.length-1].fit + ")");
			update();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
			if( g[0].fit < 0.1 ) {
				break;
			}
			// make new individuals
			for (int i = g.length/3; i < g.length; i++ ) {
				int p1 = r.nextInt(g.length/3);
				int p2 = r.nextInt(g.length/3);
				g[i] = new Offset(g[p1], g[p2], r);
			}
		}
		
		this.setEnabled(true);
		return g[0];
	}

	public static void writeVictoriaPark(
		LaserData laser,
		GpsData gps,
		BufferedWriter vpOut)
		throws IOException {
		vpOut.write(
			"P 5 "
				+ gps.getMaxSpeed()
				+ " "
				+ gps.getMaxTurn()
				+ " 0.0 80 361");
		vpOut.newLine();
		int l = 0;
		int g = 0;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(4);
		nf.setGroupingUsed(false);
		while (l < laser.size() || g < gps.size()) {
			if (l < laser.size() && g < gps.size()) {
				double tl = laser.getTS(l);
				double tg = gps.getTS(g);
				//System.out.print("tl:"+tl+"\ttg"+tg+"\t");
				if (tl < tg) {
					vpOut.write("L " + laser.getCarmenFormat(l, nf));
					l++;
					//					System.out.println("printing laser");
				} else {
					vpOut.write("O " + gps.getCarmenFormat(g, nf));
					g++;
					//					System.out.println("printing gps");
				}
			} else if (l < laser.size()) {
				vpOut.write("L " + laser.getCarmenFormat(l, nf));
				l++;
			} else {
				vpOut.write("O " + gps.getCarmenFormat(g, nf));
				g++;
			}
			vpOut.newLine();
		}
	}
}
