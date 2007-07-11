/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.storage.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 * 10.07.2007
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class TestJCRVSReadWrite extends JcrImplBaseTest {

  private static Log log = ExoLogger.getLogger("jcr.TestJCRVSReadWrite");

  public static final int FILES_COUNT = 1000;
  public static final int FILE_SIZE_KB = 11;
  public static final int FILE_SIZE = FILE_SIZE_KB * 1024;

  protected Node testRoot = null;

  protected List<String> properties = null;
  
  protected File fBLOB = null;

  public void setUp() throws Exception {
    super.setUp();

    if (fBLOB == null) {
      fBLOB = createBLOBTempFile("treeVSTest_", FILE_SIZE_KB);
      fBLOB.deleteOnExit();
    }
    
    testRoot = root.addNode("tree_vs_test");
    root.save();
  }

  protected void tearDown() throws Exception {
    long time = System.currentTimeMillis();

    testRoot.remove();
    root.save();
    
    log.info("Tear down of " + getName() + ",\t" + (System.currentTimeMillis() - time));

    super.tearDown();
  }
    
  protected List<String> createPlainCase() throws Exception {
    List<String> props = new ArrayList<String>();
    String rootPath = testRoot.getPath();
    
    for (int i=0; i<FILES_COUNT; i++) {
      InputStream fis = new FileInputStream(fBLOB);
      try {
        Node resource = testRoot.addNode("blob" + i, "nt:file")
          .addNode("jcr:content", "nt:resource");
        String path = resource.setProperty("jcr:data", fis).getPath();
        resource.setProperty("jcr:mimeType", "application/x-octet-stream");
        resource.setProperty("jcr:lastModified", Calendar.getInstance());
        testRoot.save();
        
        props.add(path.substring(rootPath.length() + 1));
      } catch(RepositoryException e) {
        log.warn("Can't create test case, " + e);
        throw new Exception("Can't create test case, " + e, e);
      } finally {
        fis.close();
      }
    }
    return props;
  }

  protected void deleteCase() throws RepositoryException {
    for (NodeIterator iter = testRoot.getNodes(); iter.hasNext(); ) {
      iter.nextNode().remove();
    }
    
    testRoot.save();
  }
  
  public void testReadWriteNtFile() throws Exception {
    long time = System.currentTimeMillis();
    List<String> props = createPlainCase();
    log.info(getName() + " ADD -- " + (System.currentTimeMillis() - time));

    time = System.currentTimeMillis();
    // read randomize
    Set<String> caseProps = new HashSet<String>(props);
    for (String prop: caseProps) {
      InputStream stream = testRoot.getProperty(prop).getStream();
      assertEquals("Value has wrong length", FILE_SIZE, stream.available());
    }
    log.info(getName() + " READ -- " + (System.currentTimeMillis() - time));

    time = System.currentTimeMillis();
    deleteCase();
    log.info(getName() + " DELETE -- " + (System.currentTimeMillis() - time));
  }
}
