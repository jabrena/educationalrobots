package vp.ui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class LandmarkPanel extends JPanel {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 5334085300151840399L;
	Hashtable<Color, Point2D[]> pointMap = new Hashtable<Color, Point2D[]>();
//	Point2D points[] = new Point2D[0];
	double pointRadius = 5;
	
	public void setPoints( Color color, Point2D[] pts ) {
		if(pts != null) {
			pointMap.put(color, pts);
		} else {
			pointMap.remove(color);
		}
		repaint();
		setOpaque(true);
		setBackground(Color.white);
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		
		g2.translate(getWidth()/2.0, getHeight()/2.0);
		
		double pointDiameter = 2*pointRadius;
		
		g.setColor(Color.black);
		g.drawLine(0, (int) -pointRadius, 0, (int) pointRadius);
		g.drawLine((int) -pointRadius, 0, (int) pointRadius, 0);

		Enumeration keys = pointMap.keys();
		while( keys.hasMoreElements() ) {
			Color color = (Color) keys.nextElement();
			Point2D[] points = pointMap.get(color);
			for (int i = 0; i < points.length; i++) {
				int x = (int) (points[i].getX()/10 - pointRadius);
				int y = (int) (points[i].getY()/10 - pointRadius);
				int w = (int) pointDiameter;
				int h = (int) pointDiameter;			
				g.setColor(color);
				g.fillOval(x, y, w, h);
				g.setColor(Color.black);
				g.drawOval(x, y, w, h);
	//			System.out.println("Drawing point " + points[i]);
			}
		}
		
	}

	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(new LandmarkPanel(), BorderLayout.CENTER);
		f.setSize(500,500);
		f.setVisible(true);
	}

}
