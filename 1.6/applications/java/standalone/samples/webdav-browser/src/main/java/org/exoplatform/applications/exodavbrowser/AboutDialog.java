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

public class AboutDialog extends javax.swing.JDialog {
  private JPanel jPanel1;
  private JLabel jl_version;
  private JLabel jLabel3;
  private JPanel jPanel6;
  private JPanel jPanel5;
  private JLabel jLabel2;
  private JLabel jl_name;
  private JLabel jl_copy;
  private JPanel jPanel4;
  private JLabel jLabel1;
  private JPanel jPanel3;
  private JPanel jPanel2;
  private JButton jb_ok;
  private int x;
  private int y;
  
  public AboutDialog(JFrame frame) {
    super(frame);
    
    y = (frame.getHeight() - frame.getLocation().y)/2 - 102;
    x = (frame.getWidth() - frame.getLocation().x)/2 - 200;
    
    initGUI();
  }
  
  private void initGUI() {
    try {
      {
        this.setTitle("About DAVeXplorer");
      }
      {
        jPanel1 = new JPanel();
        FlowLayout jPanel1Layout = new FlowLayout();
        jPanel1.setLayout(jPanel1Layout);
        getContentPane().add(jPanel1, BorderLayout.SOUTH);
        {
          jLabel2 = new JLabel();
          jPanel1.add(jLabel2);
          jLabel2.setText(" ");
          jLabel2.setPreferredSize(new java.awt.Dimension(317, 14));
        }
        {
          jb_ok = new JButton();
          jPanel1.add(jb_ok);
          jb_ok.setText("OK");
        }
      }
      {
        jPanel2 = new JPanel();
        BorderLayout jPanel2Layout = new BorderLayout();
        getContentPane().add(jPanel2, BorderLayout.WEST);
        jPanel2.setLayout(jPanel2Layout);
        {
          jPanel3 = new JPanel();
          BorderLayout jPanel3Layout = new BorderLayout();
          jPanel3.setLayout(jPanel3Layout);
          jPanel2.add(jPanel3, BorderLayout.NORTH);
          {
            jLabel1 = new JLabel();
            jPanel3.add(jLabel1, BorderLayout.CENTER);
            jLabel1.setIcon(new DAVConst().getImage(DAVConst.Image.sExologo));
          }
        }
        {
          jPanel4 = new JPanel();
          BorderLayout jPanel4Layout = new BorderLayout();
          jPanel4.setLayout(jPanel4Layout);
          jPanel2.add(jPanel4, BorderLayout.CENTER);
          {
            jPanel5 = new JPanel();
            BorderLayout jPanel5Layout = new BorderLayout();
            jPanel4.add(jPanel5, BorderLayout.WEST);
            jPanel5.setLayout(jPanel5Layout);
            {
              jLabel3 = new JLabel();
              jPanel5.add(jLabel3, BorderLayout.CENTER);
              jLabel3.setText("       ");
            }
          }
          {
            jPanel6 = new JPanel();
            jPanel4.add(jPanel6, BorderLayout.CENTER);
            BorderLayout jPanel6Layout = new BorderLayout();
            jPanel6.setLayout(jPanel6Layout);
            {
              jl_name = new JLabel();
              jPanel6.add(jl_name, BorderLayout.NORTH);
              jl_name.setText(DAVConst.Info.sTitle);
              jl_name.setPreferredSize(new java.awt.Dimension(390, 22));
            }
            {
              jl_copy = new JLabel();
              jPanel6.add(jl_copy, BorderLayout.SOUTH);
              jl_copy.setText(DAVConst.Info.sCopyright);
              jl_copy.setPreferredSize(new java.awt.Dimension(358, 22));
            }
            {
              jl_version = new JLabel();
              jPanel6.add(jl_version, BorderLayout.CENTER);
              jl_version.setText(DAVConst.Info.sVersion);
            }
          }
        }
      }
      
      /*EVENT*/
      jb_ok.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          setVisible(false);
        }
      });
      
      this.setSize(399, 205);
      this.setLocation(x, y);
      this.setResizable(false);
      this.setModal(true);
      this.setVisible(true);
           
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
