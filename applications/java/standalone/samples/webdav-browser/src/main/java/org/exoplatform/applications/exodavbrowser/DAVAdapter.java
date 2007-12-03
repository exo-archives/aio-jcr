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

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableModel;

import org.exoplatform.frameworks.httpclient.TextUtils;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.WebDavContext;
import org.exoplatform.frameworks.webdavclient.commands.DavCheckIn;
import org.exoplatform.frameworks.webdavclient.commands.DavCheckOut;
import org.exoplatform.frameworks.webdavclient.commands.DavCopy;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavGet;
import org.exoplatform.frameworks.webdavclient.commands.DavLock;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavMove;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.commands.DavPut;
import org.exoplatform.frameworks.webdavclient.commands.DavReport;
import org.exoplatform.frameworks.webdavclient.commands.DavUnCheckOut;
import org.exoplatform.frameworks.webdavclient.commands.DavUnLock;
import org.exoplatform.frameworks.webdavclient.commands.DavVersionControl;
import org.exoplatform.frameworks.webdavclient.documents.DocumentApi;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.properties.CheckedInProp;
import org.exoplatform.frameworks.webdavclient.properties.CheckedOutProp;
import org.exoplatform.frameworks.webdavclient.properties.ContentLengthProp;
import org.exoplatform.frameworks.webdavclient.properties.CreatorDisplayNameProp;
import org.exoplatform.frameworks.webdavclient.properties.DisplayNameProp;
import org.exoplatform.frameworks.webdavclient.properties.LastModifiedProp;
import org.exoplatform.frameworks.webdavclient.properties.LockDiscoveryProp;
import org.exoplatform.frameworks.webdavclient.properties.ResourceTypeProp;
import org.exoplatform.frameworks.webdavclient.properties.LockDiscoveryProp.ActiveLock;


/**
 * Created by The eXo Platform SAS
 * Author : Alex Reshetnyak
 *          alex_reshetnyak@yahoo.com
 * ${date}  
 */

public class DAVAdapter{
  
  private DefaultTableModel dataModel;
  private static WebDavContext location;
  private String sParentResurcePath;
  private String sResurcePath;
  private String sResurceDisplayName;
  private Vector temp;
  private DavPropFind propFind;
  private String sLock;
  private String sDisplayName;
  private String sIsCollection;
  private String sType;
  private String sLastModified;
  private String sContentLength;
  private String sVersion;
  private String sLockToken;
  private String sResurcePathCopy;
  private String sResurceCopyName; 
  private Vector<ResurceLockToken> vLockToken;
  private Icon iDirIcom;
  private String sServerHost;
  private int iServerPort;
  private String sServerPath;
  private boolean bLogin;
  private static int iCurrentStatus;
    
  public static WebDavContext getServerLocation(){
    return location;
  }
  public  String getHref(WebDavContext sl){
    String sHref =getServerLocation().getHost() +":"+ getServerLocation().getPort() + getServerLocation().getServletPath();
    sHref.replaceAll("//","");
    return "http://" + sHref;  
  } 
  
  
  
  public static int getCurrentSatus(){
    return iCurrentStatus;
  }
  
  public DAVAdapter(){
    sResurcePath = new String(""); 
    sLock = new String();
    sDisplayName = new String();
    sIsCollection = new String();
    sType = new String();
    sLastModified = new String();
    sContentLength = new String();
    sLockToken = new String();
    vLockToken = new Vector();
    sResurcePathCopy = new String();
    iDirIcom = new ImageIcon(DAVConst.Image.sDirIcon);
    bLogin = false;
  }
  
  public WebDavContext setServetLocation(String host, int port, String ServletPath){
    location = new WebDavContext();
    location.setHost(host);
    location.setPort(port);
    location.setServletPath(ServletPath);
    
    sServerHost = host;
    iServerPort = port;
    sServerPath = ServletPath;
    
    return location;
  }
  
  public String getServerLocations(){
    return (sServerHost + ":" + iServerPort + sServerPath);
  }
  
  public void setDataModel(DefaultTableModel dm){
    dataModel = dm;
  };
  
  private void clearDataModel(){
    while(dataModel.getRowCount() != 0){
      dataModel.removeRow(0);
    }
  }
  
  private String getParentDir(String dir){
       String temp = dir;
    String[] sar =  temp.split("/");
    String sRet = new String();
    
    if(sar.length > 1){
      for (int i = 0; i < sar.length-1; i++) {
        if(sar[i].equals("") != true){
          sRet = sRet +"/"+sar[i];
        }
      }
    }
    else
    {
      sRet = dir; 
    }
    return sRet;
  }
  
