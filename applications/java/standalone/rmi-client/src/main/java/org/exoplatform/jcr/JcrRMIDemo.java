/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.jcr;

import javax.jcr.*;
import org.exoplatform.services.jcr.rmi.api.client.ClientRepositoryFactory;

/**
 * Created by The eXo Platform SARL .
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
