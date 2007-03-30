/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.applications.exodavbrowser;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Created by The eXo Platform SARL
 * Author : Alex Reshetnyak
 *          alex_reshetnyak@yahoo.com
 * ${date}  
 */

public class MessagerDialog extends javax.swing.JDialog {
  private JPanel jPanel1;
  private JButton jButton1;
  private JLabel jLabel2;
  private JLabel jLabel1;
  private JPanel jPanel2;
  private String sMessage;

  public MessagerDialog(JFrame jf, String msg) {
    super(jf);

    sMessage = msg;
    
    initGUI();
  }
  
  private void initGUI() {
    try {
      {
        this.setTitle("Message");
      }
      {
        jPanel1 = new JPanel();
        BorderLayout jPanel1Layout = new BorderLayout();
        getContentPane().add(jPanel1, BorderLayout.CENTER);
        jPanel1.setLayout(jPanel1Layout);
        {
          jLabel1 = new JLabel();
          jPanel1.add(jLabel1, BorderLayout.CENTER);
          jLabel1.setText(sMessage);
        }
        {
          jLabel2 = new JLabel();
          BorderLayout jLabel2Layout = new BorderLayout();
          jPanel1.add(jLabel2, BorderLayout.WEST);
          jLabel2.setIcon(new DAVConst().getImage(DAVConst.Image.sMsgIcon));
          jLabel2.setLayout(null);
          jLabel2.setPreferredSize(new java.awt.Dimension(54, 74));
        }
      }
      {
        jPanel2 = new JPanel();
        FlowLayout jPanel2Layout = new FlowLayout();
        getContentPane().add(jPanel2, BorderLayout.SOUTH);
        jPanel2.setLayout(jPanel2Layout);
        {
          jButton1 = new JButton();
          jPanel2.add(jButton1);
          jButton1.setText("OK");
          
          jButton1.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
              setVisible(false);
            }
          });
        }
      }
      this.setSize(250, 133);
      this.setLocation(400,300);
      this.setResizable(false);
      this.setModal(true);
      this.setVisible(true);
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}