  private String getDisplayName(String ResurcePath){
    String[] temp = ResurcePath.split("/");
    if (ResurcePath.compareTo("/") == 0 )
      return "/";
    else
      return temp[temp.length-1];
  }
  
  private String getType( String ResurceDisplayName){
    String[] temp = ResurceDisplayName.split("[.]");
    return temp[temp.length-1];
  }
  public void getDir(){
    getDir( sResurcePath);
  }
  
  public void getDir( String DisplayName){
    if (DisplayName.compareTo(sResurcePath) != 0 ){
      if (DisplayName.equals("..")){
        sResurcePath = sParentResurcePath;
        sParentResurcePath = getParentDir(sParentResurcePath);
      } else {
        sResurcePath += ("/" + DisplayName) ;
        sParentResurcePath = getParentDir(sResurcePath);
      }
    }      
      
      if (sResurcePath.equals(""))sResurcePath = "/";  
      
      if (sResurcePath.compareTo("/") != 0) {
        sResurceDisplayName = getDisplayName(sResurcePath);
      } else {
        sResurceDisplayName = "";
      }
      
      gDir(sResurcePath);
  }
  
  public void gDir(String ResurcePath){
    sResurcePath = ResurcePath;
    sParentResurcePath = getParentDir(sResurcePath);
    
    String ssDisplayName = new String();
    if(ResurcePath.compareTo("//") == 0)
      ssDisplayName = "/";
    else
      ssDisplayName = getDisplayName(ResurcePath);
    
    try {
      System.out.println( "--->" + ResurcePath);
      propFind = new DavPropFind(getServerLocation());
      propFind.setResourcePath(ResurcePath);
            
      propFind.setRequiredProperty(Const.DavProp.DISPLAYNAME);
      propFind.setRequiredProperty(Const.DavProp.LOCKDISCOVERY);
      propFind.setRequiredProperty(Const.DavProp.RESOURCETYPE);
      propFind.setRequiredProperty(Const.DavProp.GETLASTMODIFIED);
      propFind.setRequiredProperty(Const.DavProp.GETCONTENTLENGTH);
      propFind.setRequiredProperty(Const.DavProp.VERSIONNAME);
      propFind.setRequiredProperty(Const.DavProp.CHECKEDIN);
      propFind.setRequiredProperty(Const.DavProp.CHECKEDOUT);

      int status = propFind.execute();
      
      iCurrentStatus = status;
      
      if (status > 400){
        String sTemp = sResurcePath;
        sResurcePath = sParentResurcePath;
        sParentResurcePath = getParentDir(sParentResurcePath);
        
        Message msg = new Message(status);
                
        if (status != 501){
          if(msg.getSatus() != DAVConst.SatusDialog.iCANCEL) gDir(sTemp);
        }
        else {
          clearDataModel();
          iCurrentStatus = status;
        }

      }
      else{
        
        clearDataModel();
        
        Multistatus multistatus = (Multistatus)propFind.getMultistatus();
        ArrayList responses = multistatus.getResponses();
        
        if (((ResponseDoc)responses.get(0)).getHref().compareTo(getHref(getServerLocation())) == 0)
          responses.remove(0);
        
          for (int i = 0; i < responses.size(); i++) {
            ResponseDoc curResponse = (ResponseDoc)responses.get(i);

            /*Display Name*/
            sDisplayName = ((DisplayNameProp)curResponse.getProperty(Const.DavProp.DISPLAYNAME)).getDisplayName();
            
            /*DIR & TYPE*/
            boolean bIsColl = ((ResourceTypeProp)curResponse.getProperty(Const.DavProp.RESOURCETYPE)).isCollection();
            
            sType = "T";
            //sType = getType(sResurceDisplayName);
            sIsCollection = "";
            if(bIsColl){
              sIsCollection = "<DIR>";
              sType = "";
            }
                                   
            /*LOCK*/
            LockDiscoveryProp lockProp = (LockDiscoveryProp)curResponse.getProperty(Const.DavProp.LOCKDISCOVERY);
            //System.out.println("LOCK - " + lockProp);
            ActiveLock alock = lockProp.getActiveLock();
            //System.out.println("ACTIVELOCK - " + alock);
            sLock = "";
            if(alock != null)sLock = "Lock";
            
            /*LAST MODIFIED FOR FILE*/
            if(bIsColl == false){
              LastModifiedProp lastModified = (LastModifiedProp)curResponse.getProperty(Const.DavProp.GETLASTMODIFIED);
              //System.out.println("LastModified = " + lastModified);
              sLastModified = lastModified.getLastModified();
            }else
            /*LAST MODIFIED FOR DIR -- empty*/
            { 
              sLastModified = "";
            }
                      
            /*CONTENT LENGTH*/
            ContentLengthProp pContentLength = (ContentLengthProp)curResponse.getProperty(Const.DavProp.GETCONTENTLENGTH);
            sContentLength = Long.toString(pContentLength.getContentLength());
                      
            /*VERSION*/
            CheckedInProp pCheckedIn = (CheckedInProp)curResponse.getProperty(Const.DavProp.CHECKEDIN);
            CheckedOutProp pCheckedOut = (CheckedOutProp)curResponse.getProperty(Const.DavProp.CHECKEDOUT);
            
            if((pCheckedIn.isCheckedIn() == true) || (pCheckedOut.isCheckedOut() == true)){
              sVersion = "Versions";
            } else{
              sVersion = "";
            }
                                
//            if(((sResurcePath.equals("/") != true) && (sResurcePath.equals("//") != true)) && (i == 0)){
//              {sLock = "";}
//              temp = new Vector();
//              temp.add("<-");
//              temp.add(sLock);
//              temp.add("");
//              temp.add("..");
//              temp.add("");
//              temp.add("<DIR>");
//              temp.add("");
//              dataModel.addRow(temp);
//            }

            if( (sDisplayName.compareTo(ssDisplayName) != 0) 
                && 
                (sDisplayName.compareTo("") != 0)
                && (sResurceDisplayName.compareTo(sDisplayName) != 0) ){
           
              temp = new Vector();
              if (bIsColl == true)
                temp.add("D");
              else
                temp.add("F");
              
              //temp.add(iDirIcon);
              temp.add(sLock);
              temp.add(sVersion);
              temp.add(sDisplayName);

              if (bIsColl == true){
                temp.add(sType);
                temp.add(sIsCollection);
              } else {
                temp.add(getType(sDisplayName));
                temp.add(sContentLength);
              }
              
              temp.add(sLastModified);
              dataModel.addRow(temp);
            }
           
            
          }

      }
    } catch (Exception e) {
      new Message().errorMsg("Connection error!");
      clearDataModel();
      iCurrentStatus = 501;
    }
  }   
  
