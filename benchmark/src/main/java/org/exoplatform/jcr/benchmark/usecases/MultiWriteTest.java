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
package org.exoplatform.jcr.benchmark.usecases;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;

import org.exoplatform.jcr.benchmark.JCRTestBase;
import org.exoplatform.jcr.benchmark.JCRTestContext;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: MultiWriteTest.java 111 2008-11-11 11:11:11Z serg $
 */
public class MultiWriteTest extends JCRTestBase {

  private final static int SIZE      = 5 * 1024 * 1024;

  static int               threadnum = 0;

  private Node             parent;

  private File             file;

  int                      curnum;

  public InputStream getStream() throws IOException {
    return new FileInputStream(file);
  }

  private Property prop;

  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {

    curnum = threadnum++;
    file = File.createTempFile("Thread", "_" + curnum);

    OutputStream out = new FileOutputStream(file);
    String s = String.valueOf(curnum);
    for (int j = 0; j < SIZE; j++) {
      out.write(s.getBytes());
    }
    out.close();

    System.out.println(file.getAbsolutePath() + "   added");

    Session session = context.getSession();
    Node rootNode = session.getRootNode();
    if (rootNode.hasNode("parentNode")) {
      parent = rootNode.getNode("parentNode");
      System.out.println(curnum + " get node");
    } else {
      parent = rootNode.addNode("parentNode");
      session.save();
      System.out.println(curnum + " ADD node");
    }

    if (parent.hasProperty("prop")) {
      prop = parent.getProperty("prop");
      System.out.println(curnum + " get prop");
    } else {
      // create big file
      File f = File.createTempFile("Thread", "_first");
      out = new FileOutputStream(f);
      for (int j = 0; j < SIZE; j++) {
        out.write("a".getBytes());
      }
      out.close();
      System.out.println(f.getAbsolutePath() + "   added");

      prop = parent.setProperty("prop", new FileInputStream(f));
      session.save();
      System.out.println(curnum + " ADD prop");
    }

  }

  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    try {
      System.out.println("doRun " + curnum);
      prop.setValue(getStream());
      parent.save();
    } catch (Exception e) {
      System.out.println( "====================" + curnum + " thread : ");
      e.printStackTrace();
      //throw new Exception(e);
    }
  }

  public void doFinish(final TestCase tc, JCRTestContext context) throws Exception {

    
    InputStream in = parent.getProperty("prop").getStream();

    byte[] buf = new byte[4];
    in.read(buf);
    in.close();
    System.out.println(curnum + " - " + new String(buf));
  }
}
