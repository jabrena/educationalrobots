package vp.ui;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 322540000582049719L;
	final BufferedImage image;
	final Graphics2D imageGraphics;
	private double scale = 1.0;
	public ImagePanel(BufferedImage image) {
		super();
		this.image = image;
		this.imageGraphics = image.createGraphics();
		setOpaque(false);
	}

	public void paint(Graphics g) {
		super.paint(g);
//		int width = image.getWidth();
//		int height = image.getHeight();
//		if( width <= 0 || height <= 0 ) {
			g.drawImage(image, 0, 0, this);
//		} else {
//			g.drawImage(image, 0, 0, (int)(scale*width), (int)(scale*height), this);
//		}
	}

	public BufferedImage getImage() {
		return image;
	}

	public Graphics2D getImageGraphics() {
		return imageGraphics;
	}
}
