/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.applications.exodavbrowser;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Created by The eXo Platform SARL
 * Author : Alex Reshetnyak
 *          alex_reshetnyak@yahoo.com
 * ${date}  
 */

public class CreateFolderDialog extends JDialog {
  private JPanel jPanel1;
  private JPanel jp_field;
  private JButton jb_cancel;
  private JPanel jp_buttons;
  private JButton jb_Ok;
  private JTextField jtf_folder_name;
  private JLabel jLabel1;
  private int x;
  private int y;
  public String sFolderName;
  

  /**
  * Auto-generated main method to display this JDialog
  */
  public static void main(String[] args) {
    JFrame frame = new JFrame();
    CreateFolderDialog inst = new CreateFolderDialog(frame);
    inst.setVisible(true);
  }
  
  public CreateFolderDialog(JFrame frame) {
    super(frame);
    
    y = (frame.getHeight() - frame.getLocation().y)/2 - 63;
    x = (frame.getWidth() - frame.getLocation().x)/2 - 132; 
    
    initGUI();
  }
  
  private void initGUI() {
    try {
      {
        sFolderName = new String();
        BoxLayout thisLayout = new BoxLayout(getContentPane(), BoxLayout.X_AXIS);
        getContentPane().setLayout(thisLayout);
        this.setTitle("Create Folder");
        this.setPreferredSize(new java.awt.Dimension(265, 126));
        this.setFont(new java.awt.Font("Tahoma",1,11));
        {
          jPanel1 = new JPanel();
          BoxLayout jPanel1Layout = new BoxLayout(jPanel1, BoxLayout.Y_AXIS);
          jPanel1.setLayout(jPanel1Layout);
          getContentPane().add(jPanel1);
          jPanel1.setPreferredSize(new java.awt.Dimension(238, 54));
          {
            jp_field = new JPanel();
            jPanel1.add(jp_field);
            {
              jLabel1 = new JLabel();
              jp_field.add(jLabel1);
              jLabel1.setText("Enter folder name:");
              jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12));
              jLabel1.setPreferredSize(new Dimension(240, 20));
            }
            {
              jtf_folder_name = new JTextField();
              jp_field.add(jtf_folder_name);
              jtf_folder_name.setPreferredSize(new Dimension(240, 20));
            }
          }
          {
            jp_buttons = new JPanel();
            jPanel1.add(jp_buttons);
            jp_buttons.setPreferredSize(new Dimension(257, 15));
            {
              jb_Ok = new JButton();
              jp_buttons.add(jb_Ok);
              jb_Ok.setText("OK");
            }
            {
              jb_cancel = new JButton();
              jp_buttons.add(jb_cancel);
              jb_cancel.setText("Cancel");
            }
          }
          
          /*EVENT*/
          jb_Ok.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
              sFolderName = jtf_folder_name.getText();
              setVisible(false);
            }
          });
          
          jb_cancel.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
              setVisible(false);
            }
          });
          
        }
      }
      this.setSize(265, 126);
      this.setLocation(x, y);
      this.setResizable(false);
      this.setModal(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