  public Vector getDirTree(String ResurcePath){
    
    Vector vRes = new Vector(); 
    
    String[] temp = ResurcePath.split("/");
    String dName = ResurcePath;
    if (temp.length > 0)
    dName = temp[temp.length-1];
    
    try {
      DavPropFind propFind = new DavPropFind(getServerLocation());
      propFind.setResourcePath(ResurcePath);
      
      propFind.setRequiredProperty(Const.DavProp.DISPLAYNAME);
      propFind.setRequiredProperty(Const.DavProp.RESOURCETYPE);
      
      int status = propFind.execute();
      if (status  == Const.HttpStatus.MULTISTATUS){
        
        Multistatus multistatus = (Multistatus)propFind.getMultistatus();
        ArrayList responses = multistatus.getResponses();
        
        
        System.out.println("my -->> "  + getHref(getServerLocation()));
        System.out.println("hr -->> "  + ((ResponseDoc)responses.get(0)).getHref());
        
        if (((ResponseDoc)responses.get(0)).getHref().compareTo(getHref(getServerLocation())) == 0)
          responses.remove(0);
        
          for (int i = 0; i < responses.size(); i++) {
            ResponseDoc curResponse = (ResponseDoc)responses.get(i);
            
            Vector vTemp = new Vector();
            
            /*Display Name*/
            sDisplayName = ((DisplayNameProp)curResponse.getProperty(Const.DavProp.DISPLAYNAME)).getDisplayName();
            /*DIR & TYPE*/
            boolean bIsColl = ((ResourceTypeProp)curResponse.getProperty(Const.DavProp.RESOURCETYPE)).isCollection();
            if (((sDisplayName.compareTo("") != 0) && (sDisplayName.compareTo(dName) != 0))){  
              vTemp.add(sDisplayName);
              vTemp.add(bIsColl);
  
              vRes.add(vTemp);
            }
          }
      } 
    } catch (Exception e) {}
    
    return vRes;
  }
  
