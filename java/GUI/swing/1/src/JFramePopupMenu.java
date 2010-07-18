import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
 
public class JFramePopupMenu extends JFrame  {
	private JPanel jContentPane = null;
	private JButton jbnPopup = null;
	private JTextField jtfNumOfMenus = null;
	private JLabel lblNumElem = null;
    private XJPopupMenu scrollablePopupMenu = new XJPopupMenu(this);
    
    private JButton getBtnPopup() {
        if (jbnPopup == null) {
            jbnPopup = new JButton();
            jbnPopup.setText("View Scrollable popup menu ");
            int n = Integer.parseInt(getTxtNumElem().getText());
            for (int i=0;i<n;i++){
            	XCheckedButton xx = new XCheckedButton(" JMenuItem  " + (i+1));
                xx.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        System.out.println( e );
                        scrollablePopupMenu.hidemenu();
                    }
                });
                // Add Custom JSeperator after 2nd and 7th MenuItem.
                if(i == 2 || i == 7){
                	scrollablePopupMenu.addSeparator();
                }
                scrollablePopupMenu.add(xx);
            }
            
            
            jbnPopup.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                	Component source = (Component) e.getSource();
                	scrollablePopupMenu.show(source, e.getX(), e.getY());
				}
            });
        }
        return jbnPopup;
    }
    
 
	private JTextField getTxtNumElem() {
		if (jtfNumOfMenus == null) {
			jtfNumOfMenus = new JTextField();
			jtfNumOfMenus.setColumns(3);
			jtfNumOfMenus.setText("60");
		}
		return jtfNumOfMenus;
	}
 
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFramePopupMenu thisClass = new JFramePopupMenu();
				thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				thisClass.setVisible(true);
			}
		});
	}
 
	public JFramePopupMenu() {
		super();
		initialize();
	}
 
	private void initialize() {
		this.setSize(274, 109);
		this.setContentPane(getJContentPane());
		this.setTitle(" Scrollable JPopupMenu ");
	}
 
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			lblNumElem = new JLabel();
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setHgap(8);
			flowLayout.setVgap(8);
			jContentPane = new JPanel();
			jContentPane.setLayout(flowLayout);
			jContentPane.add(getBtnPopup(), null);
			jContentPane.add(lblNumElem, null);
			jContentPane.add(getTxtNumElem(), null);
		}
		return jContentPane;
	}
}
