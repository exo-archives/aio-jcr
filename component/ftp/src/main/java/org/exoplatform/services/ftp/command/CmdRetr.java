/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.ftp.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.services.ftp.FtpConst;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class CmdRetr extends FtpCommandImpl {

  private static Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "CmdRetr");
  
  public CmdRetr() {
    commandName = FtpConst.Commands.CMD_RETR; 
  }
  
  public void run(String []params) throws IOException {
    if (clientSession().getDataTransiver() == null) {
      reply(FtpConst.Replyes.REPLY_425);
      return;
    }
    
    if (params.length < 2) {
      reply(String.format(FtpConst.Replyes.REPLY_500_PARAMREQUIRED, FtpConst.Commands.CMD_RETR));
      return;
    }
    
    String resName = params[1];
    
    boolean isResource = IsResource(resName);
    if (!isResource) {
      reply(String.format(FtpConst.Replyes.REPLY_550, resName));
      return;
    }
    
    try {      
      ArrayList<String> newPath = clientSession().getFullPath(resName);
      Session curSession = clientSession().getSession(newPath.get(0));   
      String repoPath = clientSession().getRepoPath(newPath);

      Node parentNode = (Node)curSession.getItem(repoPath);      
      Node dataNode = parentNode.getNode(FtpConst.NodeTypes.JCR_CONTENT);
      
      Property dataProp = dataNode.getProperty(FtpConst.NodeTypes.JCR_DATA);

      InputStream inStream = dataProp.getStream();
      
      if (FtpConst.Commands.CMD_REST.equals(clientSession().getPrevCommand())) {
        String prevVal = clientSession().getPrevParams();      
        
        int seekPos = new Integer(prevVal);
        
        if (seekPos > inStream.available()) {
          reply(FtpConst.Replyes.REPLY_550_RESTORE);
          return;
        }
        
        for (int i = 0; i < seekPos; i++) {
          inStream.read();
        }
      }

      while (!clientSession().getDataTransiver().isConnected()) {        
        Thread.sleep(100);
      }

      reply(FtpConst.Replyes.REPLY_125);
      
      int BUFFER_SIZE = 4096;
      
      try {        
        byte []buffer = new byte[BUFFER_SIZE];
        OutputStream outStream = clientSession().getDataTransiver().getOutputStream(); 
        
        while (true) {
          int readed = inStream.read(buffer, 0, BUFFER_SIZE);
          if (readed < 0) {
            break;
          }
          outStream.write(buffer, 0, readed);
        }        
        
      } catch (Exception exc) {        
        reply(FtpConst.Replyes.REPLY_451);
        return;
      } finally {
        clientSession().closeDataTransiver();
      }
      reply(FtpConst.Replyes.REPLY_226);
      return;
    } catch (Throwable exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
    }
    
    clientSession().closeDataTransiver();
    reply(String.format(FtpConst.Replyes.REPLY_550, resName));
  }
  
  public boolean IsResource(String resName) {
    ArrayList<String> newPath = clientSession().getFullPath(resName);
    try {
      String repoPath = clientSession().getRepoPath(newPath);
      Session curSession = clientSession().getSession(newPath.get(0));
      
      Node parentNode = (Node)curSession.getItem(repoPath);
      if (parentNode.isNodeType(FtpConst.NodeTypes.NT_FILE)) {
        return true;
      }
    } catch (PathNotFoundException exc) {
    } catch (NoSuchWorkspaceException wexc) {
    } catch (Throwable exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
    }
    return false;
  }
  
}
