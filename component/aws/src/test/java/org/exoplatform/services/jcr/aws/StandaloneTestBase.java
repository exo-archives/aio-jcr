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
package org.exoplatform.services.jcr.aws;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;

import junit.framework.TestCase;

import org.exoplatform.services.log.Log;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.CredentialsImpl;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public abstract class StandaloneTestBase extends TestCase {

  /**
   * Logger.
   */
  protected static final Log    LOG = ExoLogger.getLogger("jcr.JCRAWSTest");

  protected SessionImpl         session;

  protected RepositoryImpl      repository;

  protected CredentialsImpl     credentials;

  protected Workspace           workspace;

  protected RepositoryService   repositoryService;

  protected Node                root;

  protected ValueFactory        valueFactory;

  protected StandaloneContainer container;

  /**
   * CompareStreamException.
   * 
   */
  protected class CompareStreamException extends Exception {

    CompareStreamException(String message) {
      super(message);
    }

    CompareStreamException(String message, Throwable e) {
      super(message, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void setUp() throws Exception {
    String containerConf = getClass().getResource("/conf/standalone/test-configuration.xml")
                                     .toString();
    String loginConf = Thread.currentThread()
                             .getContextClassLoader()
                             .getResource("login.conf")
                             .toString();

    StandaloneContainer.addConfigurationURL(containerConf);
    container = StandaloneContainer.getInstance();

    if (System.getProperty("java.security.auth.login.config") == null)
      System.setProperty("java.security.auth.login.config", loginConf);

    credentials = new CredentialsImpl("root", "exo".toCharArray());

    repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    // container.start();
    repository = (RepositoryImpl) repositoryService.getDefaultRepository();

    session = (SessionImpl) repository.login(credentials, "ws");
    workspace = session.getWorkspace();
    root = session.getRootNode();
    valueFactory = session.getValueFactory();
  }

  /**
   * {@inheritDoc}
   */
  protected void tearDown() throws Exception {

    LOG.info("tearDown() BEGIN " + getClass().getName() + "." + getName());
    if (session != null) {
      try {
        session.refresh(false);
        Node rootNode = session.getRootNode();
        if (rootNode.hasNodes()) {
          // clean test root
          for (NodeIterator children = rootNode.getNodes(); children.hasNext();) {
            Node node = children.nextNode();
            if (!node.getPath().startsWith("/jcr:system")
                && !node.getPath().startsWith("/exo:audit")) {
              // log.info("DELETing ------------- "+node.getPath());
              node.remove();
            }
          }
          session.save();
        }
      } catch (Exception e) {
        e.printStackTrace();
        LOG.error("===== Exception in tearDown() " + e.toString());
      } finally {
        session.logout();
      }
    }

    super.tearDown();
    // log.info("tearDown() END " + getClass().getName() + "." + getName());
  }

  // ====== utils =======

  protected void checkItemsExisted(String[] exists, String[] notExists) throws RepositoryException {
    String path = null;
    if (exists != null) {
      try {
        for (String nodePath : exists) {
          path = nodePath;
          session.getItem(path);
        }
      } catch (PathNotFoundException e) {
        fail("Item must exists " + path + ". " + e.getMessage());
      }
    }
    if (notExists != null) {
      try {
        for (String nodePath : notExists) {
          session.getItem(nodePath);
          fail("Item must not exists " + nodePath);
        }
      } catch (PathNotFoundException e) {
        // ok
      }
    }
  }

  protected void checkNodesExistedByUUID(String[] exists, String[] notExists) throws RepositoryException {
    String uuid = null;
    if (exists != null) {
      try {
        for (String nodePath : exists) {
          uuid = nodePath;
          session.getNodeByUUID(uuid);
        }
      } catch (PathNotFoundException e) {
        fail("Node must exists, UUID " + uuid + ". " + e.getMessage());
      }
    }
    if (notExists != null) {
      try {
        for (String nodeUUID : notExists) {
          session.getNodeByUUID(nodeUUID);
          fail("Node must not exists, UUID " + nodeUUID);
        }
      } catch (PathNotFoundException e) {
        // ok
      }
    }
  }

  protected void compareStream(InputStream etalon, InputStream data) throws IOException {
    try {
      compareStream(etalon, data, 0, 0, -1);
    } catch (CompareStreamException e) {
      fail(e.getMessage());
    }
  }

  /**
   * Compare etalon stream with data stream begining from the offset in etalon and position in data.
   * Length bytes will be readed and compared. if length is lower 0 then compare streams till one of
   * them will be read.
   * 
   * @param etalon
   *          etalon stream
   * @param data
   *          data stream
   * @param etalonPos
   *          etalon position
   * @param length
   *          length to check
   * @param dataPos
   *          data position
   * @throws IOException
   *           error
   */
  protected void compareStream(InputStream etalon,
                               InputStream data,
                               long etalonPos,
                               long dataPos,
                               long length) throws IOException, CompareStreamException {

    int dindex = 0;

    skipStream(etalon, etalonPos);
    skipStream(data, dataPos);

    byte[] ebuff = new byte[1024];
    int eread = 0;

    while ((eread = etalon.read(ebuff)) > 0) {

      byte[] dbuff = new byte[eread];
      int erindex = 0;
      while (erindex < eread) {
        int dread = -1;
        try {
          dread = data.read(dbuff);
        } catch (IOException e) {
          throw new CompareStreamException("Streams is not equals by length or data stream is unreadable. Cause: "
              + e.getMessage());
        }

        if (dread == -1)
          throw new CompareStreamException("Streams is not equals by length. Data end-of-stream reached at position "
              + dindex);

        for (int i = 0; i < dread; i++) {
          byte eb = ebuff[i];
          byte db = dbuff[i];
          if (eb != db)
            throw new CompareStreamException("Streams is not equals. Wrong byte stored at position "
                + dindex
                + " of data stream. Expected 0x"
                + Integer.toHexString(eb)
                + " '"
                + new String(new byte[] { eb })
                + "' but found 0x"
                + Integer.toHexString(db)
                + " '"
                + new String(new byte[] { db }) + "'");

          erindex++;
          dindex++;
          if (length > 0 && dindex >= length)
            return; // tested length reached
        }

        if (dread < eread)
          dbuff = new byte[eread - dread];
      }
    }

    if (data.available() > 0)
      throw new CompareStreamException("Streams is not equals by length. Data stream contains more data. Were read "
          + dindex);
  }

  protected void skipStream(InputStream stream, long pos) throws IOException {
    long curPos = pos;
    long sk = 0;
    while ((sk = stream.skip(curPos)) > 0) {
      curPos -= sk;
    }
    ;
    if (sk < 0)
      fail("Can not read the stream (skip bytes)");
    if (curPos != 0)
      fail("Can not skip bytes from the stream (" + pos + " bytes)");
  }

  protected File createBLOBTempFile(int sizeInKb) throws IOException {
    return createBLOBTempFile("exo_jcr_test_temp_file_", sizeInKb);
  }

  protected File createBLOBTempFile(String prefix, int sizeInKb) throws IOException {
    // create test file
    byte[] data = new byte[1024]; // 1Kb

    File testFile = File.createTempFile(prefix, ".tmp");
    FileOutputStream tempOut = new FileOutputStream(testFile);
    Random random = new Random();

    for (int i = 0; i < sizeInKb; i++) {
      random.nextBytes(data);
      tempOut.write(data);
    }
    tempOut.close();
    testFile.deleteOnExit(); // delete on test exit
    LOG.info("Temp file created: " + testFile.getAbsolutePath() + " size: " + testFile.length());
    return testFile;
  }

  protected void checkMixins(String[] mixins, NodeImpl node) {
    try {
      String[] nodeMixins = node.getMixinTypeNames();
      assertEquals("Mixins count is different", mixins.length, nodeMixins.length);

      compareMixins(mixins, nodeMixins);
    } catch (RepositoryException e) {
      fail("Mixins isn't accessible on the node " + node.getPath());
    }
  }

  protected void compareMixins(String[] mixins, String[] nodeMixins) {
    nextMixin: for (String mixin : mixins) {
      for (String nodeMixin : nodeMixins) {
        if (mixin.equals(nodeMixin))
          continue nextMixin;
      }

      fail("Mixin '" + mixin + "' isn't accessible");
    }
  }

  protected String memoryInfo() {
    String info = "";
    info = "free: " + mb(Runtime.getRuntime().freeMemory()) + "M of "
        + mb(Runtime.getRuntime().totalMemory()) + "M (max: "
        + mb(Runtime.getRuntime().maxMemory()) + "M)";
    return info;
  }

  // bytes to Mbytes
  protected String mb(long mem) {
    return String.valueOf(Math.round(mem * 100d / (1024d * 1024d)) / 100d);
  }

  protected String execTime(long from) {
    return Math.round(((System.currentTimeMillis() - from) * 100.00d / 60000.00d)) / 100.00d
        + "min";
  }

}
