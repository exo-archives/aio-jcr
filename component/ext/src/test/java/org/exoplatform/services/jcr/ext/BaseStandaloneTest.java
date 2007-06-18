package org.exoplatform.services.jcr.ext;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * Created by The eXo Platform SARL .
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: BaseStandaloneTest.java 12004 2007-01-17 12:03:57Z geaz $
 */
public abstract class BaseStandaloneTest extends TestCase {

  protected static Log log = ExoLogger.getLogger("jcr.JCRExtTest");

  protected SessionImpl session;

  protected RepositoryImpl repository;

  protected CredentialsImpl credentials;

  protected Workspace workspace;

  protected RepositoryService repositoryService;

  protected Node root;

  protected ValueFactory valueFactory;

  protected StandaloneContainer container;

  public void setUp() throws Exception {

       StandaloneContainer
          .addConfigurationPath("src/main/java/conf/standalone/test/test-configuration.xml");

      container = StandaloneContainer.getInstance();

      if (System.getProperty("java.security.auth.login.config") == null)
        System.setProperty("java.security.auth.login.config",
            "src/main/resources/login.conf");

      credentials = new CredentialsImpl("exo", "exo".toCharArray());

      repositoryService = (RepositoryService) container
          .getComponentInstanceOfType(RepositoryService.class);

      repository = (RepositoryImpl) repositoryService.getDefaultRepository();

      // repository.getContainer().start();

      if (!repository.isWorkspaceInitialized("ws"))
        repository.initWorkspace("ws", "nt:unstructured");

      session = (SessionImpl) repository.login(credentials, "ws");
      workspace = session.getWorkspace();
      root = session.getRootNode();
      valueFactory = session.getValueFactory();
  }

  protected void tearDown() throws Exception {
//
//    log.info("tearDown() BEGIN " + getClass().getName() + "." + getName());
//    if (session != null) {
//      try {
//        session.refresh(false);
//        Node rootNode = session.getRootNode();
//        if (rootNode.hasNodes()) {
//          // clean test root
//          for (NodeIterator children = rootNode.getNodes(); children.hasNext();) {
//            Node node = children.nextNode();
//            if (!node.getPath().startsWith("/jcr:system")) {
////              log.info("DELETing ------------- "+node.getPath());
//              node.remove();
//            }
//          }
//          session.save();
//        }
//      } catch (Exception e) {
//        e.printStackTrace();
//        log.error("===== Exception in tearDown() " + e.toString());
//      } finally {
//        session.logout();
//      }
//    }
    super.tearDown();

    //log.info("tearDown() END " + getClass().getName() + "." + getName());
  }

//  protected abstract String getConfPath();
//
//  public void initRepository() throws RepositoryException {
//  }

  // ====== utils =======

  protected void checkItemsExisted(String[] exists, String[] notExists) throws RepositoryException {
    String path = null;
    if (exists != null) {
      try {
        for (String nodePath: exists) {
          path = nodePath;
          session.getItem(path);
        }
      } catch(PathNotFoundException e) {
        fail("Item must exists " + path + ". " + e.getMessage());
      }
    }
    if (notExists != null) {
      try {
        for (String nodePath: notExists) {
          session.getItem(nodePath);
          fail("Item must not exists " + nodePath);
        }
      } catch(PathNotFoundException e) {
        // ok
      }
    }
  }

  protected void checkNodesExistedByUUID(String[] exists, String[] notExists) throws RepositoryException {
    String uuid = null;
    if (exists != null) {
      try {
        for (String nodePath: exists) {
          uuid = nodePath;
          session.getNodeByUUID(uuid);
        }
      } catch(PathNotFoundException e) {
        fail("Node must exists, UUID " + uuid + ". " + e.getMessage());
      }
    }
    if (notExists != null) {
      try {
        for (String nodeUUID: notExists) {
          session.getNodeByUUID(nodeUUID);
          fail("Node must not exists, UUID " + nodeUUID);
        }
      } catch(PathNotFoundException e) {
        // ok
      }
    }
  }


  protected void compareStream(InputStream etalon, InputStream data) throws IOException  {

    int index = 0;

    byte[] ebuff = new byte[64 * 1024];
    int eread = 0;
    ByteArrayOutputStream buff = new ByteArrayOutputStream();
    while ((eread = etalon.read(ebuff)) > 0) {

      byte[] dbuff = new byte[eread];
      while (buff.size() < eread) {
        int dread = -1;
        try {
          dread = data.read(dbuff);
        } catch(IOException e) {
          fail("Streams is not equals by length or data stream is unreadable. Cause: " + e.getMessage());
        }
        buff.write(dbuff, 0, dread);
      }

      dbuff = buff.toByteArray();

      for (int i=0; i<eread; i++) {
        byte eb = ebuff[i];
        byte db = dbuff[i];
        index++;
        if (eb != db)
          fail("Streams is not equals. Wrong byte stored at position " + index + " of data stream." );
      }

      buff = new ByteArrayOutputStream();
      if (dbuff.length > eread) {
        buff.write(dbuff, eread, dbuff.length);
      }
    }

    if (buff.size() > 0 || data.available() > 0)
      fail("Streams is not equals by length.");
  }

  protected File createBLOBTempFile(int sizeInKb) throws IOException {
    return createBLOBTempFile("exo_jcr_test_temp_file_", sizeInKb);
  }

  protected File createBLOBTempFile(String prefix, int sizeInKb) throws IOException {
    // create test file
    byte[] data = new byte[1024]; // 1KB
    Arrays.fill(data, (byte)65); // symbol A
    File testFile = File.createTempFile(prefix, ".tmp");
    FileOutputStream tempOut = new FileOutputStream(testFile);
    for (int i=0; i<sizeInKb; i++) {
      tempOut.write(data);
    }
    tempOut.close();
    testFile.deleteOnExit(); // delete on test exit
    log.info("Temp file created: " + testFile.getAbsolutePath()+" size: "+testFile.length());
    return testFile;
  }

}
