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
package org.exoplatform.jcr.benchmark.ext.asyncrep;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.jcr.benchmark.JCRTestContext;
import org.exoplatform.services.jcr.ext.replication.async.AsyncReplication;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: LoadFilesAndSynchronizeTest.java 111 2008-11-11 11:11:11Z serg $
 */
public class LoadFilesAndSynchronizeTest extends AsyncTestBase {

  private final int    COUNT_I       = 5;

  private final int    COUNT_J       = 5;

  private Node         root;

  public static byte[] contentOfFile = null;

  @Override
  public void doPrepare(TestCase tc, AsyncTestContext context) throws Exception {

    if (contentOfFile == null) {
      contentOfFile = new byte[(int) new File("../resources/benchmark.pdf").length()];
      InputStream is = new FileInputStream("../resources/benchmark.pdf");
      int offset = 0;
      int numRead = 0;
      while (offset < contentOfFile.length
          && (numRead = is.read(contentOfFile, offset, contentOfFile.length - offset)) >= 0) {
        offset += numRead;
      }
      if (offset < contentOfFile.length) {
        throw new IOException("Could not completely read file ");
      }
      is.close();
    }

    String rootFolder = tc.getParam("ext.rootFolder");
    Session s = context.getSession();
    root = s.getRootNode().addNode(rootFolder); // , "nt:folder"
    s.save();
  }

  @Override
  public void doRun(final TestCase tc, AsyncTestContext context) throws Exception {
    Session s = context.getSession();

    for (int i = 0; i < COUNT_I; i++) {
      for (int j = 0; j < COUNT_J; j++) {
        // create file
        Node nodeToAdd = root.addNode("node_" + i + "_" + j, "nt:file");
        Node contentNodeOfNodeToAdd = nodeToAdd.addNode("jcr:content", "nt:resource");
        contentNodeOfNodeToAdd.setProperty("jcr:data", new ByteArrayInputStream(contentOfFile));
        contentNodeOfNodeToAdd.setProperty("jcr:mimeType", "application/pdf");
        contentNodeOfNodeToAdd.setProperty("jcr:lastModified", Calendar.getInstance());
        System.out.println(nodeToAdd.getName() + " file added");
      }
      s.save();
      System.out.println(i + " log saved");
    }

    AsyncReplication rep = context.getReplicationServer();
    rep.synchronize();
    System.out.println("Synchronize started.");

    // wait for synchronization end
    while (rep.isActive()) {
      Thread.sleep(3000);
    }

    // check files
    String opRootName = tc.getParam("ext.oponentRootFolder");

    if (!s.getRootNode().hasNode(opRootName)) {
      System.out.println("FAIL: there is no merged opponent folder.");
    } else {
      System.out.println("OK : opponent folder is merged.");
      Node opRoot = s.getRootNode().getNode(opRootName);
      if (opRoot.getNodes().getSize() != (COUNT_I * COUNT_J)) {
        System.out.println("FAIL: add files count is not expected. There is "
            + opRoot.getNodes().getSize() + " but must " + (COUNT_I * COUNT_J));
      } else {
        System.out.println("OK : opponent files is added.");
      }
    }
  }

  @Override
  public void doFinish(TestCase tc, AsyncTestContext context) throws Exception {
    root.remove();
    context.getSession().save();
  }
}
