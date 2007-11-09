/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.applications.exodavbrowser;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.JFrame;

/**
 * Created by The eXo Platform SARL
 * Author : Alex Reshetnyak
 *          alex_reshetnyak@yahoo.com
 * ${date}  
 */

public class DAVTable {
  
  private JTable jt;
  private DAVJTree DAVjtree;
  
  private DefaultTableModel dataModel;
  private DAVAdapter davAdapter;
  private KeyListener keyl; 
  private JFrame mainFrame;
  private Vector<TRow> ddata;
  //private String[] names = {"Icon", "Lock", "Versions", "Display Name", "Type", "Size", "Date", "Display"};
  private Vector names;
  private final ImageIcon iconDir = new DAVConst().getImage(DAVConst.Image.sDirIcon); 
  private final ImageIcon iconFile = new DAVConst().getImage(DAVConst.Image.sFileIcon); 
  private final ImageIcon iconParentDir = new DAVConst().getImage(DAVConst.Image.sParentDirIcon);
  private final ImageIcon iconLock = new DAVConst().getImage(DAVConst.Image.sLockIcon);
  private final ImageIcon iconUnLock = new DAVConst().getImage(DAVConst.Image.sUnLockIcon);
  private final ImageIcon iconVersion = new DAVConst().getImage(DAVConst.Image.sVersionIcon);

  
  
  
  public DAVTable() {
    super();
    
    DAVjtree = new DAVJTree();
 
    names = new Vector();
    names.add("Icon");
    names.add("Lock");
    names.add("Versions");
    names.add("Display Name");
    names.add("Type");
    names.add("Size");
    names.add("Date");
    
    ddata = new Vector();

    dataModel = new DefaultTableModel(ddata, names);
            
    keyl  = new KeyListener() {
      public void keyTyped(KeyEvent e){};

      public void keyPressed(KeyEvent e){
        System.out.println(e.getKeyText(e.getKeyCode()));
        //System.out.println(e.getKeyChar());
        
//        if(e.getKeyText(e.getKeyCode()).equals("Enter")){
//          int selrow = jt.getSelectedRow();
//          if (((String)(jt.getModel().getValueAt(selrow,DAVConst.TableColumn.SIZE))).compareTo("<DIR>") == 0){
//            String sleDir =  (String)(jt.getModel().getValueAt(selrow,DAVConst.TableColumn.DISPLAYNAME));  /*3 - Display Name*/
//            davAdapter.getDir(sleDir);
//          }
//        }  
//          if(e.getKeyText(e.getKeyCode()).equals("F1")){  /*add*/
//       
//          }
//          
//          if(e.getKeyText(e.getKeyCode()).equals("F2")){  /*UpDate*/
//            davAdapter.getDir();        
//          }
//          
//          if(e.getKeyText(e.getKeyCode()).equals("F3")){  /*Lock resurce*/
//            int selrow = jt.getSelectedRow();
//            String resurce = (String)(jt.getModel().getValueAt(selrow,3));
//            davAdapter.resurceLock(resurce);
//            davAdapter.getDir();
//          }
//          
//          if(e.getKeyText(e.getKeyCode()).equals("F4")){  /*UnLock*/
//            int selrow = jt.getSelectedRow();
//            String resurce = (String)(jt.getModel().getValueAt(selrow,3));
//            davAdapter.resurceUnLock(resurce);
//            davAdapter.getDir();
//          }
//          
          /*Refresh*/
          if(e.getKeyText(e.getKeyCode()).equals("F5")){  
            davAdapter.getDir();
          }
//          if(e.getKeyText(e.getKeyCode()).equals("F6")){  /*CheckOut resurce*/
//            int selrow = jt.getSelectedRow();
//            String resurce = (String)(jt.getModel().getValueAt(selrow,3));
//            davAdapter.resurceCheckOut(resurce);
//            davAdapter.getDir();
//          }
          if(e.getKeyText(e.getKeyCode()).equals("F7")){  /*Put VersionControl resurce*/
            CreateFolderDialog cFolderDialog = new CreateFolderDialog(mainFrame);
            cFolderDialog.setVisible(true);
            
            if ( cFolderDialog.sFolderName.compareTo("") != 0){
              CreateFolder(cFolderDialog.sFolderName);
            }
          }
//          
//          if(e.getKeyText(e.getKeyCode()).equals("F9")){  
//           setColumnWidth();
//          }
      };

      public void keyReleased(KeyEvent e){};
      
    };
    
    jt = new JTable(dataModel);
        
    ListSelectionModel listMod =  jt.getSelectionModel();
    listMod.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    listMod.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent e) {

      }
    });
    
    jt.setAutoscrolls(true);
    
    setColumnWidth();
    //jt.setRowSelectionAllowed(true);
    jt.setShowHorizontalLines(false);
    jt.setShowVerticalLines(false);
    jt.setSelectionBackground(new Color(216,210,238));
    jt.setSelectionForeground(new Color(0,0,0));
    //jt.setCellSelectionEnabled(true);
    
    /*SET ICON RENDER*/
    setColumnRenderer();
    
    
    jt.addMouseListener(new MouseAdapter(){
      public void mouseClicked(MouseEvent e){
       //if (e.getClickCount() == 2){
          System.out.println(" double click" + e.getClickCount());
          }
//       }
      } );
    
    jt.addKeyListener( keyl);
  }
  
  public JTable getJTable(){
    return jt;   
  }
 
  public void setJFrame( JFrame jf){
    mainFrame = jf;
  }
  
  private void clearDataModel(){
    while(dataModel.getRowCount() != 0){
      dataModel.removeRow(0);
    }
  }
  
  
  /*COMMANDS*/
  
  /*Lock resurce*/
  public void Lock(){
    if (getSelectionResurce(DAVConst.TableColumn.DISPLAYNAME).compareTo("") != 0 )
      davAdapter.resurceLock(getSelectionResurce(DAVConst.TableColumn.DISPLAYNAME));
    davAdapter.getDir();
  }
  
  /*UnLock resurce*/
  public void UnLock(){
    if (getSelectionResurce(DAVConst.TableColumn.DISPLAYNAME).compareTo("") != 0 )
      davAdapter.resurceUnLock(getSelectionResurce(DAVConst.TableColumn.DISPLAYNAME));   
    davAdapter.getDir();
  }
  
  /*Put VersionControl resurce*/
  public void PutVersionControl(){
    if (getSelectionResurce(DAVConst.TableColumn.DISPLAYNAME).compareTo("") != 0 )
      davAdapter.resurceVersionControl(getSelectionResurce(DAVConst.TableColumn.DISPLAYNAME));
    davAdapter.getDir();
  }
  
  /*CheckIn resurce*/
  public void CheckIn(){ 
    if (getSelectionResurce(DAVConst.TableColumn.DISPLAYNAME).compareTo("") != 0 )
      davAdapter.resurceCheckIn(getSelectionResurce(DAVConst.TableColumn.DISPLAYNAME));
  }
  
  /*CheckOut resurce*/
  public void CheckOut(){
    if (getSelectionResurce(DAVConst.TableColumn.DISPLAYNAME).compareTo("") != 0 )
      davAdapter.resurceCheckOut(getSelectionResurce(DAVConst.TableColumn.DISPLAYNAME));
  }
  
  /*UnCheckOut resurce*/
  public void UnCheckOut(){
    if (getSelectionResurce(DAVConst.TableColumn.DISPLAYNAME).compareTo("") != 0 )
      davAdapter.resurceUnCheckOut(getSelectionResurce(DAVConst.TableColumn.DISPLAYNAME));
  }
  
  public Vector getVersionReport(){
    Vector v = new Vector();
    if (getSelectionResurce(DAVConst.TableColumn.DISPLAYNAME).compareTo("") != 0 )
      v = davAdapter.getVersionReport(getSelectionResurce(DAVConst.TableColumn.DISPLAYNAME));
    return v; 
  } 
    
  /*Dir Refresh*/
  public void DirRefresh(){
    davAdapter.getDir();
  }
    
  /*Create folder*/
  public void CreateFolder(String DisplayName){
    davAdapter.CreateFolder(DisplayName);
    davAdapter.getDir();
  }
  
  public void PutFile(File f){
    davAdapter.PutFile(f);
    davAdapter.getDir();
  }
  
