/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.usecases;

import java.io.ByteArrayInputStream;
import java.util.Calendar;

import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: SampleUseCaseTest.java 12841 2007-02-16 08:58:38Z peterit $ JCR
 *          Use Case test sample
 */

public class SimpleGetDataTest extends BaseUsecasesTest {

  /**
   * Sample test. An example how to make it
   * 
   * @throws Exception
   */
  public void testSimpleGetData() throws Exception {
    // Local small files creating
    Node testLocalSmallFiles = root.addNode("testLocalSmallFiles");
    Node localSmallFile = testLocalSmallFiles.addNode("smallFile", "nt:file");
    Node contentNode = localSmallFile.addNode("jcr:content", "nt:resource");
    byte[] data = new byte[32];
    contentNode.setProperty("jcr:data", new ByteArrayInputStream(data));
    contentNode.setProperty("jcr:mimeType", "application/octet-stream");
    contentNode.setProperty("jcr:lastModified", Calendar.getInstance());
    session.save();
    assertNotNull(session.getRootNode().getNode("testLocalSmallFiles").getNode("smallFile").getNode(
    "jcr:content").getProperty("jcr:data").getValue());
  }
}
