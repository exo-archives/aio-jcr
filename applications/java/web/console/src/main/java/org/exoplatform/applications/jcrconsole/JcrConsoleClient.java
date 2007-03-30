/*
 * Copyright 2004-2005 The Apache Software Foundation or its licensors,
 *                     as applicable.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exoplatform.applications.jcrconsole;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.SimpleCredentials;

import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.Command;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.frameworks.jcr.cli.CliAppContext;
import org.exoplatform.services.command.impl.CommandService;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * Command line interface client
 */
public class JcrConsoleClient {

  private CliAppContext       context;

  private StandaloneContainer container;

  private CommandService      cservice;

  private ArrayList<String>   params              = new ArrayList<String>();

  private final String        PARAMETERS_KEY      = "parametersss";

  private int                 CONSOLE_LINES_COUNT = 20;

  private int                 TMP_CONSOLE_LINES_COUNT;

  private boolean             exit                = false;

  private Catalog             catalog             = null;

  JcrConsoleClient() {
    initContext();
  }

  JcrConsoleClient(CliAppContext ctx) {
    this.context = ctx;
  }

  private void initContext() {
    try {
      if (System.getProperty("java.security.auth.login.config") == null)
        System.setProperty("java.security.auth.login.config", Thread.currentThread()
            .getContextClassLoader().getResource("login.conf").toString());

      String confPath = Thread.currentThread().getContextClassLoader().getResource(
          "conf/standalone/jcr-console-configuration.xml").toString();
      StandaloneContainer.setConfigurationURL(confPath);

      container = StandaloneContainer.getInstance();

      RepositoryService repService = (RepositoryService) container
          .getComponentInstanceOfType(RepositoryService.class);

      cservice = (CommandService) container.getComponentInstanceOfType(CommandService.class);

      catalog = cservice.getCatalog("CLI");

      Credentials cred = new SimpleCredentials("admin", "admin".toCharArray());

      // we need to login (see BasicAppContext, 38) and set current item before
      // ctx using
      context = new CliAppContext(repService.getRepository(), PARAMETERS_KEY, cred);
      Node root = context.getSession().getRootNode();
      context.setCurrentItem(root);
    } catch (Exception e) {
      context = null;
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    JcrConsoleClient client = new JcrConsoleClient();
    client.run(args);
  }

  private void run(String[] args) {
    // Prompt command
    System.out.println("*** Welcome to eXo JCR Console ***");
    while (!exit) {
      try {
        System.out.print(">");
        // Read input
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String input = bufferedReader.readLine();
        if (input.trim().equals("exit") || input.trim().equals("quit")) { // exit?
          exit = true;
          System.out.println("Good bye...");
        } else if (input.trim().length() == 0) {
          // Do nothing
        } else {
          this.runCommand(input);
        }
      } catch (Exception e) {
        System.out.println("Invalid command");
      }
    }
    System.exit(0);// exit application
  }

  void runCommand(String input) throws Exception {
    if (input.startsWith("#") || input.length() == 0) {
      return;
    }
    String commandLine = input;
    String command = commandLine.substring(0, (commandLine.indexOf(" ") < 0) ? commandLine.length()
        : commandLine.indexOf(" "));
    commandLine = commandLine.substring(commandLine.indexOf(command) + command.length());
    commandLine = commandLine.trim();

    Command commandToExecute = catalog.getCommand(command);
    params = parseQuery(commandLine);
    context.put(PARAMETERS_KEY, params);
    commandToExecute.execute(context);
    // here we should organize formatted console output
    String[] strings = input.split("\\|");
    if (strings.length == 2) {
      TMP_CONSOLE_LINES_COUNT = new Integer(strings[1]);
    } else {
      TMP_CONSOLE_LINES_COUNT = CONSOLE_LINES_COUNT;
    }
    // *************************************************
    String output = context.getOutput();
    String[] outputStrings = output.split("\n");
    int outputLinesCount = outputStrings.length;
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    if (outputLinesCount > TMP_CONSOLE_LINES_COUNT) {
      int i = 0;
      int j = 0;
      boolean f = true;
      while ((i < outputLinesCount) || (f == true)) {
        if (j == TMP_CONSOLE_LINES_COUNT) {
          System.out.println("Continue output? [y/n]");
          String yesOrNo = in.readLine();
          if (yesOrNo.equalsIgnoreCase("y")) {
            j = 0;
            f = false;
            continue;
          } else if (yesOrNo.equalsIgnoreCase("n")) {
            f = false;
            break;
          } else {
            continue;
          }
        }
        System.out.println(outputStrings[i]);
        i++;
        j++;
      }
    } else {
      System.out.print(output);
    }
    // *************************************************
  }

  private ArrayList parseQuery(String query) {
    int i = 0;
    ArrayList paramsss = new ArrayList<String>();
    try {
      paramsss.clear();
      if (query.indexOf("\"") == -1) {
        while (!query.equals("")) {
          String item = query.substring(0, (query.indexOf(" ") < 0) ? query.length() : query
              .indexOf(" "));

          paramsss.add(item);
          query = query.substring(query.indexOf(item) + item.length());
          query = query.trim();
          i++;
        }
      } else {

        while (!query.equals("")) {
          String item = "";
          if (query.startsWith("\"")) {
            item = query.substring(query.indexOf("\""), (query.indexOf("\"", 1) < 0) ? query
                .length() : query.indexOf("\"", 1) + 1);
          } else {
            item = query.substring(0, (query.indexOf(" ") < 0) ? query.length() : query
                .indexOf(" "));
          }
          item = item.trim();
          if (item != null && item != "") {
            paramsss.add(item);
          }
          query = query.substring(query.indexOf(item) + item.length());
          query = query.trim();
          i++;
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return paramsss;
  }

}
