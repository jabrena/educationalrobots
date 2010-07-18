import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.*;
import java.awt.event.*;

public class JListExample extends JFrame{

	JList list;
	String[] listColorNames = {"black", "blue", "green", "yellow", "white"};
	Color[] listColorValues = {Color.BLACK, Color.BLUE, Color.GREEN,
							Color.YELLOW, Color.WHITE};
	Container contentpane;
	public JListExample(){
		super("List Source Demo");
		contentpane = getContentPane();
		contentpane.setLayout(new FlowLayout());
		list = new JList(listColorNames);
		list.setSelectedIndex(0);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		contentpane.add(new JScrollPane(list));
		list.addListSelectionListener(
		     new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e){
			   contentpane.setBackground(listColorValues
					   					[list.getSelectedIndex()]);
				}
		     }
		);
		
		setSize(200,200);
		setVisible(true);
		
	}
	
	public static void main(String[] args)
	{
		JListExample test = new JListExample();
		test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	
}