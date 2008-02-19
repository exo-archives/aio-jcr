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
package org.exoplatform.jcr.benchmark.usecases.query;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.exoplatform.jcr.benchmark.JCRTestBase;
import org.exoplatform.jcr.benchmark.JCRTestContext;
import org.exoplatform.services.log.ExoLogger;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */
public class SearchNodesByPropertyUsingLikeOperatorTest extends JCRTestBase {
  /*
   * This test calculates the time of query execution, dedicated structure has
   * been created.
   */

  public static Log log          = ExoLogger.getLogger("jcr.benchmark");

  private int       RESULT_NODES = 20; //incl. content node

  private String    sqlQuery     = "select * from nt:base where someowner like 'exoa%'";

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    QueryManager manager = context.getSession().getWorkspace().getQueryManager();
    Query query = manager.createQuery(sqlQuery, Query.SQL);
    QueryResult queryResult = query.execute();
    NodeIterator nodeIterator = queryResult.getNodes();
    if (nodeIterator.getSize() != RESULT_NODES) {
      log.error("Must be founded " + RESULT_NODES + " nodes but was: " + nodeIterator.getSize());
    }
    while (nodeIterator.hasNext()) {
      Node node = nodeIterator.nextNode();
    }
  }

}
