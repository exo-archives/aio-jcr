/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.applications.exodavbrowser;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.exoplatform.frameworks.webdavclient.WebDavContext;

/**
 * Created by The eXo Platform SARL
 * Author : Alex Reshetnyak
 *          alex_reshetnyak@yahoo.com
 * ${date}  
 */

public class WriteDirDialog  extends JDialog implements Runnable
{

  {
    //Set Look & Feel
    try {
      javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  private JPanel jPanel1;
  private JLabel jLabel1;
  private JButton jb_pause;
  private JButton jb_stop;
  private JPanel jPanel6;
  private JLabel jl_complete;
  private JProgressBar jProgressBar1;
  private int x;
  private int y;
  private WebDavContext slLocation;
  private String sResutcePath;
  private File fTemp;
  private  Put2DirThread copyDirThread;
  private boolean bStart;
  
  public WriteDirDialog(JFrame frame, WebDavContext sl ,String ResutcePath, File f) {
    super(frame);
    
    slLocation = sl;
    sResutcePath = ResutcePath; 
    fTemp = f;
    
    copyDirThread = new Put2DirThread( sl,sResutcePath, f);
    
    y = (frame.getHeight() - frame.getLocation().y)/2 - 36;
    x = (frame.getWidth() - frame.getLocation().x)/2 - 129;
    
    initGUI();
  }
  
  private void initGUI() {
    try {
      {
        jPanel6 = new JPanel();
        getContentPane().add(jPanel6, BorderLayout.NORTH);
        BorderLayout jPanel6Layout = new BorderLayout();
        jPanel6.setLayout(jPanel6Layout);
        jPanel6.setPreferredSize(new java.awt.Dimension(244, 16));
        jPanel6.setSize(244, 16);
        {
          jl_complete = new JLabel();
          jPanel6.add(jl_complete, BorderLayout.CENTER);
          jl_complete.setText("COPY");
          jl_complete.setPreferredSize(new java.awt.Dimension(180, 16));
          jl_complete.setSize(213, 16);
        }
        {
          jLabel1 = new JLabel();
          jPanel6.add(jLabel1, BorderLayout.WEST);
          jLabel1.setText("  ");
        }
      }
      {
        jPanel1 = new JPanel();
        getContentPane().add(jPanel1, BorderLayout.SOUTH);
        FlowLayout jPanel1Layout = new FlowLayout();
        jPanel1.setLayout(jPanel1Layout);
        {
          jProgressBar1 = new JProgressBar();
          jPanel1.add(jProgressBar1);
          //jProgressBar1.setString("0%");
          jProgressBar1.setPreferredSize(new java.awt.Dimension(166, 15));
          jProgressBar1.setStringPainted(true);
          jProgressBar1.setValue(0);
        }
        {
          jb_stop = new JButton();
          jPanel1.add(jb_stop);
          jb_stop.setIcon(new DAVConst().getImage(DAVConst.Image.sStop));
          jb_stop.setRolloverIcon(new DAVConst().getImage(DAVConst.Image.sStopFocus));
          jb_stop.setFocusable(false);
          jb_stop.setBorderPainted(false);
          jb_stop.setContentAreaFilled(false);
          jb_stop.setMargin(new Insets(0,0,0,0));
          
          jb_stop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              WriteDirDialogExit();
            }
          });
        }
        {
          jb_pause = new JButton();
          jPanel1.add(jb_pause);
          jb_pause.setText("Pause");
          jb_pause.setPreferredSize(new java.awt.Dimension(63, 20));

          jb_pause.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              //copyDirThread.stop();
              try {
                if (bStart == false) {
                  bStart = true;
                  jb_pause.setText("Pause");
                  copyDirThread.Go();
                } else {
                  bStart = false;
                  jb_pause.setText("Start");
                  copyDirThread.Pause();
                }
              } catch (Exception ee) {}
            }
          });
        }

      }
      this.setSize(/*254*/280, 78);
      this.setLocation(x, y);
      this.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
      this.setVisible(true);
      bStart = true;
      copyDirThread.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public void run(){
    while (copyDirThread != null){
      try {
        getProgres();
        Thread.sleep(500);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  synchronized void getProgres(){
    if ( copyDirThread.getDirSize() != 0){
      double dTamp =  ( 100 * copyDirThread.getCopyComplete()) / copyDirThread.getDirSize();
      this.setTitle( "" + (new Double(dTamp).intValue()) +"% " + copyDirThread.getCurFileName() + " (Copy)");
      jProgressBar1.setValue(new Double(dTamp).intValue());
      jl_complete.setText("[ " + copyDirThread.getCopyComplete() + " byte / "  + copyDirThread.getDirSize()+ " byte ]");
      //Log.info("[ " + copyDirThread.getCopyComplete() + " byte / "  + copyDirThread.getDirSize()+ " byte ]");
      if (new Double(dTamp).intValue() == 100 )
        this.setVisible(false);
    }
  }
  
  private void WriteDirDialogExit(){
    this.setVisible(false);
    copyDirThread.Pause();
    copyDirThread.stop();
  }
}
