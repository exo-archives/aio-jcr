/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.applications.repoload;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;


/**
 * Created by The eXo Platform SARL Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.com.ua reshetnyak.alex@gmail.com 23.03.2007
 * 13:20:00
 * 
 * @version $Id: RepositoryDataUploader.java 23.03.2007 13:20:00 rainfox
 */


public class RepositoryDataUploader {
  
  protected static Log log = ExoLogger.getLogger("repload.RepositoryDataUploader");
  
  public static void main(String[] args) {
    long start, end;
    DataReader dataReader;
    DataUploader dataUploader;
    
    if (isRead(args))
      dataReader = new DataReader(args);
    else
      try {
        dataUploader = new DataUploader(args);
  
        dataUploader.initRepository();
  
        start = System.currentTimeMillis(); // to get the time of start
        
        try {
          dataUploader.uploadData();
        } catch (Exception e) {
          log.info("Error upload data", e);
        }
        
        end = System.currentTimeMillis();
        
        dataUploader.readData();
  
        log.info("The time of the adding of " + dataUploader.countNodes + " nodes: "
            + ((end - start) / 1000.0) + " sec");
      } catch (Exception e) {
        e.printStackTrace();
        log.error("Error upload data", e);
      }
  }
  
  private static boolean isRead(String[] args ){
    for (int i = 0; i < args.length; i++)
      if (args[i].equals("-read"))
        return true;  
    return false;
    
  }

  private static boolean isWrite(String[] args ){
    for (int i = 0; i < args.length; i++)
      if (args[i].equals("-write"))
        return true;  
    return false;
    
  }
  
}
