package vp.ui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author oursland
 */
public class SimulationControlPanel extends JPanel {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 6307069733451176831L;
	private JButton startButton = new JButton("Start/Stop");
	private JButton stepButton = new JButton("Step");
	private JLabel particleCountLabel = new JLabel("xxxx particles");
	private JTextField mapField = new JTextField(4);
	private JButton prevMapButton = new JButton("<");
	private JButton nextMapButton = new JButton(">");
	private JLabel scoreLabel = new JLabel("Score xxxxxxxxxxxxxxx");
	private JLabel landmarkCountLabel = new JLabel("Landmarks xxxx");

	private boolean running = false;
	private Object stepSemaphore = new Object();
	
	private final ParticleFilterPanel filterPanel;

	private ActionListener l = new SimCPActionListener();

	public SimulationControlPanel(ParticleFilterPanel filterPanel) {
		this.filterPanel = filterPanel;
		setLayout(new GridLayout(6,1));
		add(startButton);
		add(stepButton);
		add(particleCountLabel);
		
		JPanel mapPanel = new JPanel();
		mapPanel.add(new JLabel("Display Map "));
		mapPanel.add(mapField);
		mapField.setText(Integer.toString(filterPanel.getDisplayIndex()));
		mapPanel.add(prevMapButton);
		mapPanel.add(nextMapButton);
		add(mapPanel);
		add(landmarkCountLabel);
		add(scoreLabel);

		particleCountLabel.setText( filterPanel.getFilterManager().getFilter().size() + " particles");

		startButton.addActionListener(l);
		stepButton.addActionListener(l);
		prevMapButton.addActionListener(l);
		nextMapButton.addActionListener(l);
	}

	public void testRunning() throws InterruptedException {
		updateData();
		synchronized (stepSemaphore) {
			if (!running) {
				stepSemaphore.wait();
			} else {
//				Thread.sleep(30);
			}
		}
	}

	private void updateData() {
		mapField.setText(Integer.toString(filterPanel.getDisplayIndex()));
		scoreLabel.setText("Map Score " + filterPanel.getDisplayMap().getImportanceWeight());
		landmarkCountLabel.setText("Landmarks " + filterPanel.getDisplayMap().getLandmarkCount());
	}
	
	private class SimCPActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			synchronized (stepSemaphore) {
				Object src = e.getSource();
				if (src == startButton) {
					running = !running;
					stepSemaphore.notify();
				} else if (src == stepButton) {
					stepSemaphore.notify();
				} else if (src == prevMapButton) {
					filterPanel.setDisplayIndex(filterPanel.getDisplayIndex()-1);
				} else if (src == nextMapButton) {
					filterPanel.setDisplayIndex(filterPanel.getDisplayIndex()+1);
				}
			}
			updateData();
		}
	}
	
}
