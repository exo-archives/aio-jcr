/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.connectors.jcr;

import java.net.MalformedURLException;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.rmi.impl.server.JCRServerImpl;

/**
 * Created by The eXo Platform SARL .
 *
 * @author <a href="mailto:lautarul@gmail.com">Roman Pedchenko</a>
 * @version $Id: RMIStarter.java 7175 2006-07-19 07:57:44Z peterit $
 */

public class RMIStarter {

  final static public int DO_BIND = 1;
  final static public int DO_UNBIND = 2;

  public static void main(String[] argv) {
    int bindOper = 0;

    if (argv.length > 0) {
      for (int i = 0; i < argv.length; i++) {
        if (argv[i].equals("bind")) {
          bindOper = DO_BIND;
        } else if (argv[i].equals("unbind")) {
          bindOper = DO_UNBIND;
        } else if (i == 1) {
          try {
            StandaloneContainer.addConfigurationPath(argv[i]);
          } catch(MalformedURLException e) {
            System.out.println("Error: malformed url in repository configuration, didn't set");
          }
        }
      }
    }
    try {
      StandaloneContainer sc = StandaloneContainer.getInstance();
      JCRServerImpl binder = (JCRServerImpl)sc.getComponentInstanceOfType(JCRServerImpl.class);
      if (bindOper == DO_BIND) {
        binder.bind();
        System.out.println("JCR RMI service registered (bind) and started successfully");
      } else if (bindOper == DO_UNBIND) {
        binder.unbind();
        System.out.println("JCR RMI service unregistered (unbind) successfully");
      }
    } catch (Exception e) {
      System.out.println("Error: " + e);
      e.printStackTrace();
    }
  }

}
