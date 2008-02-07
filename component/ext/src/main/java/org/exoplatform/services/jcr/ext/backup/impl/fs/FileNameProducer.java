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
package org.exoplatform.services.jcr.ext.backup.impl.fs;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by The eXo Platform SARL Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.com.ua Nov 20, 2007
 */
public class FileNameProducer {
  private String  serviceDir;
  private String  backupSetName;
  private File    backupSetDir;
  private boolean isFullBackup;
  private Calendar timeStamp;  

  public FileNameProducer(String backupSetName, String serviceDir, Calendar timeStamp, boolean isFullBackup) {
    this.backupSetName = backupSetName;
    this.serviceDir = serviceDir;
    this.isFullBackup = isFullBackup;
    this.timeStamp = timeStamp;
  }

  public FileNameProducer(String repositoryName, String workspaceName, String serviceDir, Calendar timeStamp, 
      boolean isFullBackup) {
    this.backupSetName = repositoryName + "_" + workspaceName;
    this.serviceDir = serviceDir;
    this.isFullBackup = isFullBackup;
    this.timeStamp = timeStamp;
  }

  public File getNextFile() {
    File nextFile = null;

    try {
      String sTime = "-" + getStrDate(timeStamp) + "_" + getStrTime(timeStamp);
      
      backupSetDir = new File(serviceDir + File.separator + backupSetName + sTime);

      if (!backupSetDir.exists())
        backupSetDir.mkdirs();

      String sNextName = generateName();

      nextFile = new File(backupSetDir.getAbsoluteFile() + File.separator + sNextName);
      nextFile.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return nextFile;
  }

  private String generateName() {

    Calendar date = Calendar.getInstance();
    
    String sDate = getStrDate(date);
    String sTime = getStrTime(date);

    String fileName = backupSetName + "-"+ sDate +"_" + sTime + ".";

    if (isFullBackup)
      fileName += "0";
    else
      fileName += getNextSufix();

    return fileName;
  }

  private String getNextSufix() {

    String[] fileList = backupSetDir.list();

    int sufix = 0;

    for (int i = 0; i < fileList.length; i++) {

      String[] stringArray = fileList[i].split("[.]");

      int currentSufix = Integer.valueOf(stringArray[1]).intValue();

      if (currentSufix > sufix)
        sufix = currentSufix;
    }

    return String.valueOf(++sufix);
  }

  public static String getStrDate(Calendar c) {
    // Returns as a String (YYYYMMDD) a Calendar date
    int m = c.get(Calendar.MONTH) + 1;
    int d = c.get(Calendar.DATE);
    return "" + c.get(Calendar.YEAR) + (m < 10 ? "0" + m : m) + (d < 10 ? "0" + d : d);
  }
  
  public static String getStrTime(Calendar c) {
    // Returns as a String (YYYYMMDD) a Calendar date
    int h = c.get(Calendar.HOUR);
    int m = c.get(Calendar.MINUTE);
    int s = c.get(Calendar.SECOND);
    return "" + (h < 10 ? "0" + h : h) + (m < 10 ? "0" + m : m) + (s < 10 ? "0" + s : s);
  }

}
