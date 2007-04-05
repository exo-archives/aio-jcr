/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.applications.repoload;


/**
 * Created by The eXo Platform SARL Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.com.ua reshetnyak.alex@gmail.com 23.03.2007
 * 13:20:00
 * 
 * @version $Id: RepositoryDataUploader.java 23.03.2007 13:20:00 rainfox
 */


public class RepositoryDataUploader {

  public static void main(String[] args) {
    long start, end;

    try {
      DataUploader dataUploader = new DataUploader(args);

      dataUploader.initRepository();

      start = System.currentTimeMillis(); // to get the time of start
      
      dataUploader.uploadData();
      
      end = System.currentTimeMillis();
      
      dataUploader.readData();

      System.out.println("The time of the adding of " + dataUploader.countNodes + " nodes: "
          + ((end - start) / 1000.0) + " sec");
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Error upload data");
    }
  }
}
