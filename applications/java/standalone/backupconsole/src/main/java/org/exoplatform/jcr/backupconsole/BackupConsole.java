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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: BackupConsole.java 111 2008-11-11 11:11:11Z serg $
 */
public class BackupConsole {

  private static final String INCORRECT_PARAM     = "Incorrect parameter: ";

  private static final String TO_MANY_PARAMS      = "Too many parameters.";

  private static final String LOGIN_PASS_SPLITTER = ":";
  
  private static final String FORCE_CLOSE = "force-close-session";

  private static final String HELP_INFO           = "Help info:\n"
                                                      + " [-ssl] <auth> <host> <cmd> \n"
                                                      + " <auth> :   login:pathword\n"
                                                      + " <host> :   <host ip>:<port>/<context>\n"
                                                      + " <cmd>  :   start <repo/ws>  [ <incr>  <incr_jobnumber>]\n"
                                                      + "            stop  <repo/ws>\n"
                                                      + "            status <repo/ws>\n"
                                                      + "            restore <repo/ws> <path> <pathToConfigFile>\n"
                                                      + "            drop [force-close-session] <repo/ws>  \n\n"
                                                      + " <repo/ws>   - /<reponame>/<ws name>\n"
                                                      + " <path>      - path to backup file\n"
                                                      + " <incr>      - incemental job period\n"
                                                      + " <incr_jobnumber> - inremential job number\n"
                                                      + " force-close-session - do server need to close opened session.\n";

  /**
   * Main.
   * 
   * @param args - arguments used as parameters for execute backup server
   *          commands.
   */
  public static void main(String[] args) {

    int curArg = 0;
    if (curArg == args.length) {
      System.out.println(INCORRECT_PARAM + "There is no any parameters.");
      return;
    }

    // help
    if (args[curArg].equalsIgnoreCase("help")) {
      System.out.println(HELP_INFO);
      return;
    }

    // this parameter is always first so do not check is it exist
    boolean isSSL = false;
    if (args[curArg].equalsIgnoreCase("-ssl")) {
      isSSL = true;
      curArg++;
    }

    // login:password
    if (curArg == args.length) {
      System.out.println(INCORRECT_PARAM + "There is no Login:Password parameter.");
      return;
    }
    String login = args[curArg++];
    if (!login.matches("[^:]+:[^:]+")) {
      System.out.println(INCORRECT_PARAM + "There is incorrect Login:Password parameter - " + login);
      return;
    }

    // host:port
    if (curArg == args.length) {
      System.out.println(INCORRECT_PARAM + "There is no Host:Port parameter.");
      return;
    }
    String host = args[curArg++];
    //if (!host.matches("\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}:\\d{1,6}")) {
    //  System.out.println(INCORRECT_PARAM + "There is incorrect Host:Port parameter - " + host);
   //   return;
   // }

    // initialize transport and backup client
    String[] lp = login.split(LOGIN_PASS_SPLITTER);
    ClientTransport transport = new ClientTransportImpl(lp[0], lp[1], host, isSSL);
    BackupClient client = new BackupClientImpl(transport, lp[0], lp[1]);

    // commands
    if (curArg == args.length) {
      System.out.println(INCORRECT_PARAM + "There is no command parameter.");
      return;
    }
    String command = args[curArg++];

    try {
      if (command.equalsIgnoreCase("start")) {
        String pathToWS = getRepoWS(args, curArg++);
        if (pathToWS == null)
          return;

        if (curArg == args.length) {
          System.out.println(client.startBackUp(pathToWS));
        } else {
          // incremental job period
          String incr = args[curArg++];
          long inc = 0;
          try {
            inc = Long.parseLong(incr);
          } catch (NumberFormatException e) {
            System.out.println(INCORRECT_PARAM + "Incemental job period is not didgit - "
                + e.getMessage());
            return;
          }

          // incremental job number
          if (curArg == args.length) {
            System.out.println(INCORRECT_PARAM + "There is no job number parameter.");
            return;
          }
          String jobNumber = args[curArg++];
          int jn = 0;
          try {
            jn = Integer.parseInt(jobNumber);
          } catch (NumberFormatException e) {
            System.out.println(INCORRECT_PARAM + "Job number is not didgit - " + e.getMessage());
            return;
          }

          if (curArg < args.length) {
            System.out.println(TO_MANY_PARAMS);
            return;
          }
          System.out.println(client.startIncrementalBackUp(pathToWS, inc, jn));
        }
      } else if (command.equalsIgnoreCase("stop")) {
        String pathToWS = getRepoWS(args, curArg++);
        if (pathToWS == null)
          return;

        if (curArg < args.length) {
          System.out.println(TO_MANY_PARAMS);
          return;
        }
        System.out.println(client.stop(pathToWS));
      } else if (command.equalsIgnoreCase("drop")) {
        
        if (curArg == args.length) {
          System.out.println(INCORRECT_PARAM + "There is no path to workspace or force-session-close parameter.");
        }
        
        String param = args[curArg++];
        boolean isForce = true;
        
        if(!param.equalsIgnoreCase(FORCE_CLOSE)){
          curArg--; 
          isForce = false; 
        }
        
        String pathToWS = getRepoWS(args, curArg++);
        
        if (pathToWS == null)
          return;

        if (curArg < args.length) {
          System.out.println(TO_MANY_PARAMS);
          return;
        }
        System.out.println(client.drop(isForce, pathToWS));
      } else if (command.equalsIgnoreCase("status")) {
        String pathToWS = getRepoWS(args, curArg++);
        if (pathToWS == null)
          return;

        if (curArg < args.length) {
          System.out.println(TO_MANY_PARAMS);
          return;
        }
        System.out.println(client.status(pathToWS));
      } else if (command.equalsIgnoreCase("restore")) {

        String pathToWS = getRepoWS(args, curArg++);
        if (pathToWS == null)
          return;

        // path to backup file
        if (curArg == args.length) {
          System.out.println(INCORRECT_PARAM + "There is no path to backup file parameter.");
          return;
        }
        String pathToBackup = args[curArg++];

        if (curArg == args.length) {
          System.out.println(INCORRECT_PARAM + "There is no path to config file parameter.");
          return;
        }
        String pathToConf = args[curArg++];

        File conf = new File(pathToConf);
        if (!conf.exists()) {
          System.out.println(" File " + pathToConf + " do not exist. Check the path.");
          return;
        }

        if (curArg < args.length) {
          System.out.println(TO_MANY_PARAMS);
          return;
        }
        System.out.println(client.restore(pathToWS, pathToBackup, new FileInputStream(conf)));
      } else {
        System.out.println("Unknown command <" + command + ">");
      }

    } catch (IOException e) {
      System.out.println("ERROR: " + e.getMessage());
      e.printStackTrace();
    } catch (BackupExecuteException e) {
      System.out.println("ERROR: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Get parameter from argument list, check it and return as valid path to
   * repository and workspace.
   * 
   * @param args list of arguments.
   * @param curArg argument index.
   * @return String valid path.
   */
  private static String getRepoWS(String[] args, int curArg) {
    if (curArg == args.length) {
      System.out.println(INCORRECT_PARAM + "There is no path to workspace parameter.");
      return null;
    }
    // make correct path
    String repWS = args[curArg];
    repWS = repWS.replaceAll("\\\\", "/");

    if (!repWS.matches("[/][^/]+[/][^/]+")) {
      System.out.println(INCORRECT_PARAM + "There is incorrect path to workspace parameter: "
          + repWS);
      return null;
    } else {
      return repWS;
    }
  }
}
