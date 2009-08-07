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
package org.exoplatform.connectors.jcr;

import java.net.MalformedURLException;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.rmi.impl.server.JCRServerImpl;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:lautarul@gmail.com">Roman Pedchenko</a>
 * @version $Id$
 */

public class RMIStarter {

  final static public int DO_BIND   = 1;

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
          } catch (MalformedURLException e) {
            System.out.println("Error: malformed url in repository configuration, didn't set");
          }
        }
      }
    }
    try {
      StandaloneContainer sc = StandaloneContainer.getInstance();
      JCRServerImpl binder = (JCRServerImpl) sc.getComponentInstanceOfType(JCRServerImpl.class);
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
