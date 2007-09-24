/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.load.blob;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.exoplatform.services.jcr.BaseStandaloneTest;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.tools.tree.TreeGenerator;
import org.exoplatform.services.jcr.impl.tools.tree.ValueSsh1Comparator;
import org.exoplatform.services.jcr.impl.tools.tree.ValueSsh1Generator;
import org.exoplatform.services.jcr.impl.tools.tree.generator.RandomValueNodeGenerator;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class TestLoadRepo extends BaseStandaloneTest {

  @Override
  protected String getRepositoryName() {
    // TODO Auto-generated method stub
    return null;
  }

  public void _testExport() throws Exception {
    Node testNode = root.addNode("testNode");
    File checkSummValue = new File(System.getProperty("java.io.tmpdir"), "repo.ssh1");
    BufferedOutputStream sshStrream = new BufferedOutputStream(new FileOutputStream(checkSummValue));
    RandomValueNodeGenerator nodeGenerator = new RandomValueNodeGenerator(session.getValueFactory(),
        6,
        5,
        8,
        5,
        1024 * 1024);
    TreeGenerator generator = new TreeGenerator(testNode, nodeGenerator);
    generator.genereteTree();
    root.save();
    ValueSsh1Generator ssh1Generator = new ValueSsh1Generator(session.getTransientNodesManager(),
        sshStrream);
    ((NodeImpl) testNode).getData().accept(ssh1Generator);
    sshStrream.close();
    File exportFile = new File(System.getProperty("java.io.tmpdir"), "testExport.xml");
    OutputStream os = new FileOutputStream(exportFile);
    session.exportSystemView(testNode.getPath(), os, false, false);
    os.close();
  }

  public void testImport() throws ItemExistsException,
      PathNotFoundException,
      VersionException,
      ConstraintViolationException,
      LockException,
      RepositoryException,
      IOException,
      NoSuchAlgorithmException {

    File importFile = new File(System.getProperty("java.io.tmpdir"), "testExport.xml");
    InputStream is = new FileInputStream(importFile);

    
    session.getWorkspace().getNamespaceRegistry().registerNamespace("exojcrtest_old", "http://www.exoplatform.org/jcr/exojcrtest");
    session
        .importXML(root.getPath(), is, ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
    session.save();
    
    File ssh1File = new File(System.getProperty("java.io.tmpdir"), "repo.ssh1");
    InputStream isSSH1 = new FileInputStream(ssh1File);

    ValueSsh1Comparator ssh1Comparator = new ValueSsh1Comparator(session.getTransientNodesManager(),
        isSSH1);
    ((NodeImpl)  root.getNode("testNode")).getData().accept(ssh1Comparator);
  
    
    
  };

}
