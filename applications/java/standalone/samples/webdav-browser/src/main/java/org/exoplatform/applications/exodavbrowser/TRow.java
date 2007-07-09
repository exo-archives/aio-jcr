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

public class TRow {
  
  private Vector rrow;
  
  public TRow(){
    rrow = new Vector(7);
  }
  
  public TRow( Vector vv){
    for (int i = 0; i < 7; i++) {
      rrow.add(vv.get(i));
    }
  }
  
  public Object getObject(int i ){
    return (Object)rrow.get(i);
  }
  
  public void setOdject( int i, Object o){
    if((i>=0 ) && (i<=7))
      rrow.add(i, o);
  }
}