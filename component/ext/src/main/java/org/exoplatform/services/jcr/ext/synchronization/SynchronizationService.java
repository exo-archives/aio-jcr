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
package org.exoplatform.services.jcr.ext.synchronization;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.apache.ws.commons.util.Base64;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.backup.BackupManager;
import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 18.08.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: SynchronizationService.java 111 2008-11-11 11:11:11Z peterit $
 */

@Path("/synchronization/")
public class SynchronizationService implements ResourceContainer {

  public static final String        ID_DELIMITER = "$|";

  protected final RepositoryService repoService;

  protected final BackupManager     backupManager;

  protected final InitParams        params;

  /**
   * User identity in request
   */
  protected class RequestIdentity {

    final String userName;

    final String password;

    RequestIdentity(String userName, String password) {
      this.userName = userName;
      this.password = password;
    }
  }

  protected RequestIdentity parseIdentityLine(String identityLine) throws IOException {

    String l = new String(Base64.decode(identityLine));
    String[] lpair = l.split(ID_DELIMITER);

    String userName, password;
    if (lpair.length > 0) {
      userName = lpair[0];
      if (lpair.length > 1)
        password = lpair[1];
      else
        password = lpair[1];
    } else
      userName = password = null;

    return new RequestIdentity(userName, password);
  }

  protected String makeIdentityLine(RequestIdentity identity) {

    String l = identity.userName + ID_DELIMITER + identity.password;
    return new String(Base64.encode(l.getBytes()));
  }

  public SynchronizationService(RepositoryService repoService,
                                BackupManager backupManager,
                                InitParams params) {
    this.repoService = repoService;
    this.backupManager = backupManager;
    this.params = params;
  }

  @GET
  @Path("/hostfullbackup/{repositoryName}/{workspaceName}/{resourcePath}/")
  public Response hostFullBackup(@PathParam("repositoryName") String repositoryName,
                                 @PathParam("workspaceName") String workspaceName,
                                 @PathParam("resourcePath") String resourcePath,
                                 @PathParam("fileName") String fileName,
                                 @PathParam("id") String identityLine) {

    try {
      RequestIdentity identity = parseIdentityLine(identityLine);

      return Response.ok("TODO".toString(), "text/plain").build();
    } catch (IOException e) {
      return Response.ok("TODO".toString(), "text/plain").build();
    }
  }

}
