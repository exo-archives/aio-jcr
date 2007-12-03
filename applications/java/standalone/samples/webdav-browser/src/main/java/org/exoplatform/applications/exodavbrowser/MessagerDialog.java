/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
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
 * Created by The eXo Platform SAS
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

