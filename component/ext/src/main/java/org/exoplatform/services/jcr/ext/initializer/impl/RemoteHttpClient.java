/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.initializer.impl;

import java.io.IOException;
import java.net.URL;

import org.exoplatform.common.http.client.AuthorizationHandler;
import org.exoplatform.common.http.client.AuthorizationInfo;
import org.exoplatform.common.http.client.CookieModule;
import org.exoplatform.common.http.client.HTTPConnection;
import org.exoplatform.common.http.client.HTTPResponse;
import org.exoplatform.common.http.client.ModuleException;
import org.exoplatform.common.http.client.ParseException;
import org.exoplatform.services.jcr.ext.initializer.RemoteWorkspaceInitializationException;
import org.exoplatform.services.jcr.ext.initializer.RemoteWorkspaceInitializationService;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 20.03.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: RemoteHTTPClient.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class RemoteHttpClient {

  /**
   * Url to remote data source.
   */
  private final String dataSourceUrl;

  public RemoteHttpClient(String dataSourceUrl) {
    this.dataSourceUrl = dataSourceUrl;
  }

  public String execute(String repositoryName, String workspaceName) throws RemoteWorkspaceInitializationException {
    String result = "FAIL";

    try {
      // execute the GET
      
      
      String complURL = dataSourceUrl 
          + RemoteWorkspaceInitializationService.Constants.BASE_URL + "/" + repositoryName + "/"
          + workspaceName + "/"
          + RemoteWorkspaceInitializationService.Constants.OperationType.GET_WORKSPACE;

      URL url = new URL(complURL);

      String userInfo = url.getUserInfo();
      if (userInfo == null || userInfo.split(":").length != 2)
        throw new RemoteWorkspaceInitializationException("Fail remote initializetion : the user name or password not not specified : "
            + dataSourceUrl);

      String userName = userInfo.split(":")[0];
      String password = userInfo.split(":")[1];

      HTTPConnection connection = new HTTPConnection(url);
      connection.removeModule(CookieModule.class);

      String realmName = getRealm(complURL);
      connection.addBasicAuthorization(realmName, userName, password);

      HTTPResponse resp = connection.Get(url.getFile());

      result = resp.getText();
      
      AuthorizationInfo.removeAuthorization(url.getHost(), url.getPort(), "Basic", realmName);
      
      if (resp.getStatusCode() != 200 )
        throw new RemoteWorkspaceInitializationException("Fail remote initializetion : " + result);
        
    } catch (ModuleException e) {
      throw new RemoteWorkspaceInitializationException(e.getMessage(), e);
    } catch (ParseException e) {
      throw new RemoteWorkspaceInitializationException(e.getMessage(), e);
    } catch (IOException e) {
      throw new RemoteWorkspaceInitializationException(e.getMessage(), e);
    }

    return result;
  }

  /**
   * Get realm by URL.
   * 
   * @param sUrl
   *          URL string.
   * @return realm name string.
   * @throws IOException
   *           transport exception.
   * @throws ModuleException
   *           ModuleException.
   */
  private String getRealm(String sUrl) throws IOException, ModuleException {

    AuthorizationHandler ah = AuthorizationInfo.getAuthHandler();

    try {
      URL url = new URL(sUrl);
      HTTPConnection connection = new HTTPConnection(url);
      connection.removeModule(CookieModule.class);
      AuthorizationInfo.setAuthHandler(null);

      HTTPResponse resp = connection.Get(url.getFile());

      String authHeader = resp.getHeader("WWW-Authenticate");

      String realm = authHeader.split("=")[1];
      realm = realm.substring(1, realm.length() - 1);

      return realm;

    } finally {
      AuthorizationInfo.setAuthHandler(ah);
    }
  }
}
