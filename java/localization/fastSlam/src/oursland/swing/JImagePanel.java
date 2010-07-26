package oursland.swing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JPanel;

/**
 * @author oursland
 */
public class JImagePanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -1635828426068721494L;
	private Image image = null;
	
	public JImagePanel() {
		setOpaque(true);
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		if( this.image != null ) {
			g.drawImage(image, 0, 0, this);			
		}
	}

	public void setImage(Image image) {
		this.image = image;
		if( image != null ) {
			int w = image.getWidth(null);
			while(w == -1) {
				w = image.getWidth(null);
			}
			int h = image.getHeight(null);
			while( h == -1 ) {
				h = image.getHeight(null);
			}
			Dimension d = new Dimension(w, h);
			setPreferredSize(d);
			repaint();
		}
	}
}