  public void resurceLock( String DisplayName){
    try {
      DavLock lock = new DavLock(getServerLocation());
      lock.setResourcePath(sResurcePath + "/"+ DisplayName);
      int status = lock.execute();
      
      if (status != Const.HttpStatus.OK){
        new Message(status);  
      } else {
        vLockToken.add(new ResurceLockToken(sResurcePath + "/"+ DisplayName, lock.getLockToken()));
        int ii = vLockToken.size();
        int hh = ii;
        Log.info("RESPATH   -->" + sResurcePath + "/"+ DisplayName);
        Log.info("LOCKTOKEN -->" + lock.getLockToken());
      }
      
    } catch (Exception e) {}

  }
  
  public void resurceUnLock(String DisplayName) {
      if (isToken(sResurcePath + "/"+ DisplayName) == true){
        String sToken = getToken(sResurcePath + "/"+ DisplayName);
        Log.info("LOCKTOKEN -->" + sToken);
        try {
          DavUnLock unLock = new DavUnLock(getServerLocation());
          unLock.setResourcePath(sResurcePath + "/"+ DisplayName);
          unLock.setLockToken(sToken);
          
          int status = unLock.execute();
          
          if (status > 400){
            new Message(status);
          } else {
            removToken(sResurcePath + "/"+ DisplayName);
          }
        } catch (Exception e) {}
      }

    
  }
  
//  public boolean  resurceisLocoked(String DisplayName){
//    boolean bLock = false;
//    try {
//      DavPropFind propFind = new DavPropFind(getServerLocation());
//      propFind.setResourcePath(sResurcePath + "/" +DisplayName );
//            
//      propFind.setRequiredProperty(Const.DavProp.LOCKDISCOVERY);
//      
//      int status = propFind.execute();
//      
//      Multistatus multistatus = (Multistatus)propFind.getMultistatus();
//      ArrayList responses = multistatus.getResponses();
//        for (int i = 0; i < responses.size(); i++) {
//          ResponseDoc curResponse = (ResponseDoc)responses.get(i);
//          /*LOCK*/
//          LockDiscoveryProp lockProp = (LockDiscoveryProp)curResponse.getProperty(Const.DavProp.LOCKDISCOVERY);
//          ActiveLock alock = lockProp.getActiveLock();
//
//          bLock = false;
//          if(alock != null)bLock = true;
//        }
//    } catch (Exception e) {}
//    return bLock;
//  }
  
  public boolean isToken(String resurcePath){
    boolean isToken = false;
    for (int i = 0; i < vLockToken.size(); i++) {
      if ((vLockToken.get(i).getResurcePath()).compareTo(resurcePath) == 0){
        isToken = true;
      }
    }
    return isToken;
  }
  
  public String getToken(String resurcePath){
    String sToken = new String();
    for (int i = 0; i < vLockToken.size(); i++) {
      if ((vLockToken.get(i).getResurcePath()).compareTo(resurcePath) == 0){
        sToken = vLockToken.get(i).getLockToken();
      }
    }
    return sToken;
  }
  
  public void removToken(String resurcePath){
    for (int i = 0; i < vLockToken.size(); i++) {
      if ((vLockToken.get(i).getResurcePath()).compareTo(resurcePath) == 0)
        vLockToken.remove(i);
    }
  }
  
  public void resurceVersionControl(String DisplayName){
    try {
      DavVersionControl verControl = new DavVersionControl(getServerLocation());
      verControl.setResourcePath(sResurcePath + "/" +DisplayName);
      
      int status = verControl.execute();
      
      if (status != Const.HttpStatus.OK)
        new Message(status);
      
    } catch (Exception e) {}
    
  }
  
  public void resurceCheckIn(String DisplayName){
    try {
      DavCheckIn CheckIn = new DavCheckIn(getServerLocation());
      CheckIn.setResourcePath(sResurcePath + "/" +DisplayName);
      
      int status = CheckIn.execute();
      
      if (status != Const.HttpStatus.OK)
        new Message(status);
     
    } catch (Exception e) {}
  }
  
  public void resurceCheckOut(String DisplayName){
    try {
      DavCheckOut CheckOut = new DavCheckOut(getServerLocation());
      CheckOut.setResourcePath(sResurcePath + "/" +DisplayName);
      
      int status = CheckOut.execute();
      
      if (status != Const.HttpStatus.OK)
        new Message(status);
      
    } catch (Exception e) {}
  }
  
