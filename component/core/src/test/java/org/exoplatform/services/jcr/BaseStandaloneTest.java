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
  
  protected class CompareStreamException extends Exception {
    
    CompareStreamException(String message) {
      super(message);
    }
    
    CompareStreamException(String message, Throwable e) {
      super(message, e);
    }
  }

  public void setUp() throws Exception {

    String conf = "src/test/java/conf/standalone/test-configuration.xml";
    //String conf = "src/test/java/conf/standalone/test-configuration-sjdbc.pgsql.xml";
    String loginConf = "src/main/resources/login.conf";
    if (Thread.currentThread().getContextClassLoader().getResource(conf)==null){
      conf = "component/core/" + conf;
    }
    if (Thread.currentThread().getContextClassLoader().getResource(loginConf)==null){
      loginConf = "component/core/" + loginConf;
    }

    StandaloneContainer.addConfigurationPath(conf);

    container = StandaloneContainer.getInstance();

    if (System.getProperty("java.security.auth.login.config") == null)
      System.setProperty("java.security.auth.login.config", loginConf);

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
    try {
      compareStream(etalon, data, 0, 0, -1);
    } catch(CompareStreamException e) {
      fail(e.getMessage());
    }
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
  protected void compareStream(InputStream etalon, InputStream data, long etalonPos, long dataPos, long length) throws IOException, CompareStreamException {

    int index = 0;
    
    byte[] ebuff = new byte[1024];
    int eread = 0;
    ByteArrayOutputStream buff = new ByteArrayOutputStream();
    
    skipStream(etalon, etalonPos);
//    if (etalonPos > 0) {
//      long pos = etalonPos; 
//      long sk = 0;
//      long sks = 0;
//      while (sks < etalonPos && (sk = etalon.skip(etalonPos)) > 0) {
//        sks += sk;
//      };
//      if (sk <0)
//        fail("Can not read the etalon (skip bytes)");
//      if (sks < dataPos)
//        fail("Can not skip bytes from the etalon (" + etalonPos + " bytes)");
//    }
    
    skipStream(data, dataPos);
//    if (dataPos > 0) {
//      long sk = 0;
//      long sks = 0;
//      while (sks < dataPos && (sk = data.skip(dataPos)) > 0) {
//        sks += sk;
//      };
//      if (sk <0)
//        fail("Can not read the data (skip bytes)");
//      if (sks < dataPos)
//        fail("Can not skip bytes from the data (" + dataPos + " bytes)");
//    }
    
    while ((eread = etalon.read(ebuff)) > 0) {

      byte[] dbuff = new byte[eread];
      while (buff.size() < eread) {
        int dread = -1;
        try {
          dread = data.read(dbuff);
        } catch(IOException e) {
          throw new CompareStreamException("Streams is not equals by length or data stream is unreadable. Cause: " + e.getMessage());
        }
        buff.write(dbuff, 0, dread);
        if (dread < eread)
          dbuff = new byte[eread - dread];
      }

      dbuff = buff.toByteArray();

      for (int i=0; i<eread; i++) {
        byte eb = ebuff[i];
        byte db = dbuff[i];
        if (eb != db)
          throw new CompareStreamException(
              "Streams is not equals. Wrong byte stored at position " + index + " of data stream. Expected 0x" + 
              Integer.toHexString(eb) + " '" + new String(new byte[] {eb}) + 
              "' but found 0x" + Integer.toHexString(db) + " '" + new String(new byte[] {db}) + "'");
        
        index++;
        if (length > 0 && index >= length)
          return; // tested length reached
      }

      buff = new ByteArrayOutputStream();
      if (dbuff.length > eread) {
        buff.write(dbuff, eread, dbuff.length);
      }
    }

    if (buff.size() > 0 || data.available() > 0)
      throw new CompareStreamException("Streams is not equals by length. Readed " + index);
  }  
  
  protected void skipStream(InputStream stream, long pos) throws IOException {
    long curPos = pos; 
    long sk = 0;
    while ((sk = stream.skip(curPos)) > 0) {
      curPos -= sk; 
    };
    if (sk <0)
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
  
  protected String memoryInfo() {
    String info = "";
    info = "free: " + mb(Runtime.getRuntime().freeMemory()) + "M of " + mb(Runtime.getRuntime().totalMemory()) + "M (max: " + mb(Runtime.getRuntime().maxMemory()) + "M)";
    return info;
  }
  
  // bytes to Mbytes
  protected String mb(long mem) {
    return String.valueOf(Math.round(mem * 100d/ (1024d * 1024d)) / 100d);
  }
  
  protected String execTime(long from) {
    return Math.round(((System.currentTimeMillis() - from) * 100.00d / 60000.00d)) / 100.00d + "min";
  }
}
