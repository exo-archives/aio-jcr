/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.applications.exodavbrowser;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Created by The eXo Platform SARL
 * Author : Alex Reshetnyak
 *          alex_reshetnyak@yahoo.com
 * ${date}  
 */

public class LogInDialog extends javax.swing.JDialog {
  private JPanel jPanel1;
  private JButton jb_ok;
  private JPanel jPanel7;
  private JPasswordField jPF_password;
  private JTextField jtf_login;
  private JLabel jLabel1;
  private JButton jb_cancel;
  private JLabel jLabel3;
  private JLabel jLabel2;
  private JPanel jPanel2;
  private int x;
  private int y;
  private boolean bCancel;
  private boolean bOk;

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    LogInDialog inst = new LogInDialog(frame);
    inst.setVisible(true);
  }
  
  public LogInDialog(JFrame frame) {
    super(frame);
    
    y = (frame.getHeight() - frame.getLocation().y)/2 - 63;
    x = (frame.getWidth() - frame.getLocation().x)/2 - 138; 
    
    initGUI();
  }
  
  private void initGUI() {
    try {
      BoxLayout thisLayout = new BoxLayout(getContentPane(), BoxLayout.Y_AXIS);
      getContentPane().setLayout(thisLayout);
      this.setTitle("Login");
      {
        jPanel1 = new JPanel();
        getContentPane().add(jPanel1);
        FlowLayout jPanel1Layout = new FlowLayout();
        jPanel1.setLayout(jPanel1Layout);
        {
          jLabel3 = new JLabel();
          jPanel1.add(jLabel3);
          jLabel3.setText("   ");
          jLabel3.setPreferredSize(new java.awt.Dimension(17, 14));
        }
        {
          jLabel1 = new JLabel();
          jPanel1.add(jLabel1);
          jLabel1.setText("Login:");
          jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
          jLabel1.setVerticalTextPosition(SwingConstants.BOTTOM);
          jLabel1.setPreferredSize(new java.awt.Dimension(35, 22));
        }
        {
          jtf_login = new JTextField();
          jPanel1.add(jtf_login);
          jtf_login.setPreferredSize(new java.awt.Dimension(175, 20));
        }
      }
      {
        jPanel2 = new JPanel();
        FlowLayout jPanel2Layout = new FlowLayout();
        getContentPane().add(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        {
          jLabel2 = new JLabel();
          jPanel2.add(jLabel2);
          jLabel2.setText("Password:");
          jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11));
        }
        {
          jPF_password = new JPasswordField();
          jPanel2.add(jPF_password);
          jPF_password.setPreferredSize(new java.awt.Dimension(175, 20));
        }
      }
      {
        jPanel7 = new JPanel();
        getContentPane().add(jPanel7);
        {
          jb_ok = new JButton();
          jPanel7.add(jb_ok);
          jb_ok.setText("OK");
        }
        {
          jb_cancel = new JButton();
          jPanel7.add(jb_cancel);
          jb_cancel.setText("Cancel");
        }
        
        /*EVENT*/
        jb_ok.addActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent e){
            bOk = true;
            bCancel = false;
            
            setVisible(false);
          }
        });
        
        jb_cancel.addActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent e){
            bOk = false;
            bCancel = true;
            
            setVisible(false);
          }
        });
        
      }
      this.setSize(277, 126);
      this.setLocation(x, y);
      this.setResizable(false);
      this.setModal(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public boolean isOK(){
    return bOk;
  }
  
  public boolean isCancel(){
    return bCancel;
  }
  
  public String getUserID(){
    return jtf_login.getText();
  }
  
  public String getPassword(){
    return jPF_password.getText();
  }

}
