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
package org.exoplatform.services.jcr.ext.replication.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id$
 */

public class NtFileTestCase extends BaseReplicationTestCase {

  /**
   * NtFileTestCase  constructor.
   *
   * @param repositoryService
   *          the RepositoryService.
   * @param reposytoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   */
  public NtFileTestCase(RepositoryService repositoryService,
                        String reposytoryName,
                        String workspaceName,
                        String userName,
                        String password) {
    super(repositoryService, reposytoryName, workspaceName, userName, password);
    log.info("NtFileTestCase inited");
  }

  /**
   * addNtFile.
   *
   * @param repoPath
   *          repository path
   * @param fileName
   *          the name of nt:file node
   * @param fileSize
   *          the file size
   * @return StringBuffer
   *           return the responds {'ok', 'fail'}
   */
  public StringBuffer addNtFile(String repoPath, String fileName, Long fileSize) {
    StringBuffer sb = new StringBuffer();

    log.info("ReplicationTestService.addNTFile run");
    long start, end;
    byte[] buf = new byte[BUFFER_SIZE];

    File tempFile = null;
    try {
      tempFile = File.createTempFile("tempF", "_");
      FileOutputStream fos = new FileOutputStream(tempFile);

      for (int i = 0; i < buf.length; i++)
        buf[i] = (byte) (i % DIVIDER);

      for (long i = 0; i < fileSize / BUFFER_SIZE; i++)
        fos.write(buf);
      fos.write(buf, 0, (int) (fileSize % BUFFER_SIZE));
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

      log.info("The time of the adding of nt:file : " + ((end - start) / ONE_SECONDS) + " sec");
      sb.append("ok");
    } catch (Exception e) {
      log.error("Can't save nt:file : ", e);
      sb.append("fail");
    } finally {
      tempFile.delete();
    }

    return sb;
  }

  /**
   * checkNtFile.
   *
   * @param repoPath
   *          repository path
   * @param fileName
   *          the name of nt:file node
   * @param fileSize
   *          the file size
   * @return StringBuffer
   *           return the responds {'ok', 'fail'}
   */
  public StringBuffer checkNtFile(String repoPath, String fileName, Long fileSize) {
    StringBuffer sb = new StringBuffer();

    String normalizePath = getNormalizePath(repoPath);
    try {
      Node checkNode = (Node) session.getItem(normalizePath);

      Node ntFile = checkNode.getNode(fileName);

      InputStream stream = ntFile.getNode("jcr:content").getProperty("jcr:data").getStream();

      byte buf[] = new byte[BUFFER_SIZE];
      long length = 0;
      int lenReads = 0;
      while ((lenReads = stream.read(buf)) > 0)
        length += lenReads;

      if (length == fileSize)
        sb.append("ok");
      else
        sb.append("fail");
    } catch (PathNotFoundException e) {
      log.error("Can't get node : " + normalizePath, e);
      sb.append("fail");
    } catch (RepositoryException e) {
      log.error("CheckNtFile fail", e);
      sb.append("fail");
    } catch (Exception e) {
      log.error("CheckNtFile fail", e);
      sb.append("fail");
    }

    return sb;
  }
}
