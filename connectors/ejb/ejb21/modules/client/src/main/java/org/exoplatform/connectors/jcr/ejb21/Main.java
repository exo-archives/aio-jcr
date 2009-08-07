/**
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

package org.exoplatform.connectors.jcr.ejb21;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class Main {

  public static void main(String[] args) throws Exception {
    Client c = new Client();
    for (String s : args) {
      if (s.startsWith("--server"))
        c.setServerUrl(s.substring(s.indexOf('=') + 1));
      if (s.startsWith("--jcr-path"))
        c.setJcrUrl(s.substring(s.indexOf('=') + 1));
    }

    System.out.println(c.run());
  }

}
