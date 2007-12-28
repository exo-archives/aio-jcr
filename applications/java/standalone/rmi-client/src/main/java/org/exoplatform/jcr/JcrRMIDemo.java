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
package org.exoplatform.jcr;

import javax.jcr.*;
import org.exoplatform.services.jcr.rmi.api.client.ClientRepositoryFactory;

/**
 * Created by The eXo Platform SAS .
 *
 * @author <a href="mailto:lautarul@gmail.com">Roman Pedchenko</a>
 * @version $Id: JcrRMIDemo.java 11126 2006-12-12 17:15:02Z vetal_ok $
 */

public class JcrRMIDemo {

  public static void main(String[] argv) {

    try {
      ClientRepositoryFactory factory = new ClientRepositoryFactory();
      Repository repository = factory.getRepository("//localhost:9999/repository");
      Credentials credentials = new SimpleCredentials("admin", "admin".toCharArray());
      Session session = repository.login(credentials, "production");
      Node node = session.getRootNode();
      System.out.println("root node path: " + node.getPath());
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

}
