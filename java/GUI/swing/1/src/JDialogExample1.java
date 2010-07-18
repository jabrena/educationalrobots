import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.Dimension; 

public class JDialogExample1 implements ActionListener {
    JFrame mainFrame = null;
    JButton myButton = null;

    public JDialogExample1() {
        mainFrame = new JFrame("TestTheDialog Tester");
        mainFrame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {System.exit(0);}
            });
        myButton = new JButton("Test the dialog!");
        myButton.addActionListener(this);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.getContentPane().add(myButton);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }
    
    public void actionPerformed(ActionEvent e) {
        if(myButton == e.getSource()) {
            System.err.println("Opening dialog.");
            CustomDialog myDialog = new CustomDialog(mainFrame, true, "Do you like Java?");
            System.err.println("After opening dialog.");
            if(myDialog.getAnswer()) {
                System.err.println("The answer stored in CustomDialog is 'true' (i.e. user clicked yes button.)");
            }
            else {
                System.err.println("The answer stored in CustomDialog is 'false' (i.e. user clicked no button.)");
            }
        }
    }

    public static void main(String argv[]) {

        JDialogExample1 tester = new JDialogExample1();
    }
}
