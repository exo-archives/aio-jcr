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
package org.exoplatform.services.jcr.ext.replication.external;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * Created by The eXo Platform SAS
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a> 
 * @version $Id: BaseAutintificationHttpClient.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class BasicAuthenticationHttpClient {
  private HttpClient client;

  public BasicAuthenticationHttpClient(String ipAdress, int port, String login, String password) {
    client = new HttpClient();
    client.getState().setCredentials(new AuthScope(ipAdress, port),
        new UsernamePasswordCredentials(login, password));
  }
  
  public BasicAuthenticationHttpClient(MemberInfo info) {
    client = new HttpClient();
    client.getState().setCredentials(new AuthScope(info.getIpAddress(), info.getPort()),
        new UsernamePasswordCredentials(info.getLogin(), info.getPassword()));
  }

  public String execute(String uri) {
    String result = "fail";
    GetMethod get = new GetMethod(uri);
    get.setDoAuthentication(true);

    try {
      // execute the GET
      int status = client.executeMethod(get);

      // print the status and response
      System.out.println(status + "\n" + get.getResponseBodyAsString());

      result = get.getResponseBodyAsString();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      // release any connection resources used by the method
      get.releaseConnection();
    }

    return result;
  }
}