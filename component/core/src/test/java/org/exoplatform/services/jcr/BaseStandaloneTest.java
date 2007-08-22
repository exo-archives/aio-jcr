package org.exoplatform.services.jcr;

import java.io.ByteArrayOutputStream;
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

import org.apache.commons.logging.Log;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * Created by The eXo Platform SARL .
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id$
 */
public abstract class BaseStandaloneTest extends TestCase {

  protected static Log log = ExoLogger.getLogger("jcr.JCRTest");

  protected static String TEMP_PATH = "./temp/fsroot";

  protected static String WORKSPACE = "ws";

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
      .addConfigurationPath("src/test/java/conf/standalone/test-configuration.xml");
      //.addConfigurationPath("src/test/java/conf/standalone/test-configuration-sjdbc.pgsql.xml");
      //.addConfigurationPath("src/test/java/conf/standalone/test-configuration-mjdbc.mysql.xml");

    container = StandaloneContainer.getInstance();

    if (System.getProperty("java.security.auth.login.config") == null)
      System.setProperty("java.security.auth.login.config",
          "src/main/resources/login.conf");

    credentials = new CredentialsImpl("admin", "admin".toCharArray());

    repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    repository = (RepositoryImpl) repositoryService.getDefaultRepository();

    session = (SessionImpl) repository.login(credentials, "ws");
    workspace = session.getWorkspace();
    root = session.getRootNode();
    valueFactory = session.getValueFactory();

    initRepository();
  }

  protected void tearDown() throws Exception {

    if (session != null) {
      try {
        session.refresh(false);
        Node rootNode = session.getRootNode();
        if (rootNode.hasNodes()) {
          // clean test root
          for (NodeIterator children = rootNode.getNodes(); children.hasNext();) {
            Node node = children.nextNode();
            if (!node.getPath().startsWith("/jcr:system")) {
              //log.info("DELETing ------------- "+node.getPath());
              node.remove();
            }
          }
          session.save();
        }
      } catch (Exception e) {
        log.error("tearDown() ERROR " + getClass().getName() + "." + getName() + " " + e, e);
      } finally {
        session.logout();
      }
    }
    super.tearDown();

    //log.info("tearDown() END " + getClass().getName() + "." + getName());
  }

  protected abstract String getRepositoryName();

  public void initRepository() throws RepositoryException {
  }

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
    compareStream(etalon, data, 0, 0, -1);    
  }
  
  /**
   * Compare etalon stream with data stream begining from the offset in etalon and position in data.
   * Length bytes will be readed and compared. if length is lower 0 then compare streams till one of them will be read.
   *  
   * @param etalon
   * @param data
   * @param etalonPos
   * @param length
   * @param dataPos
   * @throws IOException
   */
  protected void compareStream(InputStream etalon, InputStream data, long etalonPos, long dataPos, long length) throws IOException  {

    int index = 0;
    
    byte[] ebuff = new byte[64 * 1024];
    int eread = 0;
    ByteArrayOutputStream buff = new ByteArrayOutputStream();
    
    etalon.skip(etalonPos);
    data.skip(dataPos);
    
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
          fail("Streams is not equals. Wrong byte stored at position " + index + " of data stream. Expected 0x" + 
              Integer.toHexString(eb) + " '" + new String(new byte[] {eb}) + 
              "' found 0x" + Integer.toHexString(db) + " '" + new String(new byte[] {db}) + "'");
        if (length > 0 && index >= length)
          return; // tested length reached
      }

      buff = new ByteArrayOutputStream();
      if (dbuff.length > eread) {
        buff.write(dbuff, eread, dbuff.length);
      }
    }

    if (buff.size() > 0 || data.available() > 0)
      fail("Streams is not equals by length. Readed " + index);
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

    for (int i=0; i<sizeInKb; i++) {
      random.nextBytes(data);
      tempOut.write(data);
    }
    tempOut.close();
    testFile.deleteOnExit(); // delete on test exit
    log.info("Temp file created: " + testFile.getAbsolutePath()+" size: "+testFile.length());
    return testFile;
  }

  protected void checkMixins(String[] mixins, NodeImpl node) {
    try {
      String[] nodeMixins = node.getMixinTypeNames();
      assertEquals("Mixins count is different", mixins.length, nodeMixins.length);

      compareMixins(mixins, nodeMixins);
    } catch(RepositoryException e) {
      fail("Mixins isn't accessible on the node " + node.getPath());
    }
  }

  protected void compareMixins(String[] mixins, String[] nodeMixins) {
    nextMixin: for (String mixin: mixins) {
      for (String nodeMixin: nodeMixins) {
        if (mixin.equals(nodeMixin))
          continue nextMixin;
      }

      fail("Mixin '" + mixin + "' isn't accessible");
    }
  }

}
