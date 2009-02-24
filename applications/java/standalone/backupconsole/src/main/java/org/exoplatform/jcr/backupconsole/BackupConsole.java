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

  private static final String incorrectParam = "Incorrect parameter: ";
  private static final String toManyParams = "Too may parameters.";
  
  private static final String HELP_INFO =
    " Help info:"
    + "[-ssl] <auth> <host> <cmd> \n"
    + " <auth>:   login:pathword\n"
    + " <host>:   <host ip>:<port>\n"
    + " <cmd>:   start <repo/ws>  [ <incr>  <incr_jobnumber>]\n"
    + "          stop  <repo/ws>\n"
    + "          status <repo/ws>\n"
    + "          restore <repo/ws> <path>\n\n"
    + " <repo/ws>   -   /<reponame>/<ws name>\n"
    + " <path>      -   path to backup file\n"
    + " <incr>       - iterations count\n"
    + " <inr_jobnumber> - inremential job number\n";
    
  public static void main(String[] args) {

  //  for (int i = 0; i < args.length; i++) {
  //    System.out.println(args[i]);
  //  }

    int curArg = 0;

    if (curArg == args.length) {
      System.out.println(incorrectParam + "There is no any parameters.");
      return;
    }

    if (args[curArg].equalsIgnoreCase("help")) {
      System.out.println(HELP_INFO);
        return;
    }

    boolean isSSL = false;
    if (args[curArg].equalsIgnoreCase("-ssl")) {
      isSSL = true;
      curArg++;
    }

    if (curArg == args.length) {
      System.out.println(incorrectParam + "There is no Host:port parameter.");
      return;
    }
    String host = args[curArg++];
    // TODO check host;

    if (curArg == args.length) {
      System.out.println(incorrectParam + "There is no Login@Pathword parameter.");
      return;
    }
    String login = args[curArg++];
    // TODO check login

    ClientTransport transport = new ClientTransportImpl(host, login, isSSL);
    BackupClient client = new BackupClientImpl(transport);

    if (curArg == args.length) {
      System.out.println(incorrectParam + "There is no command parameter.");
      return;
    }
    String command = args[curArg++];

 

    if (command.equalsIgnoreCase("start")) {
      if (curArg == args.length) {
        System.out.println(incorrectParam + "There is no path to workspace parameter.");
        return;
      }
      String pathToWS = args[curArg++];
      if (curArg == args.length) {
        client.startBackUp(pathToWS);
      } else {

        String incr = args[curArg++];

        long inc = 0;
        try {
           inc = Long.parseLong(incr);
        } catch (NumberFormatException e) {
          System.out.println(incorrectParam + " Increment is not didgit - " + e.getMessage());
          return;
        }

        if (curArg == args.length) {
          System.out.println(incorrectParam + "There is no job number parameter.");
          return;
        }
        String jobNumber = args[curArg++];
        
        int jn = 0;
        try{
          jn = Integer.parseInt(jobNumber);
        }catch (NumberFormatException e) {
          System.out.println(incorrectParam + " Job number is not didgit - " + e.getMessage());
          return;
        }
        
        if (curArg < args.length) {
          System.out.println(toManyParams);
          return;
        }
        client.startIncrementalBackUp(pathToWS, inc, jn);
      }
    } else if (command.equalsIgnoreCase("stop")) {
      if (curArg == args.length) {
        System.out.println(incorrectParam + "There is no path to workspace parameter.");
        return;
      }
      String pathToWS = args[curArg++];
      if (curArg < args.length) {
        System.out.println(toManyParams);
        return;
      }
      client.stop(pathToWS);
    } else if (command.equalsIgnoreCase("status")) {
      if (curArg == args.length) {
        System.out.println(incorrectParam + "There is no path to workspace parameter.");
        return;
      }
      String pathToWS = args[curArg++];
      if (curArg < args.length) {
        System.out.println(toManyParams);
        return;
      }
      client.status(pathToWS);
    } else if (command.equalsIgnoreCase("restore")) {
      if (curArg == args.length) {
        System.out.println(incorrectParam + "There is no path to workspace parameter.");
        return;
      }
      String pathToWS = args[curArg++];

      if (curArg == args.length) {
        System.out.println(incorrectParam + "There is no source-name parameter.");
        return;
      }
      String srcname = args[curArg++];
       
      if (curArg == args.length) {
        System.out.println(incorrectParam + "There is no path to backup file parameter.");
        return;
      }
      String pathToBackup = args[curArg++];
      
      if (curArg < args.length) {
        System.out.println(toManyParams);
        return;
      }
      client.restore(pathToWS, srcname, pathToBackup);
    } else {
      System.out.println("Unknown command <" + command + ">");
    }
  }
}
