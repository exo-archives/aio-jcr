/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.applications.exodavbrowser;

import java.io.File;
import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.ServerLocation;
import org.exoplatform.frameworks.davclient.commands.DavMkCol;
import org.exoplatform.frameworks.davclient.commands.DavPut;

/**
 * Created by The eXo Platform SARL
 * Author : Alex Reshetnyak
 *          alex_reshetnyak@yahoo.com
 * ${date}  
 */

public class Put2DirThread  extends Thread {
  private ServerLocation location;
  private String sResurcePath;
  private File fTemp;
  private long lDirSize;
  private long lCopyComplete;
  private boolean bPause;
  private String sCurFileName;
  
  public void run() {
    lDirSize = getSizeDir(fTemp);
    Log.info("SIZE OF DR ->" + lDirSize);
    bPause = false;
    Put2Dir(sResurcePath,fTemp);
  }
  
  synchronized public void Pause(){
    bPause = true;
  } 
  
  synchronized public void Go(){
    bPause = false;
  }
  
  
  synchronized public long getDirSize(){
    return lDirSize;
  }
  
  synchronized public long getCopyComplete(){
    return lCopyComplete;
  }
  synchronized public String getCurFileName(){
    return sCurFileName;
  }
  
  public Put2DirThread(ServerLocation sl ,String ResurcePath, File f){
    location = sl;
    sResurcePath = ResurcePath;
    fTemp = f;
    sCurFileName = "";
  }
  
  public void Put2Dir(String ResurcePath, File f){
    CreateFolder(ResurcePath ,f.getName());
    
    String curResurcePath = ResurcePath + "/" + f.getName();
    
    String[] listDir = f.list();
    
    for (int i = 0; i < listDir.length; i++) {
      File fTemp = new File(f.getPath() + File.separator + listDir[i]);
      if (fTemp.isDirectory()){
        CreateFolder( curResurcePath, fTemp.getName());
        Put2Dir(curResurcePath,fTemp);
      }
      else{
        int h = 0;
        while (bPause == true){
          try {
            h++;
            //Log.info("pause --> " + h);
            this.sleep(500);
          } catch (Exception e) {}
          
        }
        sCurFileName = fTemp.getName();
        
        PutFile(curResurcePath,fTemp);
        lCopyComplete += fTemp.length();
      }
    }
  }
  
  public void PutFile(String ResurcePath, File f){
    try {
      DavPut put = new DavPut(location);
      put.setResourcePath(ResurcePath + "/" +f.getName());
     
      byte []dataByte = FileToByteArray.getBytesFromFile(f); 
     
      put.setRequestDataBuffer(dataByte);
     
      int status = put.execute();
     
      if (status != Const.HttpStatus.CREATED)
        new Message(status);
    } catch (Exception e) {
      e.printStackTrace();
    }
   }
  
  public void CreateFolder( String ResurcePath, String DisplayName){
    try {
      DavMkCol MkCol = new DavMkCol(location);
      MkCol.setResourcePath(ResurcePath + "/" +DisplayName);
      
      int status = MkCol.execute();
      
      if (status != Const.HttpStatus.CREATED)
        new Message(status);
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  
  public long getSizeDir(File f){
        
    long size = 0;
    String[] listDir = f.list();
    
    for (int i = 0; i < listDir.length; i++) {
      File fTemp = new File(f.getPath() + File.separator + listDir[i]);
      if (fTemp.isDirectory()){
        size += getSizeDir(fTemp);
      }
      else{
        size += fTemp.length(); 
      }
    }
    return size;
  }
  
}