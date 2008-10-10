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
package org.exoplatform.services.jcr.ext.replication.test.concurrent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.replication.test.BaseReplicationTestCase;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: ConcurrentModificationTestCase.java 111 2008-11-11 11:11:11Z rainf0x $
 */

public class ConcurrentModificationTestCase extends BaseReplicationTestCase {

  public ConcurrentModificationTestCase(RepositoryService repositoryService,
                                        String reposytoryName,
                                        String workspaceName,
                                        String userName,
                                        String password) {
    super(repositoryService, reposytoryName, workspaceName, userName, password);
  }

  public StringBuffer createContent(String repoPath,
                                    String fileName,
                                    Long iterations,
                                    String simpleContent) {
    StringBuffer sb = new StringBuffer();

    log.info("ReplicationTestService.createContent run");
    long start, end;

    File tempFile = null;
    try {
      tempFile = File.createTempFile("tempF", "_");
      FileOutputStream fos = new FileOutputStream(tempFile);

      for (long i = 0; i < iterations; i++)
        fos.write(simpleContent.getBytes());
      fos.close();

      start = System.currentTimeMillis(); // to get the time of start

      Node cool = addNodePath(repoPath).addNode(fileName, "nt:file");
      Node contentNode = cool.addNode("jcr:content", "nt:resource");
      contentNode.setProperty("jcr:encoding", "UTF-8");
      contentNode.setProperty("jcr:data", new FileInputStream(tempFile));
      contentNode.setProperty("jcr:mimeType", "application/octet-stream");
      contentNode.setProperty("jcr:lastModified", session.getValueFactory()
                                                         .createValue(Calendar.getInstance()));

      session.save();

      end = System.currentTimeMillis();

      log.info("The time of the adding of nt:file : " + ((end - start) / BaseReplicationTestCase.ONE_SECONDS) + " sec");
      sb.append("ok");
    } catch (Exception e) {
      log.error("Can't save nt:file : ", e);
      sb.append("fail");
    } finally {
      tempFile.delete();
    }

    return sb;
  }

  public StringBuffer compareData(String srcRepoPath,
                                  String srcFileName,
                                  String destRepoPath,
                                  String destFileName) {
    StringBuffer sb = new StringBuffer();

    try {
      Node srcNode = ((Node) session.getItem(getNormalizePath(srcRepoPath))).getNode(srcFileName);
      Node destNode = ((Node) session.getItem(getNormalizePath(destRepoPath))).getNode(destFileName);

      InputStream srcStream = srcNode.getNode("jcr:content").getProperty("jcr:data").getStream();
      InputStream destStream = destNode.getNode("jcr:content").getProperty("jcr:data").getStream();

      compareStream(srcStream, destStream);

      log.info("ReplicationTestService.startThread run");
      sb.append("ok");
    } catch (Exception e) {
      log.error("Can't compare the data : ", e);
      sb.append("fail");
    }

    return sb;
  }

  public StringBuffer startThreadUpdater(String srcRepoPath,
                                         String srcFileName,
                                         String destRepoPath,
                                         String destFileName,
                                         Long iterations) {
    StringBuffer sb = new StringBuffer();

    try {
      Node srcNode = ((Node) session.getItem(getNormalizePath(srcRepoPath))).getNode(srcFileName);
      Node destNode = ((Node) session.getItem(getNormalizePath(destRepoPath))).getNode(destFileName);

      DataUpdaterThread updaterThread = new DataUpdaterThread(srcNode, destNode, iterations);
      updaterThread.start();

      log.info("ReplicationTestService.startThread run");
      sb.append("ok");
    } catch (Exception e) {
      log.error("Can't start the thread : ", e);
      sb.append("fail");
    }

    return sb;
  }

  class DataUpdaterThread extends Thread {
    private final Node srcNode;

    private final Node destNode;

    private final Long iterations;

    public DataUpdaterThread(Node srcNode, Node destNode, Long iterations) {
      this.srcNode = srcNode;
      this.destNode = destNode;
      this.iterations = iterations;
    }

    @Override
    public void run() {
      String destPath = null;
      try {
        destPath = destNode.getPath();
        for (int i = 0; i < iterations; i++) {
          InputStream srcStream = srcNode.getNode("jcr:content")
                                         .getProperty("jcr:data")
                                         .getStream();

          destNode.getNode("jcr:content").setProperty("jcr:data", srcStream);
          session.save();

          log.info(Calendar.getInstance().getTime().toGMTString() + " : ");
          log.info(this.getName() + " : " + "has been updated the 'nt:file' " + destPath
              + " : iterations == " + i);
        }
      } catch (RepositoryException e) {
        log.error("Can't update the 'nt:file' " + destPath + " : ", e);
      }
    }
  }
}
