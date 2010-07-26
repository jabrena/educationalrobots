import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFrame;

import vp.VPConstant;
import vp.data.GpsData;
import vp.data.GpsDatum;
import vp.data.LaserData;
import vp.data.LaserDatum;
import vp.data.TimeData;
import vp.ui.ControlPanel;
import vp.ui.ImagePanel;
import vp.ui.LandmarkPanel;

public class VictoriaPark {

	public static void main(String[] args) {
		try {
			FileReader fr;
			fr = new FileReader("TLsr.dlm");
			TimeData Tlsr = new TimeData(new BufferedReader(fr));
			fr.close();

			fr = new FileReader("time.dlm");
			TimeData time = new TimeData(new BufferedReader(fr));
			fr.close();

			fr = new FileReader("timeGps.dlm");
			TimeData timeGps = new TimeData(new BufferedReader(fr));
			fr.close();

			BufferedReader laserIn =
				new BufferedReader(new FileReader("LASER.dlm"));
			LaserData laser = new LaserData(Tlsr, laserIn);
			laserIn.close();

			BufferedReader latIn =
				new BufferedReader(new FileReader("La_m.dlm"));
			BufferedReader lonIn =
				new BufferedReader(new FileReader("Lo_m.dlm"));
			GpsData gps = new GpsData(timeGps, latIn, lonIn);
			latIn.close();
			lonIn.close();
			
			/*
						BufferedWriter vpOut = new BufferedWriter( new java.io.FileWriter("victoria_park.rtl") );
						writeVictoriaPark(laser, gps, vpOut);
						vpOut.close();
			*/

			BufferedImage image =
				new BufferedImage(
					VPConstant.imageWidth,
					VPConstant.imageHeight,
					BufferedImage.TYPE_INT_ARGB);
			//			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			//			GraphicsDevice gs = ge.getDefaultScreenDevice();
			//			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			// Create an image that supports transparent pixels
			//			BufferedImage image = gc.createCompatibleImage(imageWidth, imageHeight, Transparency.BITMASK);

			LaserDatum ls = laser.getSample(0);
			drawLocalSensorMap(image, ls);

			JFrame f = new JFrame("Local Sensor Map");
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.getContentPane().setLayout(new BorderLayout());

			ImagePanel imagePanel = new ImagePanel(image);
			LandmarkPanel pointPanel = new LandmarkPanel();
			f.getContentPane().add(pointPanel);

			f.setSize(VPConstant.imageWidth, VPConstant.imageHeight);
			f.setVisible(true);

			ControlPanel controlPanel = new ControlPanel(imagePanel, pointPanel);
			controlPanel.setData(laser, gps);

			JFrame controlFrame = new JFrame("Control Panel");
			controlFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			controlFrame.getContentPane().setLayout(new BorderLayout());
			controlFrame.getContentPane().add(
				controlPanel,
				BorderLayout.CENTER);
			controlFrame.setBounds(f.getWidth(), 0, 300, 600);
			controlFrame.pack();
			controlFrame.setVisible(true);
/*
			Graphics2D g = image.createGraphics();
			g.setColor(Color.white);
			try {
				Thread.sleep(2000);
				g.fillRect(0, 0, imageWidth, imageHeight);
				for( int i = 0; i < gps.size(); i++ ) {
					ls = laser.getSample(i);
//					g.fillRect(0, 0, imageWidth, imageHeight);
//					drawLocalSensorMap(image, ls);
					drawGpsReading(image, i, gps);
					imagePanel.repaint();
					Thread.sleep(10);
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			g.dispose();
*/
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void drawGpsReading(BufferedImage image, int i, GpsData gps) {
		GpsDatum s = gps.getSample(i);
		int x =
			(int) Math.max(
				1,
				Math.min(
					VPConstant.imageWidth - 1,
					Math.round(
						s.x / 0.5 + (VPConstant.imageWidth / 2.0))));
		int y =
			(int) Math.max(
				1,
				Math.min(
					VPConstant.imageHeight - 1,
					Math.round(
						s.y / 0.5 + (VPConstant.imageHeight / 2.0))));
		drawBlock(x, y, image);
	}


	private static void drawLocalSensorMap(
		BufferedImage image,
		LaserDatum ls) {
		for (int k = 0; k < ls.size(); k++) {
			Point2D p = ls.getReadingLocation(k);
			if (p.distance(0.0, 0.0) < 3000) {
				int x =
					(int) Math.max(
						1,
						Math.min(
							VPConstant.imageWidth - 1,
							Math.round(
								p.getX() / VPConstant.scaleDown + (VPConstant.imageWidth / 2.0))));
				int y =
					(int) Math.max(
						1,
						Math.min(
							VPConstant.imageHeight - 1,
							Math.round(
								p.getY() / VPConstant.scaleDown + (VPConstant.imageHeight / 10.0))));
				drawBlock(x, y, image);
			}
			//			image.setRGB(x, y, Color.black.getRGB());
		}
	}

	private static void drawBlock(int x, int y, BufferedImage image) {
		image.setRGB(x, y, Color.black.getRGB());
		image.setRGB(x - 1, y, Color.black.getRGB());
		image.setRGB(x, y - 1, Color.black.getRGB());
		image.setRGB(x - 1, y - 1, Color.black.getRGB());
	}

}