  public void resurceUnCheckOut(String DisplayName){
    try {
      DavUnCheckOut unCheckOut = new DavUnCheckOut(getServerLocation());
      unCheckOut.setResourcePath(sResurcePath + "/" +DisplayName);
      
      int status = unCheckOut.execute();
      
      if (status != Const.HttpStatus.OK)
        new Message(status);
      
    } catch (Exception e) {}
  }
  
  public void CreateFolder(String DisplayName){
    try {
      //String escDisplayName = TextUtils.Escape(DisplayName,'%', false);
      DavMkCol MkCol = new DavMkCol(getServerLocation());
      MkCol.setResourcePath(sResurcePath + "/" +DisplayName);
      
      int status = MkCol.execute();
      
      if (status != Const.HttpStatus.CREATED)
        new Message(status);
      
    } catch (Exception e) {}
  }
  
//  public void CreateFolder( String ResurcePath, String DisplayName){
//    try {
//      //String escDisplayName = TextUtils.Escape(DisplayName,'%', false);
//      DavMkCol MkCol = new DavMkCol(getServerLocation());
//      MkCol.setResourcePath(ResurcePath + "/" +DisplayName);
//      
//      int status = MkCol.execute();
//      
//      if (status != Const.HttpStatus.CREATED)
//        new Message(status);
//      
//    } catch (Exception e) {}
//  }
  
  public void PutFile(File f){
   try {
     DavPut put = new DavPut(getServerLocation());
     put.setResourcePath(sResurcePath + "/" +f.getName());
    
     byte []dataByte = FileToByteArray.getBytesFromFile(f); 
    
     put.setRequestDataBuffer(dataByte);
    
     int status = put.execute();
    
     if (status != Const.HttpStatus.CREATED)
       new Message(status);
   } catch (Exception e) {}
  }
  
//  public void PutFile(String ResurcePath, File f){
//    try {
//      DavPut put = new DavPut(getServerLocation());
//      put.setResourcePath(ResurcePath + "/" +f.getName());
//     
//      byte []dataByte = FileToByteArray.getBytesFromFile(f); 
//     
//      put.setRequestDataBuffer(dataByte);
//     
//      int status = put.execute();
//     
//      if (status != Const.HttpStatus.CREATED)
//        new Message(status);
//    } catch (Exception e) {}
//   }
  
//  public void PutDir(File f){
//    //Put2Dir(sResurcePath, f);
//     p2dt = new Put2DirThread(sResurcePath, f);
//     p2dt.start();
//  }
  public String getCurrentResurcePath(){
    return sResurcePath;
  }
  
//  public void Put2Dir(String ResurcePath, File f){
//    CreateFolder(ResurcePath ,f.getName());
//    
//    String curResurcePath = ResurcePath + "/" + f.getName();
//     
//    String[] listDir = f.list();
//    
//    for (int i = 0; i < listDir.length; i++) {
//      File fTemp = new File(f.getPath() + File.separator + listDir[i]);
//      if (fTemp.isDirectory()){
//        CreateFolder( curResurcePath, fTemp.getName());
//        Put2Dir(curResurcePath,fTemp);
//      }
//      else{
//        PutFile(curResurcePath,fTemp);
//        //Log.info(curResurcePath + "/" + fTemp.getName());
//      }
//    }
//  }
  
  public void GetFile(File f, String resurceDisplayName){
    try {
      
      DavGet get = new DavGet(getServerLocation());
      get.setResourcePath(sResurcePath + "/" + resurceDisplayName);
      
      int status = get.execute();
      
      if (status >= 400 /*Const.HttpStatus.OK*/ ){
        new Message(status);
      } else {
         FileToByteArray.BytesArrayToFile(f , get.getResponseDataBuffer());        
      }      
      
    } catch (Exception e) {}
  }
  public void GetVersionFile(File f, String RsurceVersionPath){
    try {
      
      DavGet get = new DavGet(getServerLocation());
      get.setResourcePath(RsurceVersionPath);
      
      int status = get.execute();
      
      if (status >= 400 /*Const.HttpStatus.OK*/ ){
        new Message(status);
      } else {
         FileToByteArray.BytesArrayToFile(f , get.getResponseDataBuffer());        
      }      
      
    } catch (Exception e) {}
  }
 
  public void resurceCopy(String resurceName){
    //if (resurceName.compareTo("..") == 0)resurceName = sParentResurcePath;
    if (resurceName.compareTo("..") == 0){
      sResurcePathCopy = sResurcePath + "/";
    } else {
      sResurcePathCopy = sResurcePath + "/" + resurceName; 
    }
    
    sResurceCopyName = resurceName;
    Log.info("res -->" + sResurcePathCopy);
    
  }
  
