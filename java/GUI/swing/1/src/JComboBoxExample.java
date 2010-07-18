import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class JComboBoxExample extends JPanel {
    JLabel jlbPicture;

    public JComboBoxExample() {
        String[] comboTypes = { "Numbers", "Alphabets", "Symbols"};

        // Create the combo box, and set 2nd item as Default
        JComboBox comboTypesList = new JComboBox(comboTypes);
        comboTypesList.setSelectedIndex(2);
        comboTypesList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox jcmbType = (JComboBox)e.getSource();
                String cmbType = (String)jcmbType.getSelectedItem();
                jlbPicture.setIcon(new ImageIcon("./res/"+cmbType.trim().toLowerCase() + ".jpg"));
            }
        });

        // Set up the picture
        jlbPicture = new JLabel(new ImageIcon("" +
                                   comboTypes[comboTypesList.getSelectedIndex()] +
                                   ".jpg"));
        jlbPicture.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));

        jlbPicture.setPreferredSize(new Dimension(177, 122+10));

        // Layout the demo
        setLayout(new BorderLayout());
        add(comboTypesList, BorderLayout.NORTH);
        add(jlbPicture, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
    }

    public static void main(String s[]) {
        JFrame frame = new JFrame("JComboBox Usage Demo");

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });
 
        frame.setContentPane(new JComboBoxExample());
        frame.pack();
        frame.setVisible(true);
    }
}