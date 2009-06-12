/*
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

package org.exoplatform.services.jcr.webdav.command;

import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.common.util.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.command.dasl.SearchRequestEntity;
import org.exoplatform.services.jcr.webdav.command.dasl.SearchResultResponseEntity;
import org.exoplatform.services.jcr.webdav.command.dasl.UnsupportedQueryException;
import org.exoplatform.services.jcr.webdav.xml.WebDavNamespaceContext;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. Author : <a
 * href="gavrikvetal@gmail.com">Vitaly Guly</a>
 * 
 * @version $Id: $
 */

public class SearchCommand {

  /**
   * logger.
   */
  private static Log log = ExoLogger.getLogger(SearchCommand.class);

  /**
   * Webdav search method implementation.
   * 
   * @param session current session
   * @param body rrequest body
   * @param baseURI base uri
   * @return the instance of javax.ws.rs.core.Response
   */
  public Response search(Session session, HierarchicalProperty body, String baseURI) {
    try {
      SearchRequestEntity requestEntity = new SearchRequestEntity(body);

      Query query = session.getWorkspace()
                           .getQueryManager()
                           .createQuery(requestEntity.getQuery(), requestEntity.getQueryLanguage());
      QueryResult queryResult = query.execute();

      WebDavNamespaceContext nsContext = new WebDavNamespaceContext(session);
      SearchResultResponseEntity searchResult = new SearchResultResponseEntity(queryResult,
                                                                               nsContext,
                                                                               baseURI);

      return Response.status(HTTPStatus.MULTISTATUS).entity(searchResult).build();
    } catch (PathNotFoundException exc) {
      return Response.status(HTTPStatus.NOT_FOUND).build();

    } catch (UnsupportedQueryException exc) {
      return Response.status(HTTPStatus.BAD_REQUEST).build();

    } catch (Exception exc) {
      log.error(exc.getMessage(), exc);
      return Response.serverError().build();
    }

  }

}
