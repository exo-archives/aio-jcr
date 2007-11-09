/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.applications.scale;

import java.io.IOException;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Alex Reshetnyak
 *          alex.reshetnyak@exoplatform.org.ua
 *          reshetnyak.alex@gmail.com		
 * 07.05.2007 15:51:14 
 * @version $Id: Scale.java 07.05.2007 15:51:14 rainfox 
 */
public class Scale {
  
  public static void main(String[] args) {
    
    Log log = ExoLogger.getLogger("repload.Scale");
   
    ScaleBase scaleBase = new ScaleBase(args);
    
    try {
      scaleBase.createFolder();
    } catch (RepositoryException e) {
      log.error("Error create folder", e);
    }

    try {
      scaleBase.deleteFolder();
    } catch (RepositoryException e) {
      log.error("Error delete folder", e);
    }

    try {
      scaleBase.uploadFile();
    } catch (RepositoryException e) {
      log.error("Error upload file", e);
    } catch (IOException e) {
      log.error("Error upload file", e);
    }

    try {
      scaleBase.downloadFile();
    } catch (RepositoryException e) {
      log.error("Error download file", e);
    } catch (IOException e) {
      log.error("Error download file", e);
    }
  }

}
