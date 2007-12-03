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
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

/**
 * Created by The eXo Platform SAS
 * Author : Alex Reshetnyak
 *          alex_reshetnyak@yahoo.com
 * ${date}  
 */

public class VersionReport extends javax.swing.JDialog {
  {
    //Set Look & Feel
    try {
      javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  private JPanel jPanel1;
  private JList jList1;
  private JPanel jPanel6;
  private JLabel jLabel1;
  private JPanel jPanel5;
  private JButton jb_Close;
  private JPanel jPanel4;
  private JPanel jPanel3;
  private JButton jb_SaveAs;
  private JScrollPane jScrollPane1;
  private JPanel jPanel2;
  private int x;
  private int y;
  private Vector vVersionList;
  private JFrame jFrame;
  private DAVAdapter davAdapter;
  


  public static void main(String[] args) {
    JFrame frame = new JFrame();
    VersionReport inst = new VersionReport(frame, new Vector(), new DAVAdapter());
    inst.setVisible(true);
  }
  
  public VersionReport(JFrame frame, Vector VersionList, DAVAdapter da ) {
    super(frame);
    jFrame = frame;
    davAdapter = da;   
    vVersionList = VersionList;
    
    y = (frame.getHeight() - frame.getLocation().y)/2 - 152;
    x = (frame.getWidth() - frame.getLocation().x)/2 - 181; 
    
    initGUI();
  }
  
  private void initGUI() {
    try {
      BorderLayout thisLayout = new BorderLayout();
      getContentPane().setLayout(thisLayout);
      this.setTitle("Version list");
      {
        jPanel1 = new JPanel();
        BorderLayout jPanel1Layout = new BorderLayout();
        jPanel1.setLayout(jPanel1Layout);
        getContentPane().add(jPanel1, BorderLayout.CENTER);
        {
          jPanel5 = new JPanel();
          BorderLayout jPanel5Layout = new BorderLayout();
          jPanel1.add(jPanel5, BorderLayout.NORTH);
          jPanel5.setLayout(jPanel5Layout);
          {
            jLabel1 = new JLabel();
            jPanel5.add(jLabel1, BorderLayout.CENTER);
            BorderLayout jLabel1Layout = new BorderLayout();
            jLabel1.setText("  Available versions:");
            jLabel1.setLayout(null);
            jLabel1.setPreferredSize(new java.awt.Dimension(258, 20));
          }
        }
        {
          jPanel6 = new JPanel();
          BorderLayout jPanel6Layout = new BorderLayout();
          jPanel1.add(jPanel6, BorderLayout.CENTER);
          jPanel6.setLayout(jPanel6Layout);
          {
            jScrollPane1 = new JScrollPane();
            jPanel6.add(jScrollPane1, BorderLayout.CENTER);
            jScrollPane1.setPreferredSize(new java.awt.Dimension(355, 256));
            {
              ListModel jList1Model = new DefaultComboBoxModel(getList());
              jList1 = new JList();
              jScrollPane1.setViewportView(jList1);
              FlowLayout jList1Layout = new FlowLayout();
              jList1.setModel(jList1Model);
              jList1.setLayout(null);
            }
          }
          {
            jPanel2 = new JPanel();
            jPanel6.add(jPanel2, BorderLayout.EAST);
            BorderLayout jPanel2Layout = new BorderLayout();
            jPanel2.setLayout(jPanel2Layout);
            {
              jPanel4 = new JPanel();
              jPanel2.add(jPanel4, BorderLayout.CENTER);
              FlowLayout jPanel4Layout = new FlowLayout();
              jPanel4.setLayout(jPanel4Layout);
              {
                jb_Close = new JButton();
                jPanel4.add(jb_Close);
                jb_Close.setText("Close");
                jb_Close.setSize(87, 22);
                jb_Close.setPreferredSize(new java.awt.Dimension(87, 22));
              }
            }
            {
              jPanel3 = new JPanel();
              jPanel2.add(jPanel3, BorderLayout.NORTH);
              FlowLayout jPanel3Layout = new FlowLayout();
              jPanel3.setLayout(jPanel3Layout);
              {
                jb_SaveAs = new JButton();
                jPanel3.add(jb_SaveAs);
                jb_SaveAs.setText("Save as ...");
              }
            }

            jPanel2.setLayout(jPanel2Layout);
          }
          
          jb_Close.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
              setVisible(false);
            }
          });
          
          jb_SaveAs.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
              int selVersion = jList1.getSelectedIndex();
              if ( selVersion != -1 ){
                Vector vTemp = (Vector)vVersionList.get(selVersion);
                String href = (String)vTemp.get(1);
                Log.info("HREF SELECTION -->" + vTemp.get(1));
                
                
                FileDialog fd = new FileDialog(jFrame, "Save As ...", FileDialog.SAVE);
                fd.setFile("Version.txt");
                fd.setModal(true);
                fd.setVisible(true);
                
                String dir = fd.getDirectory();
                if( (dir == null) || dir.equals("") )
                    return;
                String fname = fd.getFile();
                if( (fname == null) || fname.equals("") )
                    return;
                                   
                String sResurcePath = getResurcePath("http://"+ davAdapter.getServerLocations(), href);
                
                davAdapter.GetVersionFile(new File(dir+"/"+fname), sResurcePath);
              }
              
            }
          });
        }
      }
      this.setSize(363, 304);
      this.setLocation(x, y);
      this.setResizable(false);
      this.setModal(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private Vector getList(){
    Vector sar = new Vector();
    
    for (int i = 0; i < vVersionList.size(); i++) {
      Vector vTemp = (Vector)vVersionList.get(i);
      sar.add(" " + (i+1) + "(" + vTemp.get(0) +")");
    }

    return sar;
  }
  
  private String getResurcePath(String ServerLocation, String href){
    int pos = ServerLocation.compareTo(href);
    int pos1 = href.compareTo(ServerLocation);
    String s =  href.replaceAll(ServerLocation, "");
    return s;
  }

}
