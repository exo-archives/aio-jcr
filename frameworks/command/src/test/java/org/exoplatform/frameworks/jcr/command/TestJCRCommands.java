/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.command;

import java.util.Iterator;

import javax.jcr.Credentials;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.SimpleCredentials;

import junit.framework.TestCase;

import org.apache.commons.chain.Command;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.frameworks.jcr.command.core.AddNodeCommand;
import org.exoplatform.frameworks.jcr.command.core.SaveCommand;
import org.exoplatform.services.command.impl.CommandService;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: JCRCommandsTest.java 9797 2006-10-26 12:00:12Z geaz $
 */
public class TestJCRCommands extends TestCase {

  private StandaloneContainer container;
  private CommandService cservice;
  private BasicAppContext ctx;

  //
  public void setUp() throws Exception {

    String containerConf = getClass().getResource(
        "/conf/standalone/test-configuration.xml").toString();
    String loginConf = Thread.currentThread().getContextClassLoader()
        .getResource("login.conf").toString();

    if (System.getProperty("java.security.auth.login.config") == null)
      System.setProperty("java.security.auth.login.config", loginConf);

    StandaloneContainer.addConfigurationURL(containerConf);
    container = StandaloneContainer.getInstance();

    RepositoryService repService = (RepositoryService) container
        .getComponentInstanceOfType(RepositoryService.class);

    cservice = (CommandService) container
        .getComponentInstanceOfType(CommandService.class);

    Credentials cred = new SimpleCredentials("admin", "admin".toCharArray());

    ctx = new BasicAppContext(repService.getDefaultRepository(), cred);

    // System.out.println("CTX "+ctx);
  }

  public void testCatalogInit() throws Exception {
    Iterator cs = cservice.getCatalog().getNames();
    assertTrue(cs.hasNext());
    while (cs.hasNext()) {
      System.out.println(cs.next());
    }
  }

  public void testAddNode() throws Exception {

    AddNodeCommand addNode = (AddNodeCommand) cservice.getCatalog().getCommand(
        "addNode");
    System.out.println(" " + addNode);
    ctx.put("currentNode", "/");
    ctx.put(addNode.getPathKey(), "test");
    addNode.execute(ctx);

    System.out.println(">>> " + ctx.get(addNode.getResultKey()));

    SaveCommand save = (SaveCommand) cservice.getCatalog().getCommand("save");
    // ctx.remove(save.getPathKey());
    ctx.put(addNode.getPathKey(), "/");
    save.execute(ctx);

    System.out.println(">>> SAVE >>> ");
  }

  public void testSetProperty() throws Exception {

    Command c = cservice.getCatalog().getCommand("setProperty");
    ctx.put("currentNode", "/test");
    ctx.put("name", "testProperty");
    ctx.put("propertyType", PropertyType.TYPENAME_STRING);
    ctx.put("values", "testValue");
    ctx.put("multiValued", Boolean.FALSE);

    c.execute(ctx);

    System.out.println("> set property>> " + ctx.get("result"));

    Command save = cservice.getCatalog().getCommand("save");
    save.execute(ctx);

  }

  public void testGetNodes() throws Exception {

    Command c = cservice.getCatalog().getCommand("getNodes");
    ctx.put("currentNode", "/");
    c.execute(ctx);

    assertTrue(ctx.get("result") instanceof NodeIterator);
    NodeIterator nodes = (NodeIterator) ctx.get("result");

    // System.out.println("> getNodes >> "+nodes.getSize());

    assertTrue(nodes.getSize() > 0);
  }

  public void testAddResourceFile() throws Exception {

    Command c = cservice.getCatalog().getCommand("addResourceFile");
    ctx.put("currentNode", "/");
    ctx.put("path", "resource");
    ctx.put("data", "Node data");
    ctx.put("mimeType", "text/html");

    c.execute(ctx);

    System.out.println(">>> Resource >>> " + ctx.get("result"));

    Command save = cservice.getCatalog().getCommand("save");
    ctx.put("path", "/");
    save.execute(ctx);

  }

  public void testGetNodeChain() throws Exception {
    Command cmd = cservice.getCatalog().getCommand("retrieveNodeCommand");
    ctx.put("currentNode", "/");
    ctx.put("path", "test");
    cmd.execute(ctx);

    System.out.println("RESULT >>>>>> " + ctx.get("result"));

  }
}
