/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
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
