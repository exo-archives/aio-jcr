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
package org.exoplatform.services.jcr.impl.core;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.exoplatform.services.jcr.JcrImplBaseTest;

/**
 * Created by The eXo Platform SAS. <br/>
 * 
 * Date: 08.05.2008 <br/>
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: TestRestoreWorkspaceInitializer.java 13986 2008-05-08 10:48:43Z pnedonosko $
 */
public class TestRestoreWorkspaceInitializer extends JcrImplBaseTest {

  /**
   * Make export of system ws with custom NT with multivalued properties.
   * 
   * @throws Exception
   */
  public void _testExportSystemWorkspace() throws Exception {

    Node multiv = root.addNode("multivaluedProperty", "exojcrtest:multiValued");
    multiv.setProperty("exojcrtest:multiValuedString", new String[] { "value1" });

    Value v1 = session.getValueFactory().createValue(Calendar.getInstance());
    multiv.setProperty("exojcrtest:multiValuedDate", new Value[] { v1 });

    JCRName jcrName = session.getLocationFactory().parseJCRName("exojcrtest:dummyName");
    v1 = session.getValueFactory().createValue(jcrName);
    multiv.setProperty("exojcrtest:multiValuedName", new Value[] { v1 });

    root.save();

    File outf = new File("./sv_export_root.xml");
    FileOutputStream out = new FileOutputStream(outf);
    session.exportWorkspaceSystemView(out, false, false);
    out.close();
  }

  /**
   * Should be used with RestoreWorkspaceInitializer and export file obtained in the test testExportSystemWorkspace().
   * 
   * Sample config:
   * <initializer class="org.exoplatform.services.jcr.impl.core.RestoreWorkspaceInitializer"> 
   *   <properties> 
   *     <property name="restore-path" value="./sv_export_root.xml"/>
   *   </properties>
   * </initializer>
   * 
   * @throws Exception
   */
  public void testCheckRestoreSystemWorkspace() throws Exception {

    if (root.hasNode("multivaluedProperty")) {
      Node multiv = root.getNode("multivaluedProperty");
      try {
        Property p = multiv.getProperty("exojcrtest:multiValuedString");
        p.getValues();
        p = multiv.getProperty("exojcrtest:multiValuedDate");
        p.getValues();
        p = multiv.getProperty("exojcrtest:multiValuedName");
        p.getValues();
      } catch (ValueFormatException e) {
        e.printStackTrace();
        fail(e.getMessage());
      }
    } // else skip test
  }
}
