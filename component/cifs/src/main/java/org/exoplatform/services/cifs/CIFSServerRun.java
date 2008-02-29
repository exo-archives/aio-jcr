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

package org.exoplatform.services.cifs;

import java.net.URL;

import org.exoplatform.container.StandaloneContainer;

/**
 * Created by The eXo Platform SAS Author : Karpenko Sergey
 * <p>
 * Its standalone server run implementation. Server run as a part of complete
 * standalone container, so other component runs too.
 * 
 */

public class CIFSServerRun {

  /**
   * This method used just for independent run server (without any applications server)
   * Check the conf/cifs-configuration.xml, it may not exist.
   * 
   * @param args
   */
  public static void main(String[] args) {
    System.out.println("CIFS Server Test by Exo Platform");
    System.out.println("--------------------------------");

    // TODO There is a reason get path to configuratiuon file from args
    try {

      URL configurationURL = Thread.currentThread().getContextClassLoader()
          .getResource("conf/cifs-configuration.xml");
      if (configurationURL == null)
        throw new Exception(
            "No configuration found. Check that \"conf/cifs-configuration.xml\" exists !");

      StandaloneContainer.addConfigurationURL(configurationURL.toString());

      // obtain standalone container
      StandaloneContainer container = StandaloneContainer.getInstance();

     
      
      // set JAAS auth config

      URL loginURL = Thread.currentThread().getContextClassLoader()
          .getResource("login.conf");

      if (loginURL == null)
        throw new Exception(
            "No login config found. Check that resource login.conf exists !");

      if (System.getProperty("java.security.auth.login.config") == null)
        System.setProperty("java.security.auth.login.config", loginURL
            .toString());

      System.out.println("Enter 'x' to shutdown ...");
      boolean shutdown = false;
      while (shutdown == false) {

        // Wait for the user to enter the shutdown key

        int ch = System.in.read();

        if (ch == 'x' || ch == 'X') {
          shutdown = true;
        }

        synchronized (container) {
          container.wait(20);
        }
      }

      container.stop();

    } catch (Exception ex) {

ex.printStackTrace();
    }

    System.exit(1);
  }

}
