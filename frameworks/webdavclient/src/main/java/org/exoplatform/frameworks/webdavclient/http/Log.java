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

package org.exoplatform.frameworks.webdavclient.http;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class Log {
  
//  public static final String LOGILENAME = "";
  
//  protected static PrintStream outPrintStream;

//  private static RandomAccessFile rfile = null;
  
//  private static void prepareOutFile() throws IOException {
//
//    try {
//      File outFolder = new File("/tmp");
//      boolean created = false;
//      if (!outFolder.exists()) {
//        created = outFolder.mkdirs();
//      }
//      System.out.println("TEMP FOLDER CREATED: " + created);
//      
//    } catch (Exception exc) {
//      System.out.println("Unhandled ecxeption. " + exc.getMessage());
//      exc.printStackTrace(System.out);
//    }    
//    
//    rfile = new RandomAccessFile("/tmp/exo-ooplugin.log", "rw");
//    rfile.seek(rfile.length());
//
//    ByteArrayOutputStream outStream = new ByteArrayOutputStream();    
//    outPrintStream = new PrintStream(outStream);
//    outPrintStream.println("eXo Open Office Plugin Log file init...");
//    rfile.write(outStream.toByteArray());
//  }
  
  public static void info(String message) {
    System.out.println(message);
//    try {
//      if (outPrintStream == null) {
//    	  prepareOutFile();  
//      }
//    	
//      ByteArrayOutputStream outStream = new ByteArrayOutputStream();    
//      PrintStream outPrintStream = new PrintStream(outStream);      
//      //outPrintStream.println("================================================");
//      outPrintStream.println(message);
//      
//      rfile.write(outStream.toByteArray());      
//    } catch (Exception exc) {
//      System.out.println("Unhandled exception. " + exc.getMessage());
//      exc.printStackTrace();
//    }
  }
  
  public static void info(String message, Throwable thr) {
    System.out.println(message);
    thr.printStackTrace(System.out);
//    try {
//      if (rfile == null) {
//        prepareOutFile();
//      }
//      
//      ByteArrayOutputStream outStream = new ByteArrayOutputStream();    
//      PrintStream outPrintStream = new PrintStream(outStream);
//      //outPrintStream.println("================================================");
//      outPrintStream.println(message + " " + thr.getMessage());
//      thr.printStackTrace(outPrintStream);
//      
//      rfile.write(outStream.toByteArray());
//    } catch (Exception exc) {
//      System.out.println("Unhandled exception. " + exc.getMessage());
//      exc.printStackTrace();
//    }
  }  
  
}
