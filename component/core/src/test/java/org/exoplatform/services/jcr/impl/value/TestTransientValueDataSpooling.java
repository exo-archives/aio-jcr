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
package org.exoplatform.services.jcr.impl.value;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;

import javax.imageio.stream.FileImageInputStream;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Value;

import org.exoplatform.services.jcr.BaseStandaloneTest;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.dataflow.EditableValueData;
import org.exoplatform.services.jcr.impl.dataflow.TesterTransientValueData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.io.SpoolFile;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date: 2009
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id$
 */
public class TestTransientValueDataSpooling extends BaseStandaloneTest {

  private final File tmpdir = new File(System.getProperty("java.io.tmpdir"));

  public void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * Write data from stream direct to the storage without spooling.
   * 
   * @throws Exception
   */
  public void testNotSpooling() throws Exception {
    File tmpFile = createBLOBTempFile(4048);

    int countBefore = tmpdir.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.startsWith("jcrvd");
      }
    }).length;

    NodeImpl node = (NodeImpl) root.addNode("testNode");
    node.setProperty("testProp", new FileInputStream(tmpFile));
    root.save();

    int countAfter = tmpdir.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.startsWith("jcrvd");
      }
    }).length;

    assertEquals(countBefore, countAfter);
  }

  /**
   * Spool steam on get operation.
   * 
   * @throws Exception
   */
  public void testRemoveAfterSet() throws Exception {
    File tmpFile = createBLOBTempFile(4048);

    int countBefore = tmpdir.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.startsWith("jcrvd");
      }
    }).length;

    Node node = root.addNode("testNode");
    node.setProperty("testProp", new FileInputStream(tmpFile));
    node.getProperty("testProp").getStream();
    root.save();

    int countAfter = tmpdir.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.startsWith("jcrvd");
      }
    }).length;

    assertEquals(countBefore, countAfter);
  }

  @Override
  protected String getRepositoryName() {
    // TODO Auto-generated method stub
    return null;
  }

}
