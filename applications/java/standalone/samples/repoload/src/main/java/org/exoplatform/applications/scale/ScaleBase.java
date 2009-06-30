/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.applications.scale;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.exoplatform.services.log.Log;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.CredentialsImpl;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.org.ua reshetnyak.alex@gmail.com 07.05.2007
 * 15:53:19
 * 
 * @version $Id: ScaleBase.java 07.05.2007 15:53:19 rainfox
 */
public class ScaleBase {
  Log                                log = ExoLogger.getLogger("repload.ScaleBase");

  String                             args[];

  private String                     sConf;

  private String                     sRepository;

  private String                     sWorkspace;

  private String                     sRoot;

  private String                     sVdfile;

  private String                     sMimeType;

  private StandaloneContainer        container;

  private CredentialsImpl            credentials;

  private RepositoryService          repositoryService;

  private RepositoryImpl             repository;

  private SessionImpl                session;

  private Node                       root;

  private Node                       rootTestNode;

  private long                       min, max, sum;

  private int                        iteration = 100;

  private double                     avr;

  public ScaleBase(String[] args) {
    this.args = args;
    try {
      this.initRepository();
    } catch (Exception e) {
      log.error("Error init reposytory", e);
    }
    
  }

  public void initRepository() throws Exception {
    try {
      sConf = getPartam("-conf", args);
      sRepository = getPartam("-repo", args);
      sWorkspace = getPartam("-ws", args);
      sRoot = getPartam("-root", args);
      sVdfile = getPartam("-vdfile", args);
      sMimeType = getPartam("-mimeType", args);
      
      log.info("-conf: " + sConf);

      StandaloneContainer.setConfigurationPath(sConf);
      
      container = StandaloneContainer.getInstance();

      if (System.getProperty("java.security.auth.login.config") == null)
        System.setProperty("java.security.auth.login.config", Thread.currentThread()
            .getContextClassLoader().getResource("login.conf").toString());

      credentials = new CredentialsImpl("admin", "admin".toCharArray());

      repositoryService = (RepositoryService) container
          .getComponentInstanceOfType(RepositoryService.class);

      repository = (RepositoryImpl) repositoryService.getRepository(sRepository);
      if (repository != null)
        log.info("--->>> perository");

      session = (SessionImpl) repository.login(credentials, sWorkspace);
      if (session != null)
        log.info("--->>> session");

      root = session.getRootNode();
      if (root != null)
        log.info("--->>> root");
    } catch (Exception e) {
      log.error("Can not initialize repository", e);
    }

    try {
      if (sRoot.startsWith("/")) {
        rootTestNode = (Node) session.getItem(sRoot);
        log.info("--->>> Node " + sRoot + " exist");
      } else
        new Exception("Test root is not absolute path: " + sRoot);

    } catch (PathNotFoundException e) {
      try {

        // root.addNode(sRoot);
        rootTestNode = addNodes(sRoot, root);
        session.save();
        log.info("--->>> Node " + sRoot + " create");
        // rootTestNode = (NodeImpl) root.getNode(sRoot);
        if (rootTestNode != null)
          log.info("--->>> rootTestNode");
      } catch (Exception ee) {
        log.error("Can not create roottest node: " + sRoot, ee);
      }
    }
  }

  String getPartam(String paramName, String[] args) {
    for (int i = 0; i < args.length; i++) {
      String para[] = args[i].split("=");
      if (para[0].equals(paramName))
        return para[1];
    }
    return "";
  }

  private Node addNodes(String sRoot, Node parentNode) throws Exception {
    String mas[] = sRoot.split("/");

    Node temp = parentNode;

    for (int i = 1; i < mas.length; i++) {
      if (temp.hasNode(mas[i]))
        temp = (NodeImpl) temp.getNode(mas[i]);
      else {
        temp = (NodeImpl) temp.addNode(mas[i]);
        session.save();
      }
    }

    return temp;
  }

  public void createFolder() throws RepositoryException {
    Node createBase = rootTestNode.addNode("createFolder", "nt:folder");

    min = 100000;
    max = -1;
    sum = 0;

    for (int i = 0; i < iteration; i++) {
      long start = System.currentTimeMillis();
      createBase.addNode("folder" + i, "nt:folder");
      session.save();
      long end = System.currentTimeMillis();

      long tmp = end - start;

      if (tmp > max)
        max = tmp;
      if (tmp < min)
        min = tmp;

      sum += tmp;
    }

    avr = sum / (double) iteration;

    System.out.println("\nCreate folder:");
    System.out.println("\tavr: " + avr + " ms");
    System.out.println("\tmin: " + min + " ms");
    System.out.println("\tmax: " + max + " ms");
  }

  public void deleteFolder() throws RepositoryException {
    Node deleteBase = rootTestNode.addNode("deleteFolder", "nt:folder");

    min = 10000000;
    max = 0;
    sum = 0;

    for (int i = 0; i < iteration; i++) {

      Node temp = deleteBase.addNode("folder" + i, "nt:folder");
      session.save();

      long start = System.currentTimeMillis();
      temp.remove();
      session.save();
      long end = System.currentTimeMillis();

      long tmp = end - start;

      if (tmp > max)
        max = tmp;
      if (tmp < min)
        min = tmp;

      sum += tmp;
    }

    avr = sum / (double) iteration;

    System.out.println("\nDelete folder:");
    System.out.println("\tavr: " + avr + " ms");
    System.out.println("\tmin: " + min + " ms");
    System.out.println("\tmax: " + max + " ms");
  }

  public void uploadFile() throws RepositoryException, IOException {
    Node uploadBase = rootTestNode.addNode("uploadFolder", "nt:folder");

    min = 10000000;
    max = 0;
    sum = 0;

    for (int i = 0; i < iteration; i++) {
      long start = System.currentTimeMillis();
        Node nodeFile = uploadBase.addNode("file" + i, "nt:file");
        Node contentNode = nodeFile.addNode("jcr:content", "nt:resource");
        contentNode.setProperty("jcr:data", new FileInputStream(sVdfile));
        contentNode.setProperty("jcr:mimeType", sMimeType);
        contentNode.setProperty("jcr:lastModified", Calendar.getInstance());
        session.save();
      long end = System.currentTimeMillis();

      long tmp = end - start;

      if (tmp > max)
        max = tmp;
      if (tmp < min)
        min = tmp;
      
      sum+=tmp;
    }
    
    avr = sum / (double)iteration;
    
    System.out.println("\nUpload file:");
    System.out.println("\tavr: " + avr + " ms");
    System.out.println("\tmin: " + min + " ms");
    System.out.println("\tmax: " + max + " ms");
  }

  public void downloadFile() throws RepositoryException, IOException {
    Node uploadBase = rootTestNode.getNode("uploadFolder");

    min = 10000000;
    max = 0;
    sum = 0;

    for (int i = 0; i < iteration; i++) {
      long start = System.currentTimeMillis();
      Node temp = uploadBase.getNode("file" + i);
      InputStream is = temp.getNode("jcr:content").getProperty("jcr:data").getStream();

      byte buf[] = new byte[200 * 1024];
      int len;
      while ((len = is.read(buf)) > 0) {
      }

      long end = System.currentTimeMillis();

      long tmp = end - start;

      if (tmp > max)
        max = tmp;
      if (tmp < min)
        min = tmp;

      sum += tmp;
    }

    avr = sum / (double) iteration;

    System.out.println("\nDownload file:");
    System.out.println("\tavr: " + avr + " ms");
    System.out.println("\tmin: " + min + " ms");
    System.out.println("\tmax: " + max + " ms");
  }
}
  