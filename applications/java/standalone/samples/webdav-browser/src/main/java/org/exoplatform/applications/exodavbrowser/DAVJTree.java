/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.applications.exodavbrowser;

import java.util.Vector;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

/**
 * Created by The eXo Platform SARL
 * Author : Alex Reshetnyak
 *          alex_reshetnyak@yahoo.com
 * ${date}  
 */

public class DAVJTree{
  private JTree tree;
  private DAVAdapter davAdapter;
  
  public DAVJTree(){
    
    // Retrieve the three icons
    Icon leafIcon = new DAVConst().getImage(DAVConst.Image.sDirIcon);
    Icon openIcon = new DAVConst().getImage(DAVConst.Image.sDirIcon);
    Icon closedIcon = new DAVConst().getImage(DAVConst.Image.sDirIcon);
    
    // Create tree
     tree = new JTree();
    
    // Update only one tree instance
    DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer)tree.getCellRenderer();
    renderer.setLeafIcon(leafIcon);
    renderer.setClosedIcon(closedIcon);
    renderer.setOpenIcon(openIcon);
    
    // Remove the icons
    renderer.setLeafIcon(null);
    renderer.setClosedIcon(null);
    renderer.setOpenIcon(null);
    
    // Change defaults so that all new tree components will have new icons
    UIManager.put("Tree.leafIcon", leafIcon);
    UIManager.put("Tree.openIcon", openIcon);
    UIManager.put("Tree.closedIcon", closedIcon);
            
//  Create tree with new icons
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("/");
    tree = new JTree(root);

    
    tree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent evt) {
//        new TreeUpData(davAdapter, evt).start();
        if (davAdapter != null)
          if (davAdapter.getCurrentSatus() != 501){
            Log.info("++");
            TreePath[] paths = evt.getPaths();
            JTree jtr = (JTree)evt.getSource();
            TreePath p = jtr.getSelectionPath();
                      
            String s = new String();
            Object[] ob = evt.getPath().getPath();
            String ss = new String();
              
            for (int i = 0; i < ob.length; i++) {
              ss = ss + "/" + (String)ob[i].toString();
              //Log.info(ss);
              ss = DAVAdapter.Strip2Slash(ss);
              //Log.info(ss);
            }
 
            for (int i = 0; i < paths.length; i++) {
               s += ("/" + paths[i].getLastPathComponent().toString());
            }

            Vector v = davAdapter.getDirTree(ss);
                    
            DefaultMutableTreeNode dmtn_ = (DefaultMutableTreeNode)evt.getPath().getLastPathComponent();
            dmtn_.removeAllChildren();
                    
            for (int j = 0; j < v.size(); j++) {
              DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)evt.getPath().getLastPathComponent();
              Vector vTemp = (Vector)(v.get(j));
              if ( (Boolean)vTemp.get(1) == true)
                dmtn.add(new DefaultMutableTreeNode(vTemp.get(0)));
            }
                
            davAdapter.gDir(ss);
          }
      }
    });
  }

  public JTree getJTree(){
    return tree;
  }
  
  public void setDavadapter( DAVAdapter da){
    davAdapter = da;
  }
}

class TreeUpData extends Thread {
  private DAVAdapter da;
  TreeSelectionEvent e;
  
  public TreeUpData(DAVAdapter d, TreeSelectionEvent ee){
    super();
    da = d;
    e = ee;
  }
  
  public void run(){
    if (da != null)
      if (da.isLogin() == true){
        TreePath[] paths = e.getPaths();
        JTree jtr = (JTree)e.getSource();
        TreePath p = jtr.getSelectionPath();
                  
        String s = new String();
        Object[] ob = e.getPath().getPath();
        String ss = new String();
          
        for (int i = 0; i < ob.length; i++) {
          ss = ss + "/" + (String)ob[i].toString();
          //Log.info(ss);
          ss = DAVAdapter.Strip2Slash(ss);
          //Log.info(ss);
        }

        for (int i = 0; i < paths.length; i++) {
           s += ("/" + paths[i].getLastPathComponent().toString());
        }

        Vector v = da.getDirTree(ss);
                
        DefaultMutableTreeNode dmtn_ = (DefaultMutableTreeNode)e.getPath().getLastPathComponent();
        dmtn_.removeAllChildren();
                
        for (int j = 0; j < v.size(); j++) {
          DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)e.getPath().getLastPathComponent();
          Vector vTemp = (Vector)(v.get(j));
          if ( (Boolean)vTemp.get(1) == true)
            dmtn.add(new DefaultMutableTreeNode(vTemp.get(0)));
        }
            
        da.gDir(ss);
      }
  }
   
}