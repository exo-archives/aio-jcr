/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.applications.ooplugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class Log {
  
  public static final String LOGILENAME = "";

  private static RandomAccessFile rfile = null;
  
  private static void prepareOutFile() throws IOException {

    try {
      File outFolder = new File("/tmp");
      boolean created = false;
      if (!outFolder.exists()) {
        created = outFolder.mkdirs();
      }
      System.out.println("TEMP FOLDER CREATED: " + created);
      
    } catch (Exception exc) {
      System.out.println("Unhandled ecxeption. " + exc.getMessage());
      exc.printStackTrace(System.out);
    }    
    
    rfile = new RandomAccessFile("/tmp/exo-ooplugin.log", "rw");
    rfile.seek(rfile.length());

    ByteArrayOutputStream outStream = new ByteArrayOutputStream();    
    PrintStream outPrintStream = new PrintStream(outStream);
    outPrintStream.println("eXo Open Office Plugin Log file init...");
    rfile.write(outStream.toByteArray());
  }
  
  public static void info(String message) {
    System.out.println(message);
    try {
      if (rfile == null) {
        prepareOutFile();
      }

      ByteArrayOutputStream outStream = new ByteArrayOutputStream();    
      PrintStream outPrintStream = new PrintStream(outStream);      
      //outPrintStream.println("================================================");
      outPrintStream.println(message);
      
      rfile.write(outStream.toByteArray());      
    } catch (Exception exc) {
      System.out.println("Unhandled exception. " + exc.getMessage());
      exc.printStackTrace();
    }
  }
  
  public static void info(String message, Throwable thr) {
    System.out.println(message);
    try {
      if (rfile == null) {
        prepareOutFile();
      }
      
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();    
      PrintStream outPrintStream = new PrintStream(outStream);
      //outPrintStream.println("================================================");
      outPrintStream.println(message + " " + thr.getMessage());
      thr.printStackTrace(outPrintStream);
      
      rfile.write(outStream.toByteArray());
    } catch (Exception exc) {
      System.out.println("Unhandled exception. " + exc.getMessage());
      exc.printStackTrace();
    }
  }  
  
}