//  public void PutDir(File f){
//    davAdapter.PutDir(f);
//    davAdapter.getDir();
//  }
  
  public void GetFile(File f){
    davAdapter.GetFile(f, getSelectionResurce(DAVConst.TableColumn.DISPLAYNAME));
  }
  
  public void setColumnWidth(){
    int []ar ={40,40,60,220,40,80,170};
    
    TableColumn column = null;
    for (int i = 0; i < ar.length-1; i++) {
      if (i != DAVConst.TableColumn.DISPLAYNAME){
        column = jt.getColumnModel().getColumn(i);
        column.setPreferredWidth(ar[i]);
        column.setMaxWidth(ar[i]);
        column.setMinWidth(ar[i]);
      
      } else {
        column = jt.getColumnModel().getColumn(i);
        column.setPreferredWidth(ar[i]);
      }
    }
  }
  
  public String getSelectionResurce(int col){
    int selrow = jt.getSelectedRow();
    String resurce;
    if (selrow != -1) {
      resurce = (String)(jt.getModel().getValueAt(selrow,col));
    } else {
      resurce = "";
    }
    return resurce;
  }
  
  
  public void Copy(){
    if (getSelectionResurce(DAVConst.TableColumn.DISPLAYNAME).compareTo("") != 0 )
      davAdapter.resurceCopy(getSelectionResurce(DAVConst.TableColumn.DISPLAYNAME));
  }
  
  public void Paste( int type){
    if (getSelectionResurce(DAVConst.TableColumn.DISPLAYNAME).compareTo("") != 0 ){
      if (type == DAVConst.Type.iCOPY)
        davAdapter.resurcePaste(getSelectionResurce(DAVConst.TableColumn.DISPLAYNAME));
      if (type == DAVConst.Type.iMOVE)
        davAdapter.resurceCutPaste(getSelectionResurce(DAVConst.TableColumn.DISPLAYNAME));
    }
    
    davAdapter.getDir();
  }
  
  public void Cut(){
    if (getSelectionResurce(DAVConst.TableColumn.DISPLAYNAME).compareTo("") != 0 )
      davAdapter.resurceCut(getSelectionResurce(DAVConst.TableColumn.DISPLAYNAME));
  }
  
  public void Del(){
    if (getSelectionResurce(DAVConst.TableColumn.DISPLAYNAME).compareTo("") != 0 )
      davAdapter.resurceDelete(getSelectionResurce(DAVConst.TableColumn.DISPLAYNAME));
    
    davAdapter.getDir();
  }
  
  public JTree getJTree(){
    return DAVjtree.getJTree();
  }
  
  public JTree getNewJTree(){
    DAVJTree jtTemp = new DAVJTree();
    jtTemp.setDavadapter(davAdapter);
    DAVjtree = jtTemp;
    return DAVjtree.getJTree(); 
  }
  
  public void setDAVAdapter(DAVAdapter da){
    davAdapter = da;
    DAVjtree.setDavadapter(da);
  }
  
  public DefaultTableModel getdataModel(){
    return dataModel;
  }
  
  private void setColumnRenderer(){
    /*ICON*/
    TableColumn resizeCol = jt.getColumn(jt.getColumnName(DAVConst.TableColumn.ICON));
    DefaultTableCellRenderer dirIconRenderer = new DefaultTableCellRenderer()
    {
        public void setValue(Object value)
        {
            try
            { 
              if ((value.toString()).compareTo("D") == 0)
                setIcon(iconDir);
              
              if ((value.toString()).compareTo("F") == 0)
                setIcon(iconFile);
              if ((value.toString()).compareTo("<-") == 0)
                setIcon(iconParentDir);
                
              setHorizontalAlignment( SwingConstants.CENTER );
            }
            catch (Exception e){}
        }
    };
    resizeCol.setCellRenderer(dirIconRenderer);
    
    /*LOCK*/
    resizeCol = jt.getColumn(jt.getColumnName(DAVConst.TableColumn.LOCK));
    DefaultTableCellRenderer dirLockRenderer = new DefaultTableCellRenderer()
    {
      public void setValue(Object value)
      {
        try
        { 
          if ((value.toString()).compareTo("Lock") == 0)
            setIcon(iconLock);
          if ((value.toString()).compareTo("") == 0)
            setIcon(iconUnLock);
              
          setHorizontalAlignment( SwingConstants.CENTER );
        } catch (Exception e){}
      }
    };
    resizeCol.setCellRenderer(dirLockRenderer);
  
    /*VERSION*/
    resizeCol = jt.getColumn(jt.getColumnName(DAVConst.TableColumn.VERSION));
    DefaultTableCellRenderer dirVersionRenderer = new DefaultTableCellRenderer()
    {
      public void setValue(Object value)
      {
        try
        { 
          if ((value.toString()).compareTo("Versions") == 0)
            setIcon(iconVersion);
          if ((value.toString()).compareTo("") == 0)
            setIcon(iconUnLock);
              
          setHorizontalAlignment( SwingConstants.CENTER );
        } catch (Exception e){}
      }
    };
    resizeCol.setCellRenderer(dirVersionRenderer);
  }
  

  
}
