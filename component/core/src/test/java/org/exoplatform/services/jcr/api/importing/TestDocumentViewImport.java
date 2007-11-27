/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS. All rights reserved.          *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.api.importing;

import javax.jcr.ImportUUIDBehavior;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.impl.xml.XmlSaveType;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class TestDocumentViewImport extends BaseImportTest {
  private static Log log = ExoLogger.getLogger("jcr.TestDocumentViewImport");

  /**
   * (boolean isSystemView, boolean isExportedByStream, boolean
   * isImportedByStream, XmlSaveType saveType, int testedBehavior)
   */
  public void testUuidCollision_IContentHandler_EContentHandler_Session_COLLISION_THROW() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            false,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
  }

  public void testUuidCollision_IContentHandler_EContentHandler_Session_CREATE_NEW() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            false,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
  }

  public void testUuidCollision_IContentHandler_EContentHandler_Session_REMOVE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            false,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
  }

  public void testUuidCollision_IContentHandler_EContentHandler_Session_REPLACE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            false,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
  }

  public void testUuidCollision_IContentHandler_EContentHandler_Workspace_COLLISION_THROW() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            false,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
  }

  public void testUuidCollision_IContentHandler_EContentHandler_Workspace_CREATE_NEW() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            false,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
  }

  public void testUuidCollision_IContentHandler_EContentHandler_Workspace_REMOVE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            false,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
  }

  public void testUuidCollision_IContentHandler_EContentHandler_Workspace_REPLACE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            false,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
  }

  public void testUuidCollision_IContentHandler_EStream_Session_COLLISION_THROW() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            true,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
  }

  public void testUuidCollision_IContentHandler_EStream_Session_CREATE_NEW() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            true,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
  }

  public void testUuidCollision_IContentHandler_EStream_Session_REMOVE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            true,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
  }

  public void testUuidCollision_IContentHandler_EStream_Session_REPLACE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            true,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
  }

  public void testUuidCollision_IContentHandler_EStream_Workspace_COLLISION_THROW() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            true,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
  }

  public void testUuidCollision_IContentHandler_EStream_Workspace_CREATE_NEW() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            true,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
  }

  public void testUuidCollision_IContentHandler_EStream_Workspace_REMOVE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            true,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
  }

  public void testUuidCollision_IContentHandler_EStream_Workspace_REPLACE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            true,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
  }

  public void testUuidCollision_IStream_EContentHandler_Session_COLLISION_THROW() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            false,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
  }

  public void testUuidCollision_IStream_EContentHandler_Session_CREATE_NEW() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            false,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
  }

  public void testUuidCollision_IStream_EContentHandler_Session_REMOVE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            false,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
  }

  public void testUuidCollision_IStream_EContentHandler_Session_REPLACE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            false,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
  }

  public void testUuidCollision_IStream_EContentHandler_Workspace_COLLISION_THROW() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            false,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
  }

  public void testUuidCollision_IStream_EContentHandler_Workspace_CREATE_NEW() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            false,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
  }

  public void testUuidCollision_IStream_EContentHandler_Workspace_REMOVE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            false,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
  }

  public void testUuidCollision_IStream_EContentHandler_Workspace_REPLACE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            false,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
  }

  public void testUuidCollision_IStream_EStream_Session_COLLISION_THROW() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            true,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
  }

  public void testUuidCollision_IStream_EStream_Session_CREATE_NEW() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            true,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
  }

  public void testUuidCollision_IStream_EStream_Session_REMOVE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            true,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
  }

  public void testUuidCollision_IStream_EStream_Session_REPLACE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            true,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
  }

  public void testUuidCollision_IStream_EStream_Workspace_COLLISION_THROW() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            true,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
  }

  public void testUuidCollision_IStream_EStream_Workspace_CREATE_NEW() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            true,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
  }

  public void testUuidCollision_IStream_EStream_Workspace_REMOVE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            true,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
  }

  public void testUuidCollision_IStream_EStream_Workspace_REPLACE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            true,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
  }

}
