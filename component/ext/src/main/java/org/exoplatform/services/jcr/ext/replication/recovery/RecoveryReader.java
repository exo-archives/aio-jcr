/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.recovery;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.ext.replication.FixupStream;
import org.exoplatform.services.jcr.ext.replication.PendingChangesLog;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id$
 */
public class RecoveryReader extends AbstractFSAccess {
  private static Log  log = ExoLogger.getLogger("ext.RecoveryReader");

  private FileCleaner fileCleaner;

  private File        recoveryDir;

  public RecoveryReader(FileCleaner fileCleaner, File recoveryDir) {
    this.fileCleaner = fileCleaner;
    this.recoveryDir = recoveryDir;
  }

  public TransactionChangesLog getChangesLog(String filePath) throws IOException,
                                                             ClassNotFoundException {
    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath));
    TransactionChangesLog tcl = readExternal(ois);

    ois.close();
    return tcl;
  }

  private TransactionChangesLog readExternal(ObjectInputStream in) throws IOException,
                                                                  ClassNotFoundException {
    int changesLogType = in.readInt();

    TransactionChangesLog transactionChangesLog = null;

    if (changesLogType == PendingChangesLog.Type.CHANGESLOG_WITH_STREAM) {

      // read ChangesLog
      transactionChangesLog = (TransactionChangesLog) in.readObject();

      // read FixupStream count
      int iFixupStream = in.readInt();

      ArrayList<FixupStream> listFixupStreams = new ArrayList<FixupStream>();

      for (int i = 0; i < iFixupStream; i++)
        listFixupStreams.add((FixupStream) in.readObject());

      // read stream data
      int iStreamCount = in.readInt();
      ArrayList<File> listFiles = new ArrayList<File>();

      for (int i = 0; i < iStreamCount; i++) {

        // read file size
        long fileSize = in.readLong();

        // read content file
        File contentFile = getAsFile(in, fileSize);
        listFiles.add(contentFile);
      }

      PendingChangesLog pendingChangesLog = new PendingChangesLog(transactionChangesLog,
                                                                  listFixupStreams,
                                                                  listFiles,
                                                                  fileCleaner);

      pendingChangesLog.restore();

//      TransactionChangesLog log = pendingChangesLog.getItemDataChangesLog();

    } else if (changesLogType == PendingChangesLog.Type.CHANGESLOG_WITHOUT_STREAM) {
      transactionChangesLog = (TransactionChangesLog) in.readObject();
    }

    return transactionChangesLog;
  }

  // return binary changes log up to date
  public List<String> getFilePathList(Calendar timeStamp, String ownName) throws IOException {
    File dataInfo = new File(recoveryDir.getAbsolutePath() + File.separator + ownName);

    List<String> list = new ArrayList<String>();

    if (dataInfo.exists()) {
      BufferedReader br = new BufferedReader(new FileReader(dataInfo));

      String sPath;

      while ((sPath = br.readLine()) != null) {
        if (sPath.startsWith(PREFIX_REMOVED_DATA) == false) {
          File f = new File(sPath);
          Calendar time = getTimeStamp(f.getName());

          if (timeStamp.after(time)) {
            list.add(sPath);

            if (log.isDebugEnabled())
              log.debug(sPath);
          } else
            break;
        }
      }
    }

    return list;
  }

  // return TimeStamp from file name.
  private Calendar getTimeStamp(String fileName) {
    // 20080415_090302_824_50e4cf9d7f000001009bb457938f425b
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
    Calendar timeStamp = Calendar.getInstance();

    try {
      timeStamp.setTime(dateFormat.parse(fileName));
    } catch (ParseException e) {
      log.error("Can't parce date", e);
    }
    return timeStamp;
  }
}
