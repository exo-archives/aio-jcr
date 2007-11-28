/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.ext.actions;

import javax.jcr.ItemExistsException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.exoplatform.services.jcr.ext.BaseStandaloneTest;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class TestActions extends BaseStandaloneTest {
  public void testReadAction() throws ItemExistsException,
                              PathNotFoundException,
                              VersionException,
                              ConstraintViolationException,
                              LockException,
                              RepositoryException {
    // SessionActionCatalog catalog = (SessionActionCatalog)
    // container.getComponentInstanceOfType(SessionActionCatalog.class);
    // catalog.clear();
    //
    // // test by path
    //
    // Node testNode = root.addNode("testNode");
    // PropertyImpl prop = (PropertyImpl) testNode.setProperty("test", "test");
    // root.save();
    //
    // SessionEventMatcher matcher = new SessionEventMatcher(ExtendedEvent.READ,
    // new QPath[] { prop.getData().getQPath() },
    // true,
    // null,
    // null,
    // new InternalQName[] { Constants.NT_UNSTRUCTURED });
    // DummyAction dAction = new DummyAction();
    //
    // catalog.addAction(matcher, dAction);

    // ???????????????
    // assertEquals(0, dAction.getActionExecuterCount());
    // String val = testNode.getProperty("test").getValue().getString();
    // assertEquals(1, dAction.getActionExecuterCount());

  }
}
