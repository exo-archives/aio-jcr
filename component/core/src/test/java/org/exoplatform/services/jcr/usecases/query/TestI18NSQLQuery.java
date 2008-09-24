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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

/**
 * Created by The eXo Platform SAS Author : Anh Nguyen ntuananh.vn@gmail.com Nov 15, 2007
 */

public class TestI18NSQLQuery extends BaseUsecasesTest {

  private static String[] input;

  public void testI18NQueryPath() throws Exception {

    // Create nodes
    Session session = repository.getSystemSession(repository.getSystemWorkspaceName());

    input = readInputText("testi18n.txt").split("\n");

    // Removing char '\r' (for DOS/Windows OS)
    for (int k = 0; k < input.length; k++) {
      if (input[k].endsWith("\r")) {
        input[k] = input[k].substring(0, input[k].length() - 1);
      }
    }

    Node rootNode = session.getRootNode();

    for (int i = 0; i < input.length; i++) {
      String content = input[i];
      rootNode.addNode(content, "nt:unstructured");
    }
    rootNode.save();

    // Do Query by jcr:path
    for (int i = 0; i < input.length; i++) {
      String searchInput = input[i];

      String sqlQuery = "select * from nt:unstructured where jcr:path like '/" + searchInput + "' ";

      QueryManager manager = session.getWorkspace().getQueryManager();
      Query query = manager.createQuery(sqlQuery, Query.SQL);
      QueryResult queryResult = query.execute();
      NodeIterator iter = queryResult.getNodes();

      assertEquals(1, iter.getSize());
    }

  }

  public void testI18NQueryProperty() throws Exception {

    // We have problem with unicode chars, in Vietnamese or French, the result
    // alway empty

    // Create nodes
    Session session = repository.getSystemSession(repository.getSystemWorkspaceName());

    Node rootNode = session.getRootNode();
    for (int i = 0; i < input.length; i++) {
      String content = input[i];

      Node childNode = rootNode.addNode(String.valueOf(i), "nt:unstructured");
      childNode.setProperty("exo:testi18n", content);

    }
    rootNode.save();

    // Do Query by properties
    for (int i = 0; i < input.length; i++) {

      String searchInput = input[i];

      String sqlQuery = "select * from nt:unstructured where exo:testi18n like '" + searchInput
          + "' ";

      QueryManager manager = session.getWorkspace().getQueryManager();
      Query query = manager.createQuery(sqlQuery, Query.SQL);
      QueryResult queryResult = query.execute();

      NodeIterator iter = queryResult.getNodes();
      assertEquals(1, iter.getSize());
    }

  }

  private static String readInputText(String fileName) {

    try {
      InputStream is = TestI18NSQLQuery.class.getResourceAsStream(fileName);
      ByteArrayOutputStream output = new ByteArrayOutputStream();

      int r = is.available();
      byte[] bs = new byte[r];
      while (r > 0) {
        r = is.read(bs);
        if (r > 0) {
          output.write(bs, 0, r);
        }
        r = is.available();
      }
      is.close();
      return output.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
