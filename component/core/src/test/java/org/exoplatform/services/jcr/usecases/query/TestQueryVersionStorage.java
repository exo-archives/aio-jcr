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
package org.exoplatform.services.jcr.usecases.query;

import java.io.ByteArrayInputStream;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date: 02.11.2009
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class TestQueryVersionStorage extends BaseUsecasesTest {

  private Node versionable;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    versionable = root.addNode("text-document", "nt:file");
    Node content = versionable.addNode("jcr:content", "nt:unstructured");
    Node text = content.addNode("text", "nt:unstructured");
    text.setProperty("mimeType", "text/plain");
    text.setProperty("lastModified", Calendar.getInstance());
    text.setProperty("data", new ByteArrayInputStream("text content #1".getBytes()));
    Node image = content.addNode("image", "nt:unstructured");
    image.setProperty("mimeType", "image/jpeg");
    image.setProperty("lastModified", Calendar.getInstance());
    image.setProperty("data", new ByteArrayInputStream("jpeg content #1".getBytes()));
    root.save();

    versionable.addMixin("mix:versionable");
    versionable.save();

    // version 1
    versionable.checkin();
    versionable.checkout();

    text.setProperty("lastModified", Calendar.getInstance());
    text.setProperty("data", new ByteArrayInputStream("text content #2".getBytes()));
    image.setProperty("lastModified", Calendar.getInstance());
    image.setProperty("data", new ByteArrayInputStream("jpeg content #2".getBytes()));
    versionable.save();

    // version 2
    versionable.checkin();
    versionable.checkout();
  }

  public void testPathSearch() throws InvalidQueryException, RepositoryException {

    Query q = root.getSession()
                  .getWorkspace()
                  .getQueryManager()
                  .createQuery("select * from nt:unstructured where jcr:path like '/jcr:system/%' and mimeType='text/plain'",
                               Query.SQL);

    QueryResult res = q.execute();
    NodeIterator niter = res.getNodes();
    assertEquals(2, niter.getSize());

    Query q2 = root.getSession()
                   .getWorkspace()
                   .getQueryManager()
                   .createQuery("select * from nt:unstructured where jcr:path like '/jcr:system/jcr:versionStorage/"
                                    + versionable.getVersionHistory().getName()
                                    + "/%' and mimeType='text/plain'",
                                Query.SQL);

    QueryResult res2 = q2.execute();
    NodeIterator niter2 = res2.getNodes();
    assertEquals(2, niter2.getSize());

    Query q3 = root.getSession()
                   .getWorkspace()
                   .getQueryManager()
                   .createQuery("select * from nt:unstructured where jcr:path like '/jcr:system/jcr:versionStorage/"
                                    + versionable.getVersionHistory().getName()
                                    + "/1/jcr:frozenNode/%' and mimeType='text/plain'",
                                Query.SQL);

    QueryResult res3 = q3.execute();
    NodeIterator niter3 = res3.getNodes();
    assertEquals(1, niter3.getSize());

    Query q4 = root.getSession()
                   .getWorkspace()
                   .getQueryManager()
                   .createQuery("select * from nt:unstructured where jcr:path like '/jcr:system/jcr:versionStorage/"
                                    + versionable.getVersionHistory().getName()
                                    + "/2/jcr:frozenNode/%' and mimeType='text/plain'",
                                Query.SQL);

    QueryResult res4 = q4.execute();
    NodeIterator niter4 = res4.getNodes();
    assertEquals(1, niter4.getSize());
  }
}
