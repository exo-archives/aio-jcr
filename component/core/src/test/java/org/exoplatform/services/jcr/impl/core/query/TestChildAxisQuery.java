/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.services.jcr.impl.core.query;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.util.TraversingItemVisitor;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@exoplatform.org">Sergey Kabashnyuk</a>
 * @version $Id: exo-jboss-codetemplates.xml 34360 2009-07-22 23:58:59Z ksm $
 */
public class TestChildAxisQuery extends BaseQueryTest {
  /**
   * Class logger.
   */
  private final Log log = ExoLogger.getLogger(TestChildAxisQuery.class.getName());

  /**
   * Test child nodes query
   */
  public void _testChildQuery() throws Exception {
    Node testRoot = root.addNode("testRoot");
    session.save();

    int l1NodesCount = 100;
    int l2NodesCount = 10;
    for (int i = 0; i < l1NodesCount; i++) {
      Node lavelRoot = testRoot.addNode("lavel1_node_" + i);
      for (int j = 0; j < l2NodesCount; j++) {
        lavelRoot.addNode("node_" + j);
      }
      session.save();

      checkQuery("select * from nt:unstructured where jcr:path like '" + testRoot.getPath() + "/%'",
                 (i + 1 + (i + 1) * l2NodesCount));
      checkQuery("select * from nt:unstructured where jcr:path like '" + lavelRoot.getPath()
          + "/%'", l2NodesCount);

      Thread.sleep(1000);
    }
    testRoot.remove();
    session.save();
  }

  /**
   * Test query for version storage
   * 
   * @throws Exception
   */
  public void testChildVersions() throws Exception {
    Node testRoot = root.addNode("testRoot");
    session.save();
    int l1NodesCount = 1;
    int l2NodesCount = 150;
    for (int i = 0; i < l1NodesCount; i++) {
      Node lavelRoot = testRoot.addNode("lavel1_node_" + i);

      lavelRoot.addMixin("mix:versionable");
      session.save();
      for (int j = 0; j < l2NodesCount; j++) {

        lavelRoot.checkin();
        lavelRoot.checkout();

        session.save();

        checkQuery("select * from nt:base where jcr:path like '/jcr:system/jcr:versionStorage/"
                       + lavelRoot.getVersionHistory().getName() + "/%'",
                   getNodesCount(lavelRoot.getVersionHistory()));
      }

    }
    testRoot.remove();
    session.save();
  }

  private long getNodesCount(Node checkRoot) throws RepositoryException {
    final long[] result = new long[1];
    result[0] = 0;
    TraversingItemVisitor visitor = new TraversingItemVisitor() {

      @Override
      protected void entering(Property arg0, int arg1) throws RepositoryException {
        // TODO Auto-generated method stub

      }

      @Override
      protected void entering(Node arg0, int arg1) throws RepositoryException {
        // TODO Auto-generated method stub
        result[0]++;
      }

      @Override
      protected void leaving(Property arg0, int arg1) throws RepositoryException {
        // TODO Auto-generated method stub

      }

      @Override
      protected void leaving(Node arg0, int arg1) throws RepositoryException {
        // TODO Auto-generated method stub

      }

    };
    checkRoot.accept(visitor);
    return result[0] - 1;
  }

  private void checkQuery(String query, long expected) throws InvalidQueryException,
                                                      RepositoryException {
    Query q2 = session.getWorkspace().getQueryManager().createQuery(query, Query.SQL);

    QueryResult res2 = q2.execute();
    NodeIterator niter2 = res2.getNodes();
    assertEquals("Expected size", expected, niter2.getSize());
  }
}