  public void resurcePaste(String resurceDestination){
    String sDestination = "";
    try {
      DavCopy copy = new DavCopy(getServerLocation());
            
      if (resurceDestination.compareTo("..") == 0){
        sDestination = sResurcePath + "/" + sResurceCopyName;
      } else {
        sDestination = sResurcePath + "/" + resurceDestination + "/" + sResurceCopyName;
      }
      
      sResurcePathCopy = Strip2Slash(sResurcePathCopy);
      sDestination = Strip2Slash(sDestination);
      
      copy.setResourcePath(sResurcePathCopy);
      copy.setDestinationPath(sDestination );
      
      Log.info("res -- " + sResurcePathCopy);
      Log.info ("destination " + sDestination);
      
      
      int status = copy.execute();
      
      if (status >= 400 /*Const.HttpStatus.CREATED*/ )
        new Message(status);
      
    } catch (Exception e) {}
  }
  
  public void resurceCut(String resurceName){
    if (resurceName.compareTo("..") == 0){
      sResurcePathCopy = sResurcePath + "/";
    } else {
      sResurcePathCopy = sResurcePath + "/" + resurceName; 
    }
    
    sResurceCopyName = resurceName;
    Log.info("res -->" + sResurcePathCopy);
  }
  
  public void resurceCutPaste(String resurceDestination){
    String sDestination = "";
    try {
      DavMove move = new DavMove(getServerLocation());
            
      if (resurceDestination.compareTo("..") == 0){
        sDestination = sResurcePath + "/" + sResurceCopyName;
      } else {
        sDestination = sResurcePath + "/" + resurceDestination + "/" + sResurceCopyName;
      }
      
      sResurcePathCopy = Strip2Slash(sResurcePathCopy);
      sDestination = Strip2Slash(sDestination);
      
      move.setResourcePath(sResurcePathCopy);
      move.setDestinationPath(sDestination);
      
      Log.info("res -- " + sResurcePathCopy);
      Log.info ("destination " + sDestination);
      
      
      int status = move.execute();
      
      if (status >= 400 /*Const.HttpStatus.CREATED*/ )
        new Message(status);
      
    } catch (Exception e) {}
  }
  
  public void resurceDelete(String resurceDisplyName){
    try {
      DavDelete delete = new DavDelete(getServerLocation());
      delete.setResourcePath(sResurcePath + "/" + resurceDisplyName);
      
      int status = delete.execute();

      if (status != Const.HttpStatus.NOCONTENT )
        new Message(status);
    } catch (Exception e) {}
  }
  
  public Vector getVersionReport(String resurceDisplyName ){
    Vector v = new Vector();
    try {
      DavReport report = new DavReport(getServerLocation());
      report.setResourcePath(sResurcePath + "/" + resurceDisplyName);
      report.setRequiredProperty(Const.DavProp.CREATORDISPLAYNAME);
      
      int status = report.execute();
      
      if (status != Const.HttpStatus.MULTISTATUS ){
        new Message(status);
      } else {
      
        DocumentApi doc = report.getMultistatus();
        ArrayList<ResponseDoc> responses = ((Multistatus)doc).getResponses();
        
        for (int i = 0; i < responses.size(); i++) {
          Vector vTemp = new Vector();
          ResponseDoc curResponse = (ResponseDoc)responses.get(i);
          CreatorDisplayNameProp pDisplayname = (CreatorDisplayNameProp)curResponse.getProperty(Const.DavProp.CREATORDISPLAYNAME);
          
          //Log.info("" + (i+1) + "CREATOR -->" +pDisplayname.getCreatorDisplayName() + " HREF --> " + curResponse.getHref());
          
          String hRef = TextUtils.UnEscape(curResponse.getHref(), '%');
            
          vTemp.add(pDisplayname.getCreatorDisplayName());
          vTemp.add(hRef);
          v.add(vTemp);
        }
        
      }
  
    } catch (Exception e) {}
    
    return v;
  }
  
  public boolean isLogin(){
    return bLogin;
  }
  
  public void setIsLogin(boolean Login){
    bLogin = Login;
  }
  
  public static String Strip2Slash( String str){
    String s = str.replaceAll("///", "/");
    s = s.replaceAll("//","/");
    return s; 
  }
}

