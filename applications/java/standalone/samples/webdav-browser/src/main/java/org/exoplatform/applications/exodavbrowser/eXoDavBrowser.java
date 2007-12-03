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
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;

/**
 * Created by The eXo Platform SAS
 * Author : Alex Reshetnyak
 *          alex_reshetnyak@yahoo.com
 * ${date}  
 */

public class eXoDavBrowser extends JFrame {

	{
		//Set Look & Feel
		try {
			javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}


  private JMenuBar jMenuBar1;
  private JMenu jm_File;
  private JMenu jm_View;
  private JToolBar jtb_ToolBar;
  private JPanel jp_MainPanel;
  private JTable jTable1;
  private JScrollPane jsp_dav_table;
  private JScrollPane jsp_dav_tree;
  private JMenuItem jmi_h_help;
  private JMenuItem jmi_vi_refresh;
  private JSeparator jSeparator8;
  private JMenuItem jmi_vi_view_modify;
  private JSeparator jSeparator7;
  private JMenuItem jmi_vi_view_lock_properties;
  private JMenuItem jmi_v_check_in;
  private JMenuItem jmi_v_uncheck_out;
  private JMenuItem jmi_v_check_out;
  private JSeparator jSeparator6;
  private JMenuItem jmi_v_version_report;
  private JSeparator jSeparator5;
  private JMenuItem jmi_v_put_under_vc;
  private JMenuItem jmi_e_edit_proxy_info;
  private JMenuItem jmi_e_edit_lock_info;
  private JMenuItem jmi_f_exit;
  private JSeparator jSeparator4;
  private JMenuItem jmi_f_create_collection;
  private JSeparator jSeparator3;
  private JPanel jp_dir_panel;
  private JMenuItem jmi_f_delete;
  private JMenuItem jmi_f_cut;
  private JMenuItem jmi_f_copy;
  private JMenuItem jmi_f_paste;
  private JSeparator jSeparator2;
  private JMenuItem jmi_f_unlock;
  private JMenuItem jmi_f_shared_lock;
  private JMenuItem jmi_f_exclusive_lock;
  private JSeparator jSeparator1;
  private JMenuItem jmi_f_write_file;
  private JMenuItem jmi_f_get_file;
  private JMenuItem jmi_f_write_folder;
  private JMenuItem jmi_f_get_folder;  
  private JButton jButton2;
  private JToolBar jToolBar1;
  private JButton jb_t_report;
  private JButton jb_t_uncheckout;
  private JButton jb_t_checkin;
  private JLabel jLabel2;
  private JButton jb_t_checkout;
  private JButton jb_t_put_version;
  private JLabel jLabel3;
  private JButton jb_t_unlock;
  private JButton jb_t_lock;
  private JButton jb_t_delete;
  private JButton jb_f_paste;
  private JButton jb_t_copy;
  private JLabel jLabel1;
  private JButton jb_t_write;
  private JButton jb_t_get;
  private JPanel jp_dav_panel;
  private JButton jb_GO;
  private JComboBox jcb_URL;
  private JLabel jl_HTTP;
  private JMenu jm_Help;
  private JMenu jm_Version;
  private JMenu jm_Edit;
  private DAVTable davt;
  private static JFrame  jfr;
  private int type;
  private JTree jtree;
  private KeyListener keyListener;
  public DAVAdapter davAdapter; 
  public String sServerLocation;
  

  /**
  * Auto-generated main method to display this JFrame
  */
  public static void main(String[] args) {
    eXoDavBrowser inst = new eXoDavBrowser();
    inst.setVisible(true);
  }
  
  public eXoDavBrowser() {
    super();
    jfr = this;
    this.setTitle(DAVConst.Info.sTitle + " -- " + DAVConst.Info.sEXOSite);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    initGUI();
  }
  
