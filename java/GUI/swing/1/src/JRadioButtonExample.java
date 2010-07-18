import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class JRadioButtonExample extends JPanel {
    
	static JFrame frame;

    JLabel jlbPicture;
    RadioListener myListener = null;
    public JRadioButtonExample() {
    	
    	
    	
        // Create the radio buttons and assign Keyboard shortcuts using Mnemonics
        JRadioButton jrbNumbers = new JRadioButton("Numbers");
        jrbNumbers.setMnemonic(KeyEvent.VK_N);
        jrbNumbers.setActionCommand("numbers");
        jrbNumbers.setSelected(true);

        JRadioButton jrbAlphabets = new JRadioButton("Alphabets");
        jrbAlphabets.setMnemonic(KeyEvent.VK_A);
        jrbAlphabets.setActionCommand("alphabets");

        JRadioButton jrbSymbols = new JRadioButton("Symbols");
        jrbSymbols.setMnemonic(KeyEvent.VK_S);
        jrbSymbols.setActionCommand("symbols");

        // Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        group.add(jrbNumbers);
        group.add(jrbAlphabets);
        group.add(jrbSymbols);

        // Register an action listener for the radio buttons.
        myListener = new RadioListener();
        jrbNumbers.addActionListener(myListener);
        jrbAlphabets.addActionListener(myListener);
        jrbSymbols.addActionListener(myListener);


        // Set up the picture label
        jlbPicture = new JLabel(new ImageIcon(""+"numbers" + ".jpg"));			//Set the Default Image

        jlbPicture.setPreferredSize(new Dimension(177, 122));


        // Put the radio buttons in a column in a panel
        JPanel jplRadio = new JPanel();
        jplRadio.setLayout(new GridLayout(0, 1));
        jplRadio.add(jrbNumbers);
        jplRadio.add(jrbAlphabets);
        jplRadio.add(jrbSymbols);


        setLayout(new BorderLayout());
        add(jplRadio, BorderLayout.WEST);
        add(jlbPicture, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
    }

    /** Listens to the radio buttons. */
    class RadioListener implements ActionListener { 
        public void actionPerformed(ActionEvent e) {
            jlbPicture.setIcon(new ImageIcon("./res/"+e.getActionCommand() 
                                          + ".jpg"));
        }
    }

    public static void main(String s[]) {
         frame = new JFrame("JRadioButton Usage Demo");
         frame.addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {System.exit(0);}
         });
 
         frame.getContentPane().add(new JRadioButtonExample(), BorderLayout.CENTER);
         frame.pack();
         frame.setVisible(true);
    }
}