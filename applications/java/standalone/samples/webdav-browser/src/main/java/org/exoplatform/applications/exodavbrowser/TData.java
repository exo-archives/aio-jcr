/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.applications.exodavbrowser;

import java.util.Vector;

/**
 * Created by The eXo Platform SARL
 * Author : Alex Reshetnyak
 *          alex_reshetnyak@yahoo.com
 * ${date}  
 */

public class TData {
  
  private Vector<TRow> ddata;
  
  public TData(){
    ddata = new Vector();
  }
  
  public int getRowCount(){
    return ddata.size();
  }
  
  public TRow getRow(int i){
    if(((i>=0) && (i<= ddata.size())) == false)i=0;
    return ddata.get(i);
  }
  
  public boolean addRow( TRow rrow){
    return ddata.add(rrow);
  }   
}