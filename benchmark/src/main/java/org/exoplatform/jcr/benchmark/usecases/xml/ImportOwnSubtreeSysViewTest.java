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
package org.exoplatform.jcr.benchmark.usecases.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.exoplatform.jcr.benchmark.JCRTestBase;
import org.exoplatform.jcr.benchmark.JCRTestContext;
import org.exoplatform.services.log.ExoLogger;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */
public class ImportOwnSubtreeSysViewTest extends JCRTestBase {
  /*
   * This test measures performance of exporting mechanism using docview method,
   * each thread has own node subtree of nodes
   */

  public static Log log      = ExoLogger.getLogger("jcr.benchmark");

  private Node      rootNode = null;

  private String    name     = "";

  private File      destFile = null;

  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    name = context.generateUniqueName("rootNode");
    rootNode = context.getSession().getRootNode().addNode(name);
    Node file = rootNode.addNode("child1").addNode("child2").addNode("file", "nt:file");
    Node content = file.addNode("jcr:content", "nt:resource");
    content.setProperty("jcr:data", new FileInputStream("../resources/benchmark.gif"));
    content.setProperty("jcr:mimeType", "application/pdf");
    content.setProperty("jcr:lastModified", Calendar.getInstance());
    context.getSession().save();
    destFile = File.createTempFile(name, ".xml");
    destFile.deleteOnExit(); 
    OutputStream out = new FileOutputStream(destFile);
    context.getSession().exportSystemView("/" + name, out, false, false);
    out.close();
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    InputStream is = new FileInputStream(destFile);
    context.getSession().importXML("/" + name, is, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
  }

  @Override
  public void doFinish(TestCase tc, JCRTestContext context) throws Exception {
    log.info("imported:" + context.getSession().getRootNode().getNode(name).getNode(name).getPath());
    rootNode.remove();
    context.getSession().save();
  }

}
