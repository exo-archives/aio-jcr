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
package org.exoplatform.jcr.backupconsole;

import java.io.IOException;
import java.net.URL;

import javax.ws.rs.core.Response;

import org.exoplatform.common.http.client.AuthorizationHandler;
import org.exoplatform.common.http.client.AuthorizationInfo;
import org.exoplatform.common.http.client.CookieModule;
import org.exoplatform.common.http.client.HTTPConnection;
import org.exoplatform.common.http.client.HTTPResponse;
import org.exoplatform.common.http.client.ModuleException;
import org.exoplatform.common.http.client.NVPair;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: ClientTransportImpl.java 111 2008-11-11 11:11:11Z serg $
 */
public class ClientTransportImpl implements ClientTransport {

  /**
   * String form - hostIP:port.
   */
  private final String  host;

  /**
   * Login.
   */
  private final String  login;

  /**
   * Password.
   */
  private final String  password;

  /**
   * Flag is SSL.
   */
  private final String protocol;

  /**
   * Constructor.
   * 
   * @param login Login string.
   * @param password Password string.
   * @param host host string.
   * @param isSSL isSSL flag.
   */
  public ClientTransportImpl(String login, String password, String host, String protocol) {
    this.host = host;
    this.login = login;
    this.password = password;
    this.protocol = protocol;
  }

  /**
   * Get realm by URL.
   * 
   * @param sUrl URL string.
   * @return realm name string.
   * @throws IOException transport exception.
   * @throws ModuleException ModuleException.
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

  /**
   * {@inheritDoc}
   */
  public BackupAgentResponse executePOST(String sURL, String postData) throws IOException, BackupExecuteException {
    try {
      // execute the POST
      String complURL = protocol + "://" + host + sURL;

      URL url = new URL(complURL);
      HTTPConnection connection = new HTTPConnection(url);
      connection.removeModule(CookieModule.class);

      connection.addBasicAuthorization(getRealm(complURL), login, password);

      HTTPResponse resp;
      if (postData == null) {
        resp = connection.Post(url.getFile());  
        
        if (resp.getStatusCode() != Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() &&
            resp.getStatusCode() != Response.Status.OK.getStatusCode())
          throw new BackupExecuteException("Unknown response, status code : " + resp.getStatusCode());
      } else {
        NVPair[] pairs = new NVPair[2];
        pairs[0] = new NVPair("Content-Type", "application/json; charset=UTF-8");
        pairs[1] = new NVPair("Content-Length", Integer.toString(postData.length()));

        resp = connection.Post(url.getFile(), postData.getBytes(), pairs);
        
        if (resp.getStatusCode() != Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() &&
            resp.getStatusCode() != Response.Status.OK.getStatusCode()) 
          throw new BackupExecuteException("Unknown response, status code : " + resp.getStatusCode());
      }

      BackupAgentResponse responce = new BackupAgentResponse(resp.getData(), resp.getStatusCode());
      return responce;
    } catch (ModuleException e) {
      throw new BackupExecuteException(e.getMessage(), e);
    } 
    
  }
  
  /**
   * {@inheritDoc}
   */
  public BackupAgentResponse executeGET(String sURL) throws IOException, BackupExecuteException {
    try {
      // execute the POST
      String complURL = protocol + "://" + host + sURL;

      URL url = new URL(complURL);
      HTTPConnection connection = new HTTPConnection(url);
      connection.removeModule(CookieModule.class);

      connection.addBasicAuthorization(getRealm(complURL), login, password);

      HTTPResponse resp = connection.Get(url.getFile());  
        
        if (resp.getStatusCode() != Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() &&
            resp.getStatusCode() != Response.Status.OK.getStatusCode())
          throw new BackupExecuteException("Unknown response, status code : " + resp.getStatusCode());


      BackupAgentResponse responce = new BackupAgentResponse(resp.getData(), resp.getStatusCode());
      return responce;
    } catch (ModuleException e) {
      throw new BackupExecuteException(e.getMessage(), e);
    } 
    
  }

}