  private void initGUI() {
    try {
      { JPanel jp_ToolBar = new JPanel();
        jp_ToolBar.setLayout(new BorderLayout());
        
        jtb_ToolBar = new JToolBar();
        jp_ToolBar.add(jtb_ToolBar, BorderLayout.CENTER);
        
        createButtonsToolBar();
        
        jp_ToolBar.add(jToolBar1, BorderLayout.NORTH);
        getContentPane().add(jp_ToolBar, BorderLayout.NORTH);
        {
          jl_HTTP = new JLabel();
          jtb_ToolBar.add(jl_HTTP);
          jl_HTTP.setText("http://");
        }
        {
          DAVConfig davConfig = new DAVConfig();
          Vector<String> v = davConfig.ReadConfig("url",true);
          ComboBoxModel jcb_URLModel = new DefaultComboBoxModel(v);
          jcb_URL = new JComboBox();
          jcb_URL.setActionCommand("Connect");
          jtb_ToolBar.add(jcb_URL);
          jcb_URL.setEditable(true);
          jcb_URL.setModel(jcb_URLModel);
          {
            jcb_URL.addActionListener( new ActionListener(){
              public void actionPerformed(ActionEvent e)
              {
                 if (e.getActionCommand().compareTo("comboBoxEdited") == 0)
                   Conect();
              }

            });
          }
        }
        {
          jb_GO = new JButton();
          jtb_ToolBar.add(jb_GO);
          jb_GO.setText(DAVConst.ButtonTitle.sGo);
          jb_GO.setIcon(new DAVConst().getImage(DAVConst.Image.sGo));
          //jb_GO.addAncestorListener(this);
        }
      }
      {
        jp_MainPanel = new JPanel();
        getContentPane().add(jp_MainPanel, BorderLayout.CENTER);
        BorderLayout  jp_MainPanelLayout = new BorderLayout();
        jp_MainPanel.setLayout(jp_MainPanelLayout);
        {
          jp_dir_panel = new JPanel();
          jp_MainPanel.add(jp_dir_panel, BorderLayout.CENTER);
          BorderLayout jp_dir_panelLayout = new BorderLayout();
          jp_dir_panel.setLayout(jp_dir_panelLayout);
          {
            jsp_dav_table = new JScrollPane();
            jp_dir_panel.add(jsp_dav_table, BorderLayout.CENTER);
            {
              davt = new DAVTable();
              davt.setJFrame((JFrame)this);
              jTable1 = davt.getJTable();//new JTable();
              
              jsp_dav_table.setViewportView(jTable1);
            }
          }
        }
        {
          jp_dav_panel = new JPanel();
          BorderLayout jp_dav_panelLayout = new BorderLayout();
          jp_dav_panel.setLayout(jp_dav_panelLayout);
          jp_MainPanel.add(jp_dav_panel, BorderLayout.WEST);
          {
            jsp_dav_tree = new JScrollPane();
            jsp_dav_tree.setPreferredSize(new Dimension(250,250));
            jp_dav_panel.add(jsp_dav_tree, BorderLayout.CENTER);
            {
            jtree = davt.getJTree();
            jtree.setEnabled(false);
            jsp_dav_tree.setViewportView(jtree);
            }
          }
        }
      }
      setSize(1000, 600);
      {
        jMenuBar1 = new JMenuBar();
        setJMenuBar(jMenuBar1);
        {
          jm_File = new JMenu();
          jMenuBar1.add(jm_File);
          jm_File.setText("File");
          {
            jmi_f_get_file = new JMenuItem();
            jm_File.add(jmi_f_get_file);
            jmi_f_get_file.setText(DAVConst.ButtonTitle.sGetFile);
          }
          {
            jmi_f_write_file = new JMenuItem();
            jm_File.add(jmi_f_write_file);
            jmi_f_write_file.setText(DAVConst.ButtonTitle.sWriteFile);
          }
          {
            jmi_f_write_folder = new JMenuItem();
            jm_File.add(jmi_f_write_folder);
            jmi_f_write_folder.setText(DAVConst.ButtonTitle.sWriteFolder);
          }
//          {
//            jmi_f_get_folder = new JMenuItem();
//            jm_File.add(jmi_f_get_folder);
//            jmi_f_get_folder.setText("Get Folder");
//          }
          {
            jSeparator1 = new JSeparator();
            jm_File.add(jSeparator1);
          }
          {
            jmi_f_exclusive_lock = new JMenuItem();
            jm_File.add(jmi_f_exclusive_lock);
            jmi_f_exclusive_lock.setText(DAVConst.ButtonTitle.sExclisiveLock);
          }
//          {
//            jmi_f_shared_lock = new JMenuItem();
//            jm_File.add(jmi_f_shared_lock);
//            jmi_f_shared_lock.setText("Shared Lock");
//          }
          {
            jmi_f_unlock = new JMenuItem();
            jm_File.add(jmi_f_unlock);
            jmi_f_unlock.setText(DAVConst.ButtonTitle.sUnLock);
          }
          {
            jSeparator2 = new JSeparator();
            jm_File.add(jSeparator2);
          }
//          {
//            jmi_f_cut = new JMenuItem();
//            jm_File.add(jmi_f_cut);
//            jmi_f_cut.setText("Cut");
//          }
//          {
//            jmi_f_copy = new JMenuItem();
//            jm_File.add(jmi_f_copy);
//            jmi_f_copy.setText("Copy");
//          }
//          {
//            jmi_f_paste = new JMenuItem();
//            jm_File.add(jmi_f_paste);
//            jmi_f_paste.setText("Paste");
//          }
//          {
//            jmi_f_delete = new JMenuItem();
//            jm_File.add(jmi_f_delete);
//            jmi_f_delete.setText("Delete");
//          }
//          {
//            jSeparator3 = new JSeparator();
//            jm_File.add(jSeparator3);
//          }
          {
            jmi_f_create_collection = new JMenuItem();
            jm_File.add(jmi_f_create_collection);
            jmi_f_create_collection.setText(DAVConst.ButtonTitle.sCreateFolder);
          }
          {
            jSeparator4 = new JSeparator();
            jm_File.add(jSeparator4);
          }
          {
            jmi_f_exit = new JMenuItem();
            jm_File.add(jmi_f_exit);
            jmi_f_exit.setText(DAVConst.ButtonTitle.sExit);
          }
        }
        {
          jm_Edit = new JMenu();
          jMenuBar1.add(jm_Edit);
          jm_Edit.setText(DAVConst.ButtonTitle.sEdit);
          {
            jmi_f_cut = new JMenuItem();
            jm_Edit.add(jmi_f_cut);
            jmi_f_cut.setText(DAVConst.ButtonTitle.sCut);
          }
          {
            jmi_f_copy = new JMenuItem();
            jm_Edit.add(jmi_f_copy);
            jmi_f_copy.setText(DAVConst.ButtonTitle.sCopy);
          }
          {
            jmi_f_paste = new JMenuItem();
            jm_Edit.add(jmi_f_paste);
            jmi_f_paste.setText("Paste");
          }
          {
            jmi_f_delete = new JMenuItem();
            jm_Edit.add(jmi_f_delete);
            jmi_f_delete.setText(DAVConst.ButtonTitle.sDelete);
          }
//          {
//            jmi_e_edit_lock_info = new JMenuItem();
//            jm_Edit.add(jmi_e_edit_lock_info);
//            jmi_e_edit_lock_info.setText("Edit Lock Info");
//          }
//          {
//            jmi_e_edit_proxy_info = new JMenuItem();
//            jm_Edit.add(jmi_e_edit_proxy_info);
//            jmi_e_edit_proxy_info.setText("Edit Proxy Info");
//          }
        }
        {
          jm_Version = new JMenu();
          jMenuBar1.add(jm_Version);
          jm_Version.setText(DAVConst.ButtonTitle.sVersion);
          {
            jmi_v_put_under_vc = new JMenuItem();
            jm_Version.add(jmi_v_put_under_vc);
            jmi_v_put_under_vc.setText(DAVConst.ButtonTitle.sPutUnderVersionControl);
          }
          {
            jSeparator5 = new JSeparator();
            jm_Version.add(jSeparator5);
          }
          {
            jmi_v_version_report = new JMenuItem();
            jm_Version.add(jmi_v_version_report);
            jmi_v_version_report.setText(DAVConst.ButtonTitle.sVersionReport);
          }
          {
            jSeparator6 = new JSeparator();
            jm_Version.add(jSeparator6);
          }
          {
            jmi_v_check_out = new JMenuItem();
            jm_Version.add(jmi_v_check_out);
            jmi_v_check_out.setText(DAVConst.ButtonTitle.sCheckOut);
          }
          {
            jmi_v_uncheck_out = new JMenuItem();
            jm_Version.add(jmi_v_uncheck_out);
            jmi_v_uncheck_out.setText(DAVConst.ButtonTitle.sUncheckOut);
          }
          {
            jmi_v_check_in = new JMenuItem();
            jm_Version.add(jmi_v_check_in);
            jmi_v_check_in.setText(DAVConst.ButtonTitle.sCheckIn);
          }
        }
        {
          jm_View = new JMenu();
          jMenuBar1.add(jm_View);
          jm_View.setText(DAVConst.ButtonTitle.sView);
//          {
//            jmi_vi_view_lock_properties = new JMenuItem();
//            jm_View.add(jmi_vi_view_lock_properties);
//            jmi_vi_view_lock_properties.setText("View Lock Properties");
//          }
//          {
//            jSeparator7 = new JSeparator();
//            jm_View.add(jSeparator7);
//          }
//          {
//            jmi_vi_view_modify = new JMenuItem();
//            jm_View.add(getJmi_vi_view_modify());
//            jmi_vi_view_modify.setText("View / Modify Proprerties");
//          }
//          {
//            jSeparator8 = new JSeparator();
//            jm_View.add(jSeparator8);
//          }
          {
            jmi_vi_refresh = new JMenuItem();
            jm_View.add(jmi_vi_refresh);
            jmi_vi_refresh.setText(DAVConst.ButtonTitle.sRefresh);
          }
        }
        {
          jm_Help = new JMenu();
          jMenuBar1.add(jm_Help);
          jm_Help.setText(DAVConst.ButtonTitle.sHelp);
          {
            jmi_h_help = new JMenuItem();
            jm_Help.add(jmi_h_help);
            jmi_h_help.setText(DAVConst.ButtonTitle.sAboutDAVeXplorer);
          }
        }
        
        /*Menu Event*/
        
        jmi_h_help.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e){
            new AboutDialog(jfr);
          }
         });
        
