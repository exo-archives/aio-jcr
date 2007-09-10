/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.jcr.benchmark.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by The eXo Platform SARL Author : Obmanyuk Vitaliy
 * obmanyuk.vitaliy@exoplatform.com.ua
 */
public class SimpleReportHelper {

  public static Date TODAY = new Date();

  /**
   * @param args
   */
  public static void main(String[] args) {
    // copying additional configs
    String reportsDir = "../reports/";
    String lastDirName = reportsDir + "last/";
    String srcConfig1 = "../config/test-configuration-benchmark.xml";
    String destConfig1 = lastDirName + "test-configuration-benchmark.xml";
    String srcConfig2 = "../config/test-jcr-config-benchmark.xml";
    String destConfig2 = lastDirName + "test-jcr-config-benchmark.xml";
    String message = "Copying passed ";
    try {
      {
        FileChannel srcChannel = new FileInputStream(srcConfig1).getChannel();
        FileChannel destChannel = new FileOutputStream(destConfig1).getChannel();
        destChannel.transferFrom(srcChannel, 0, srcChannel.size());
        srcChannel.close();
        destChannel.close();
      }
      {
        FileChannel srcChannel = new FileInputStream(srcConfig2).getChannel();
        FileChannel destChannel = new FileOutputStream(destConfig2).getChannel();
        destChannel.transferFrom(srcChannel, 0, srcChannel.size());
        srcChannel.close();
        destChannel.close();
      }
      message += "SUCCESSFULLY";
    } catch (IOException e) {
      e.printStackTrace();
      message += "ERROR";
    }
    System.out.println(message);
    // renaming reports directory
    message = "Renaming of 'last' directory passed ";
    DateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
    String newDirName = reportsDir + df.format(TODAY) + "-exo";
    File oldDir = new File(lastDirName);
    File newDir = new File(newDirName);
    boolean success = oldDir.renameTo(newDir);
    if (success) {
      message += "SUCCESSFULLY";
    } else {
      message += "ERROR";
    }
    System.out.println(message);
  }

}
