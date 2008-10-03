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
package org.exoplatform.services.jcr.ext.replication.test.bandwidth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.replication.test.BaseReplicationTestCase;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: BandwidthAllocationTestCase.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class BandwidthAllocationTestCase extends BaseReplicationTestCase {

  private final String alphabet = "qwertyuiop[]asdfghjkl;'zxcvbnm,./1234567890-=!@#$%^&*()_+|:?><";

  public BandwidthAllocationTestCase(RepositoryService repositoryService,
                                     String reposytoryName,
                                     String workspaceName,
                                     String userName,
                                     String password) {
    super(repositoryService, reposytoryName, workspaceName, userName, password);
  }

  public StringBuffer createBaseNode(String repoPath, String nodeName) {
    StringBuffer sb = new StringBuffer();

    try {
      Node baseNode = addNodePath(repoPath).addNode(nodeName, "nt:unstructured");
      session.save();

      sb.append("ok");
    } catch (RepositoryException e) {
      log.error("Can't locked: ", e);
      sb.append("fail");
    }

    return sb;
  }

  public StringBuffer addEmptyNode(String repoPath, String nodeName, long iterations) {
    StringBuffer sb = new StringBuffer();

    try {
      for (int i = 0; i < iterations; i++) {
        String normalizePath = getNormalizePath(repoPath);
        Node baseNode = (Node) session.getItem(normalizePath);

        Node emptyNode = baseNode.addNode(nodeName + "_" + i, "nt:base");

        session.save();
      }

      sb.append("ok");
    } catch (RepositoryException e) {
      log.error("Can't locked: ", e);
      sb.append("fail");
    }

    return sb;
  }

  public StringBuffer addStringPropertyOnly(String repoPath,
                                            String nodeName,
                                            Long size,
                                            long iterations) {
    StringBuffer sb = new StringBuffer();

    try {
      // create random value
      String sValue = "";
      for (int i = 0; i < size; i++) {
        int sIndex = (int) (Math.random() * 1124517) % alphabet.length();
        sValue += alphabet.substring(sIndex, sIndex + 1);
      }

      for (int i = 0; i < iterations; i++) {
        String normalizePath = getNormalizePath(repoPath);
        Node baseNode = ((Node) session.getItem(normalizePath)).getNode(nodeName);
        baseNode.setProperty("d", sValue);
        // log.info("ADD propety + " + sValue.length() + " B");
        session.save();
      }

      sb.append("ok");
    } catch (RepositoryException e) {
      log.error("Can't add the string propery: ", e);
      sb.append("fail");
    }

    return sb;
  }

  public StringBuffer addBinaryPropertyOnly(String repoPath,
                                            String nodeName,
                                            Long size,
                                            long iterations) {
    StringBuffer sb = new StringBuffer();

    long start, end;
    byte[] buf = new byte[BUFFER_SIZE];

    File tempFile = null;
    try {
      tempFile = File.createTempFile("tempF", "_");
      FileOutputStream fos = new FileOutputStream(tempFile);

      for (int i = 0; i < buf.length; i++)
        buf[i] = (byte) (i % 255);

      for (long i = 0; i < size / BUFFER_SIZE; i++)
        fos.write(buf);
      fos.write(buf, 0, (int) (size % BUFFER_SIZE));
      fos.close();

      start = System.currentTimeMillis(); // to get the time of start
      for (int i = 0; i < iterations; i++) {
        String normalizePath = getNormalizePath(repoPath);
        Node baseNode = ((Node) session.getItem(normalizePath)).getNode(nodeName);
        baseNode.setProperty("d", new FileInputStream(tempFile));

        session.save();
      }

      end = System.currentTimeMillis();

      log.info("The time of the adding of nt:file + " + iterations + "( " + tempFile.length()
          + " B ) : " + ((end - start) / 1000) + " sec");

      sb.append("ok");
    } catch (Exception e) {
      log.error("Can't save the binary value : ", e);
      sb.append("fail");
    } finally {
      tempFile.delete();
    }

    return sb;
  }

}
