//Create a Draggable JWindow

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class JWindowExample extends JWindow
    {
    
     private int X=0;
     private int Y=0;
    
     public JWindowExample(){
        
         setBounds(60,60,100,100);
        
         addWindowListener(new WindowAdapter(){
             public void windowClosing(WindowEvent e){
                 System.exit(0);	//An Exit Listener
             }
         });
        
         //Print (X,Y) coordinates on Mouse Click
         addMouseListener(new MouseAdapter() {
             public void mousePressed(MouseEvent e){
                 X=e.getX();
                 Y=e.getY();
                 System.out.println("The (X,Y) coordinate of window is ("+X+","+Y+")");
             }
         });
        
         addMouseMotionListener(new MouseMotionAdapter()
             {
             public void mouseDragged(MouseEvent e){
                 setLocation(getLocation().x+(e.getX()-X),getLocation().y+(e.getY()-Y));
             }
         });
        
         setVisible(true);
     }
     public static void main(String[] args){
         new JWindowExample();
     }
}