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

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: BackupConsole.java 111 2008-11-11 11:11:11Z serg $
 */
public class BackupConsole {

  public static void main(String[] args) {

    for (int i = 0; i < args.length; i++) {
      System.out.println(args[i]);
    }

    if (args[0].equalsIgnoreCase("help")) {
      // TODO print help
      return;
    }

    int curArg = 0;
    boolean isSSL = false;
    if (args[curArg].equalsIgnoreCase("-ssl")) {
      isSSL = true;
      curArg++;
    }

    String host = args[curArg++];
    // TODO check host;
    String login = args[curArg++];
    // TODO check login
    ClientTransport transport = new ClientTransportImpl(host, login, isSSL);
    BackupClient client = new BackupClientImpl(transport);

    String command = args[curArg++];
    // all commands must have path to ws
    String pathToWs = args[curArg++];
    
    if (command.equalsIgnoreCase("start")) {
      
      

    } else if (command.equalsIgnoreCase("stop")) {

    } else if (command.equalsIgnoreCase("status")) {

    } else if (command.equalsIgnoreCase("restore")) {

    } else {
      System.out.println("Unknown command <" + command + ">");
    }
  }
}
