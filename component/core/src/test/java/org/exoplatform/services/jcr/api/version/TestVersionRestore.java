/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.api.version;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.lock.LockException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;

/**
 * <code>TestVersionRestore</code> covers tests related to the methods of the
 * {@link javax.jcr.version.Version} class.
 * 
 * @test
 * @sources TestVersionRestore.java
 * @executeClass org.apache.jackrabbit.test.api.version.VersionTest
 * @keywords versioning
 */
public class TestVersionRestore extends BaseVersionTest {
 
  /**
   * Tests if we add one node to versionable and checkin.
   * Later we have to be able to restore each one version created.
   * 
   * We have versionableNode node of type nt:folder. OPV is VERSION.
   * So, we have to add a version on the node child node.  
   */
  public void testRestore() throws Exception {

    try {
      // Preparing versions
      Node doc = versionableNode.addNode("doc1", "nt:file");
      Node docContent = doc.addNode("jcr:content", "nt:unstructured");
      docContent.setProperty("doc1ContentProperty", "doc1 content"); // doc2/jcr:content/doc1ContentProperty
      root.save();
      //Version verDoc1 = doc.checkin(); // make a version for doc1 
      Version ver1 = versionableNode.checkin();      
      versionableNode.checkout();
      
      doc = versionableNode.addNode("doc2", "nt:file");
      docContent = doc.addNode("jcr:content", "nt:unstructured");
      docContent.setProperty("doc2ContentProperty", "doc2 content"); // doc2/jcr:content/doc2ContentProperty
      makeVersionable(doc);
      root.save();
      doc.checkin();
      doc.checkout();
      root.save();
      Version ver2 = versionableNode.checkin();      
      versionableNode.checkout();
      
      doc = versionableNode.addNode("doc3", "nt:file");
      doc.addNode("jcr:content", "nt:base");
      //makeVersionable(doc);
      root.save();
      Version ver3 = versionableNode.checkin();
      versionableNode.checkout();
            
      // Check version consistency
      // do restore ver1
      versionableNode.restore(ver1, true);
      
      Node doc1 = checkExisted("doc1", new String[] {"jcr:content/jcr:primaryType", "jcr:content/doc1ContentProperty"});
            
      checkNotExisted("doc2");
      checkNotExisted("doc3");
      
      versionableNode.checkout();
      doc1.remove();
      root.save();
      //doc1.save();ipossible to call save() on removed node
      
      // do restore ver2 
      versionableNode.restore(ver2, true);
      
      doc1 = checkExisted("doc1", new String[] {"jcr:content/jcr:primaryType", "jcr:content/doc1ContentProperty"});
      Node doc2 = checkExisted("doc2", new String[] {"jcr:content/jcr:primaryType", "jcr:content/doc2ContentProperty"});
      
      checkNotExisted("doc3");
      
      return;
    } catch (UnsupportedRepositoryOperationException e) {
      log.error("testRestore()", e);
      throw e;
    } catch (VersionException e) {
      log.error("testRestore()", e);
      throw e;
    } catch (LockException e) {
      log.error("testRestore()", e);
      throw e;
    } catch (RepositoryException e) {
      log.error("testRestore()", e);
      throw e;
    } catch (Exception e) {
      log.error("testRestore()", e);  
      throw e;
    }
    //fail("An exception occurs in testRestore()");
  } 
  
  /**
   * Test right version number calculation.
   * We wuill create three version then delete second and create one new.
   * Tha last version must have number 4.
   */
  public void testDelete() throws Exception {
    try {
      // Preparing versions
      Node doc = versionableNode.addNode("doc1", "nt:file");
      Node docContent = doc.addNode("jcr:content", "nt:unstructured");
      docContent.setProperty("doc1ContentProperty", "doc1 content"); // doc2/jcr:content/doc1ContentProperty
      root.save();
      //Version verDoc1 = doc.checkin(); // make a version for doc1 
      Version ver1 = versionableNode.checkin();      
      versionableNode.checkout();
      
      doc = versionableNode.addNode("doc2", "nt:file");
      docContent = doc.addNode("jcr:content", "nt:unstructured");
      docContent.setProperty("doc2ContentProperty", "doc2 content"); // doc2/jcr:content/doc2ContentProperty
      makeVersionable(doc);
      root.save();
      doc.checkin();
      doc.checkout();
      root.save();
      Version ver2 = versionableNode.checkin();      
      versionableNode.checkout();
      
      doc = versionableNode.addNode("doc3", "nt:file");
      doc.addNode("jcr:content", "nt:base");
      root.save();
      versionableNode.checkin();
      versionableNode.checkout();
            
      // do delete the ver2
      versionableNode.getVersionHistory().removeVersion(ver2.getName());
      
      Version ver4 = versionableNode.checkin();
      versionableNode.checkout();
      assertEquals("Version created has wrong version number", "4", ver4.getName());
      
      return;
    } catch (UnsupportedRepositoryOperationException e) {
      log.error("testDelete()", e);
      throw e;
    } catch (VersionException e) {
      log.error("testDelete()", e);
      throw e;
    } catch (LockException e) {
      log.error("testDelete()", e);
      throw e;
    } catch (RepositoryException e) {
      log.error("testDelete()", e);
      throw e;
    } catch (Exception e) {
      log.error("testDelete()", e);  
      throw e;
    }
  }
  
}