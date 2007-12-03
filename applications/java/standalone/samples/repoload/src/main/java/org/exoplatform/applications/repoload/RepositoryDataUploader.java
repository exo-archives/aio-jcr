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
package org.exoplatform.applications.repoload;

import org.apache.commons.logging.Log;
import org.exoplatform.applications.scale.Scale;
import org.exoplatform.applications.scale.ScaleBase;
import org.exoplatform.services.log.ExoLogger;


/**
 * Created by The eXo Platform SAS Author : Alex Reshetnyak
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

      if (isThreads(args))
        dataReader = new DataReader(args, "ThReader");
      else
        dataReader = new DataReader(args);

    else if (isScaleTest(args))
      Scale.main(args);
    else
      try {

        if (isAPIRead(args)) {

          DataUploaderAPI dataUploaderApi = new DataUploaderAPI(args);

          dataUploaderApi.initRepository();

          start = System.currentTimeMillis(); // to get the time of start

          try {
            dataUploaderApi.uploadDataAPI();
          } catch (Exception e) {
            log.info("Error upload data", e);
          }

          end = System.currentTimeMillis();

          dataUploaderApi.readData();

          log.info("The time of the adding of " + dataUploaderApi.countNodes + " nodes: "
              + ((end - start) / 1000.0) + " sec");

        } else {
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
        }

      } catch (Exception e) {
        e.printStackTrace();
        log.info("Error upload data", e);
      }
    System.exit(0);
  }

  private static boolean isRead(String[] args) {
    for (int i = 0; i < args.length; i++)
      if (args[i].equals("-read"))
        return true;
    return false;
  }

  private static boolean isWrite(String[] args) {
    for (int i = 0; i < args.length; i++)
      if (args[i].equals("-write"))
        return true;
    return false;
  }

  private static boolean isThreads(String[] args) {
    for (int i = 0; i < args.length; i++) {
      String[] pair = args[i].split("=");
      if (pair[0].equals("-threads"))
        return true;
    }
    return false;
  }

  private static boolean isAPIRead(String[] args) {
    for (int i = 0; i < args.length; i++) {
      String[] pair = args[i].split("=");
      if (pair[0].equals("-api"))
        return true;
    }
    return false;
  }

  private static boolean isScaleTest(String[] args) {
    for (int i = 0; i < args.length; i++)
      if (args[i].equals("-scale"))
        return true;
    return false;
  }
}

