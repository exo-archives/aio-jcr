/***************************************************************************
 * Copyright 2001-2009 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.version;

import java.io.ByteArrayInputStream;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.version.VersionHistory;

import org.exoplatform.services.jcr.BaseStandaloneTest;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 14, 2009  
 */
public class TestVersionForNTFileNode extends BaseStandaloneTest {

  @Override
  protected String getRepositoryName() {
    // TODO Auto-generated method stub
    return null;
  }

  public void testVersionForNTFile() throws Exception {
    Node fileNode = createFileNode("file");
    Node contentNodeBeforeAddVersion = fileNode.getNode("jcr:content");
    assertNotNull(contentNodeBeforeAddVersion.getProperty("jcr:lastModified"));
    if(fileNode.canAddMixin("mix:versionable")) {
      fileNode.addMixin("mix:versionable");
    }
    fileNode.save();
    fileNode.checkin();
    fileNode.checkout();
    session.save();
    VersionHistory vH = fileNode.getVersionHistory();
    Node contentNode = vH.getNode("1/jcr:frozenNode/jcr:content");
    assertNotNull(contentNode.getProperty("jcr:lastModified"));
  }
  
  private Node createFileNode(String name) throws Exception {
    Node testNode = session.getRootNode().addNode("Test");
    Node nodeFile = testNode.addNode(name, "nt:file");
    Node contentNode = nodeFile.addNode("jcr:content", "nt:resource");
    contentNode.setProperty("jcr:data", new ByteArrayInputStream("".getBytes()));
    contentNode.setProperty("jcr:mimeType", "image/jpg");
    contentNode.setProperty("jcr:lastModified", Calendar.getInstance());
    session.save();
    return nodeFile;
  }
  
  public void tearDown() throws Exception {
    Node testNode = session.getRootNode().getNode("Test");
    testNode.remove();
    session.save();
  }  
  
}
