import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class JTextFieldExample2 extends JFrame implements ActionListener {
	JTextField jtfInput;

	JTextArea jtAreaOutput;

	String newline = "\n";

	public JTextFieldExample2() {
		createGui();
	}

	public void createGui() {
		jtfInput = new JTextField(20);
		jtfInput.addActionListener(this);

		jtAreaOutput = new JTextArea(5, 20);
		jtAreaOutput.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(jtAreaOutput,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		GridBagLayout gridBag = new GridBagLayout();
		Container contentPane = getContentPane();
		contentPane.setLayout(gridBag);
		
		GridBagConstraints gridBagConstraintsx1 = new GridBagConstraints();
        gridBagConstraintsx1.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraintsx1.fill = GridBagConstraints.HORIZONTAL;
        contentPane.add(jtfInput, gridBagConstraintsx1);
        
        GridBagConstraints gridBagConstraintsx2 = new GridBagConstraints();
        gridBagConstraintsx2.weightx = 1.0;
        gridBagConstraintsx2.weighty = 1.0;
        contentPane.add(scrollPane, gridBagConstraintsx2);

	}

	public void actionPerformed(ActionEvent evt) {
		String text = jtfInput.getText();
		jtAreaOutput.append(text + newline);
		jtfInput.selectAll();
	}
	
	   public static void main(String[] args) {
		   JTextFieldExample2 jtfTfDemo = new JTextFieldExample2();		
	        jtfTfDemo.pack();
	        jtfTfDemo.addWindowListener(new WindowAdapter() {
	            public void windowClosing(WindowEvent e) {
	                System.exit(0);
	            }
	        });
	        jtfTfDemo.setVisible(true);
	    }
}