        jmi_f_write_folder.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e){
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.showOpenDialog(jfr);
            
            File selFile = fileChooser.getSelectedFile();
            Log.info("DIR NAME -->" + selFile.getPath());
            
            if (selFile != null) {
              //WriteDirDialog wdd =
               (new Thread(new WriteDirDialog(jfr, davAdapter.getServerLocation(),davAdapter.getCurrentResurcePath(), selFile))).start();
              //davt.PutDir(selFile);
            }
            
          }
         });
        
        jmi_f_get_file.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e){
            getFile();
          }
         });
        
        jmi_f_write_file.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e){
            writeFile();
          }
         });
        
        jmi_f_exclusive_lock.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e){
            davt.Lock();
          }
         });
        
        jmi_f_unlock.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e){
            davt.UnLock();
          }
         });
        
        jmi_f_copy.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e){
            type = DAVConst.Type.iCOPY;
            davt.Copy();
          }
         });
        
        jmi_f_cut.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e){
            type = DAVConst.Type.iMOVE;
            davt.Cut();
          }
         });
        
        jmi_f_delete.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e){
           davt.Del();
          }
         });
        
        jmi_f_paste.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e){
            davt.Paste(type);
          }
         });
        
        jmi_f_create_collection.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e){
            CreateFolderDialog cFolderDialog = new CreateFolderDialog(jfr);
            cFolderDialog.setVisible(true);
            
            if ( cFolderDialog.sFolderName.compareTo("") != 0){
              davt.CreateFolder(cFolderDialog.sFolderName);
            }
            
          }
         });
        
        jmi_f_exit.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e){
            System.exit(0);
          }
         });
        
        
        jmi_v_put_under_vc.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e){
            davt.PutVersionControl();
          }
         });
        
        jmi_v_check_out.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e){
            davt.CheckOut();
          }
         });
        
        jmi_v_uncheck_out.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e){
            davt.UnCheckOut();
          }
         });
        
        jmi_v_check_in.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e){
            davt.CheckIn();
          }
         });
        
        jmi_v_version_report.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e){
            Vector v = davt.getVersionReport();
            VersionReport vr = new VersionReport(jfr, v, davAdapter); 
            vr.setVisible(true);
            
          }
         });
                
        jmi_vi_refresh.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e){
            davt.DirRefresh();
          }
         });
        
        jb_GO.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e){
            Conect();
//            if (jcb_URL.getSelectedItem().toString().compareTo("") != 0 ){
//              ServerLocationsParser slr = new ServerLocationsParser(jcb_URL.getSelectedItem().toString());
//              davAdapter = new DAVAdapter();
//              davAdapter.setServetLocation(slr.getHost(),slr.getPort(),slr.getServerPath());
//              
//              davAdapter.setIsLogin(true);
//              if (davAdapter.isLogin() == true){
//                davAdapter.setDataModel(davt.getdataModel());
//                davAdapter.getDir("");
//                
//                davt.setDAVAdapter(davAdapter);
//                
//                sServerLocation = davAdapter.getServerLocations();
//
//                if (davAdapter.getCurrentSatus() != 501 ){
//                  jtree = davt.getNewJTree();
//                  jsp_dav_tree.setViewportView(jtree);
//                } else {
//                  jtree.setEnabled(false);
//                }
//                
//              }
//           
//              SaveUrl();         
//            }
          }
         });
        
        
        
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public JMenuItem getJmi_vi_view_modify() {
    return jmi_vi_view_modify;
  }

  public static JFrame getJFrame(){
   return jfr;//this;
  }
  public void SaveUrl(){
    boolean bNew = true;
    for (int i = 0; i < jcb_URL.getItemCount(); i++) {
      if (jcb_URL.getItemAt(i).toString().compareTo(jcb_URL.getSelectedItem().toString()) == 0)
        bNew = false;
    }
    
    if (bNew == true){
      jcb_URL.addItem(jcb_URL.getSelectedItem().toString());
      
      Vector<String> v = new Vector();
      for (int i = 0; i < jcb_URL.getItemCount(); i++) {
        v.add(jcb_URL.getItemAt(i).toString());              
      }
      DAVConfig davConfig = new DAVConfig();
      davConfig.WriteConfigEntry("url", v,true);
    }
  }
  
  public void createButtonsToolBar(){
    try {
      BorderLayout thisLayout = new BorderLayout();
      this.setLayout(thisLayout);
      this.setPreferredSize(new java.awt.Dimension(812, 33));
      {
        jToolBar1 = new JToolBar();
        this.add(jToolBar1, BorderLayout.CENTER);
        jToolBar1.setPreferredSize(new java.awt.Dimension(814, 33));
        {
          jb_t_get = new JButton();
          jToolBar1.add(jb_t_get);
          //jb_t_get.setText("GET");
          jb_t_get.setIcon(new DAVConst().getImage(DAVConst.Image.sToolGetIcon));
          jb_t_get.setToolTipText(DAVConst.ButtonTitle.sGetFile);
          
          jb_t_get.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent e){
              getFile(); 
            }
          });
        }
        {
          jb_t_write = new JButton();
          jToolBar1.add(jb_t_write);
          //jb_t_write.setText("WRITE");
          jb_t_write.setIcon(new DAVConst().getImage(DAVConst.Image.sToolWriteIcon));
          jb_t_write.setToolTipText(DAVConst.ButtonTitle.sWriteFile);
          
          jb_t_write.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent e){
              writeFile();
            }
          });
        }
        {
          jLabel1 = new JLabel();
          jToolBar1.add(jLabel1);
          jLabel1.setText("    ");
        }
        {
          jb_t_copy = new JButton();
          jToolBar1.add(jb_t_copy);
          //jb_t_copy.setText("COPY");
          jb_t_copy.setIcon(new DAVConst().getImage(DAVConst.Image.sToolCopyIcon));
          jb_t_copy.setToolTipText(DAVConst.ButtonTitle.sCopy);
          
          jb_t_copy.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent e){
              type = DAVConst.Type.iCOPY;
              davt.Copy();  
            }
          });
        }
        {
          jb_f_paste = new JButton();
          jToolBar1.add(jb_f_paste);
          //jb_f_paste.setText("PASTE");
          jb_f_paste.setIcon(new DAVConst().getImage(DAVConst.Image.sToolPasteIcon));
          jb_f_paste.setToolTipText(DAVConst.ButtonTitle.sPaste);
          
          jb_f_paste.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent e){
              davt.Paste(type);
            }
          });
        }
        {
          jb_t_delete = new JButton();
          jToolBar1.add(jb_t_delete);
          //jb_t_delete.setText("DELETE");
          jb_t_delete.setIcon(new DAVConst().getImage(DAVConst.Image.sToolDeleteIcon));
          jb_t_delete.setToolTipText(DAVConst.ButtonTitle.sDelete);
          
          jb_t_delete.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent e){
              davt.Del();
            }
          });
        }
        {
          jLabel2 = new JLabel();
          jToolBar1.add(jLabel2);
          jLabel2.setText("    ");
        }
        {
          jb_t_lock = new JButton();
          jToolBar1.add(jb_t_lock);
          //jb_t_lock.setText("LOCK");
          jb_t_lock.setIcon(new DAVConst().getImage(DAVConst.Image.sToolLockIcon));
          jb_t_lock.setToolTipText(DAVConst.ButtonTitle.sExclisiveLock);
          
          jb_t_lock.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent e){
              davt.Lock();
            }
          });
        }
        {
          jb_t_unlock = new JButton();
          jToolBar1.add(jb_t_unlock);
          //jb_t_unlock.setText("UNLOCK");
          jb_t_unlock.setIcon(new DAVConst().getImage(DAVConst.Image.sToolUnLockIcon));
          jb_t_unlock.setToolTipText(DAVConst.ButtonTitle.sUnLock);
          
          jb_t_unlock.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent e){
              davt.UnLock();
            }
          });
        }
        {
          jLabel3 = new JLabel();
          jToolBar1.add(jLabel3);
          jLabel3.setText("    ");
        }
        {
          jb_t_put_version = new JButton();
          jToolBar1.add(jb_t_put_version);
          //jb_t_put_version.setText("PUT VERSION");
          jb_t_put_version.setIcon(new DAVConst().getImage(DAVConst.Image.sToolPutVersionIcon));
          jb_t_put_version.setToolTipText(DAVConst.ButtonTitle.sPutUnderVersionControl);
          
          jb_t_put_version.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent e){
              davt.PutVersionControl();
            }
          });
        }
        {
          jb_t_checkout = new JButton();
          jToolBar1.add(jb_t_checkout);
          //jb_t_checkout.setText("CHECKOUT");
          jb_t_checkout.setIcon(new DAVConst().getImage(DAVConst.Image.sToolCheckOutIcon));
          jb_t_checkout.setToolTipText(DAVConst.ButtonTitle.sCheckOut);
          
          jb_t_checkout.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent e){
              davt.CheckOut();
            }
          });
        }
        {
          jb_t_uncheckout = new JButton();
          jToolBar1.add(jb_t_uncheckout);
          //jb_t_uncheckout.setText("UNCHECKOUT");
          jb_t_uncheckout.setIcon(new DAVConst().getImage(DAVConst.Image.sToolUnCheckOutIcon));
          jb_t_uncheckout.setToolTipText(DAVConst.ButtonTitle.sUncheckOut);
          
          jb_t_uncheckout.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent e){
              davt.UnCheckOut();
            }
          });
        }
        {
          jb_t_checkin = new JButton();
          jToolBar1.add(jb_t_checkin);
          //jb_t_checkin.setText("CHECKIN");
          jb_t_checkin.setIcon(new DAVConst().getImage(DAVConst.Image.sToolCheckInIcon));
          jb_t_checkin.setToolTipText(DAVConst.ButtonTitle.sCheckIn);
          
          jb_t_checkin.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent e){
              davt.CheckIn();
            }
          });
        }
        {
          jb_t_report = new JButton();
          jToolBar1.add(jb_t_report);
          //jb_t_report.setText("REPORT");
          jb_t_report.setIcon(new DAVConst().getImage(DAVConst.Image.sToolVersionReportIcon));
          jb_t_report.setToolTipText(DAVConst.ButtonTitle.sVersionReport);
          
          jb_t_report.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent e){
              Vector v = davt.getVersionReport();
              VersionReport vr = new VersionReport(jfr, v, davAdapter); 
              vr.setVisible(true);  
            }
          });
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public void getFile(){
    if (davt.getSelectionResurce(DAVConst.TableColumn.DISPLAYNAME).compareTo("") != 0 ){
      FileDialog fd = new FileDialog(jfr, "Save As ...", FileDialog.SAVE);
      fd.setFile(davt.getSelectionResurce(DAVConst.TableColumn.DISPLAYNAME));
      fd.setModal(true);
      fd.setVisible(true);
      
      
      String dir = fd.getDirectory();
      if( (dir == null) || dir.equals("") )
          return;
      String fname = fd.getFile();
      if( (fname == null) || fname.equals("") )
          return;
                         
      char sch = File.separatorChar;
      davt.GetFile(new File(dir + sch + fname));
    }
  }
  
  public void writeFile(){
    JFileChooser fc = new JFileChooser();
    
    fc.showOpenDialog(jfr);
    File selFile = fc.getSelectedFile();
    
    if (selFile.length() > 10*1024*1024){
      JOptionPane.showMessageDialog(this, new String("Can't upload file larger than 10M") , "Information", 1 );
      return;
    }
    if (selFile != null ){
      davt.PutFile(selFile);
    }
  }
  
  public void Conect(){
    if (jcb_URL.getSelectedItem().toString().compareTo("") != 0 ){
      ServerLocationsParser slr = new ServerLocationsParser(jcb_URL.getSelectedItem().toString());
      davAdapter = new DAVAdapter();
      davAdapter.setServetLocation(slr.getHost(),slr.getPort(),slr.getServerPath());
      
      davAdapter.setIsLogin(true);
      if (davAdapter.isLogin() == true){
        //davt.setDAVAdapter(davAdapter);
        
        davAdapter.setDataModel(davt.getdataModel());
        davAdapter.getDir("");
        
        davt.setDAVAdapter(davAdapter);
        
        sServerLocation = davAdapter.getServerLocations();
    
        if (davAdapter.getCurrentSatus() != 501 ){
          jtree = davt.getNewJTree();
          jsp_dav_tree.setViewportView(jtree);
        } else {
          jtree = davt.getNewJTree();
          jsp_dav_tree.setViewportView(jtree);
          jtree.setEnabled(false);
        }
        
      }
    
      SaveUrl();         
    }
  }
  
}
