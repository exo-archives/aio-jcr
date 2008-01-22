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

package org.exoplatform.services.jcr.webdav.command.deltav;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.Response;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class VersionControlCommand {
  
  private static Log log = ExoLogger.getLogger("jcr.VersionControlCommand");
  
  public Response versionControl(Session session, String path) {
    
    log.info("VERSION-CONTROL " + path);
    
    try {      
      Node node = (Node)session.getItem(path);
      
      if (!node.isNodeType("mix:versionable")) {
        node.addMixin("mix:versionable");
        session.save();
      }
      return Response.Builder.ok().build();
      
    } catch (LockException exc) {
      return Response.Builder.withStatus(WebDavStatus.LOCKED).build();
      
    } catch (PathNotFoundException exc) {
      return Response.Builder.notFound().build();
    
    } catch (Exception exc) {
      return Response.Builder.serverError().build(); 
    }
    
  }

}
