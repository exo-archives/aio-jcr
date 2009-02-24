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

import org.exoplatform.common.http.client.CookieModule;
import org.exoplatform.common.http.client.HTTPConnection;
import org.exoplatform.common.http.client.HTTPResponse;
import org.exoplatform.common.http.client.ModuleException;
import org.exoplatform.common.http.client.ParseException;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: ClientTransportImpl.java 111 2008-11-11 11:11:11Z serg $
 */
public class ClientTransportImpl implements ClientTransport {

  private final String  host;

  private final String  login;

  private final String  password;

  private final boolean isSSL;

  public ClientTransportImpl(String host, String login, String pathword, boolean isSSL) {
    this.host = host;
    this.login = login;
    this.password = pathword;
    this.isSSL = isSSL;
  }

  public String execute(String sURL) throws IOException, BackupExecuteException {
    String result = "fail";

    try {
      // execute the GET
      String complURL = "http" + (isSSL ? "s" : "") + "://" + host + sURL;
      
      System.out.println(complURL);
      URL url = new URL(complURL);
      HTTPConnection connection = new HTTPConnection(url);
      connection.removeModule(CookieModule.class);

      connection.addBasicAuthorization("eXo REST services", login, password);

      HTTPResponse resp = connection.Get(url.getFile());

      // print the status and response
      // if (resp.getStatusCode() != 200)
      // System.out.println(resp.getStatusCode() + "\n" + resp.getText());

      result = resp.getText();
    } catch (ModuleException e) {
      throw new BackupExecuteException(e.getMessage(), e);
    } catch (ParseException e) {
      throw new BackupExecuteException(e.getMessage(), e);
    }

    return result;
  }

}
