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

import java.util.Vector;

/**
 * Created by The eXo Platform SAS
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