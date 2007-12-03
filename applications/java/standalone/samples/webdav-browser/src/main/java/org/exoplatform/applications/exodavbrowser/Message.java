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

import javax.swing.JOptionPane;

/**
 * Created by The eXo Platform SAS
 * Author : Alex Reshetnyak
 *          alex_reshetnyak@yahoo.com
 * ${date}  
 */

public class Message{
  private String sMSG;
  private int statusDialog;
  
  public Message(String mes){
    Log.info(mes);
  }
  public Message(){}
  
  public void errorMsg(String sMessage){
    Object[] options = { "OK" };
    JOptionPane.showOptionDialog(eXoDavBrowser.getJFrame(),sMessage,"Error Message", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
  }

  
  public Message(int status){
    Log.info("Message -->" + status);
    
    if (status == 200)
      sMSG = "     200  -->  0K";
    if (status == 201)
      sMSG = "     201  -->  CREATED";
    if (status == 204)
      sMSG = "     204  -->  NO CONTENT";
    if (status == 207)
      sMSG = "     207  -->  MULTISTATUS";
    if (status == 401)
      sMSG = "     401  -->  AUTHNEEDED";
    if (status == 404)
      sMSG = "     404  -->  NOT FOUND";
    if (status == 412)
      sMSG = "     412  -->  PRECONDITION FAILED";
    if (status == 409)
      sMSG = "     409  -->  CONFLICT";
    if (status == 403)
      sMSG = "     403  -->  FORBIDDEN";
    if (status == 405)
      sMSG = "     405  -->  METHOD NOT ALLOWED";
    if (status == 415)
      sMSG = "     415  -->  UNSUPPORTED MEDIATYPE";
    if (status == 507)
      sMSG = "     507  -->  IN SUFFICIENTS TORAGE";
    if (status == 501)
      sMSG = "     501  -->  NOT IMPLEMENTED";

    


    if (status == 401) {
      LogInDialog lid =  new LogInDialog(eXoDavBrowser.getJFrame());
      lid.setVisible(true);
      
      if (lid.isOK()){
        DAVAdapter.getServerLocation().setUserId(lid.getUserID());
        DAVAdapter.getServerLocation().setUserPass(lid.getPassword());
        statusDialog = DAVConst.SatusDialog.iOK;
      } else {
        //new MessagerDialog(eXoDavBrowser.getJFrame(),sMSG);
        errorMsg(sMSG);
        statusDialog = DAVConst.SatusDialog.iCANCEL;
      }
      
    } else {
      //new MessagerDialog(eXoDavBrowser.getJFrame(), sMSG);
      errorMsg(sMSG);
    }
  }
  
  public int getSatus(){
    return statusDialog;
  }
}