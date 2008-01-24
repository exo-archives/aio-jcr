/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.impl.core.query;

import javax.jcr.Node;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class TestQueryScore extends JcrImplBaseTest {
  /**
   * Class logger.
   */
  private final Log log = ExoLogger.getLogger("jcr.TestQueryScore");

  public void testScore() throws Exception {
    Node testRoot = root.addNode("testRoot");
    Node firstNode = testRoot.addNode("firstNode");
    firstNode.setProperty("text", "eXo Platform");

    Node secondNode = testRoot.addNode("secondNode");
    secondNode.setProperty("text", "eXo text text Platform");
    
    Node thirdNode = testRoot.addNode("thirdNode");
    
    thirdNode.setProperty("text", "eXo text text text Platform");

    session.save();

    QueryManager qm = session.getWorkspace().getQueryManager();

    Query query = qm.createQuery("select * from nt:unstructured where "
        + " contains(*, 'eXo Platform')  order by jcr:score desc", Query.SQL);
    QueryResult result = query.execute();

    RowIterator rows = result.getRows();
    assertEquals(3, rows.getSize());
    while (rows.hasNext()) {
      Row row = rows.nextRow();
      log.info(row.getValue("jcr:path").getString());
      log.info(row.getValue("jcr:score").getString());
    }
  }
}
