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

package org.exoplatform.services.jcr.webdav.resource;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Created by The eXo Platform SARL .<br/>
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class ResourceUtil {

  public static boolean isFile(Node node) {
    try {
      if (!node.isNodeType("nt:file"))
        return false;
      if (!node.getNode("jcr:content").isNodeType("nt:resource"))
        return false;
      return true;
    } catch (RepositoryException e) {
      e.printStackTrace();
      return false;
    }
  }

  public static boolean isVersion(Node node) {
    try {
      if (node.isNodeType("nt:version"))
        return true;
      return false;
    } catch (RepositoryException e) {
      e.printStackTrace();
      return false;
    }
  }

  public static boolean isVersioned(Node node) {
    try {
      if (node.isNodeType("mix:versionable"))
        return true;
      return false;
    } catch (RepositoryException e) {
      e.printStackTrace();
      return false;
    }
  }

}
