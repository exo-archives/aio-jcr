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
package org.exoplatform.services.jcr.ext.replication.async.transport;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 21.01.2009
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: SimpleChat.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public class SimpleChat extends ReceiverAdapter {
  JChannel     channel;

  final String userName;
  
  final String config;

  public static void main(String[] args) throws Exception {
    if (args.length > 0) {
      String config = null;
      String user;
      if (args[0].startsWith("-config")) {
        config = args[0].split("=")[1];
        System.out.println("use config " + config);
      }
      if (args.length > 1 && args[1].startsWith("-user")) {
        user = args[1].split("=")[1];
        System.out.println("use username " + user);
        new SimpleChat(user, config).start();
      } else
        System.out.println("user name expected, try java.... -user=exo");
    } else
      System.out.println("user name expected, try java.... -user=exo");
  }

  SimpleChat(String userName, String config) {
    this.userName = userName;
    this.config = config;
  }

  public void viewAccepted(View new_view) {
    System.out.println(userName + " ** view: " + new_view);
  }

  public void receive(Message msg) {
    System.out.println(userName + " " + msg.getSrc() + ": " + msg.getObject());
  }

  private void start() throws Exception {
    channel = config != null ? new JChannel(config) : new JChannel();
    channel.setReceiver(this);
    channel.connect("ChatCluster");
    eventLoop();
    channel.close();
  }

  private void eventLoop() {
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    while (true) {
      try {
        System.out.print(userName + "> ");
        System.out.flush();
        String line = in.readLine().toLowerCase();
        if (line.startsWith("quit") || line.startsWith("exit")) {
          break;
        }
        line = "[" + userName + "] " + line;
        Message msg = new Message(null, null, line);
        channel.send(msg);
      } catch (Exception e) {
      }
    }
  }
}
