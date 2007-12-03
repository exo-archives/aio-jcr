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
package org.exoplatform.jcr.benchmark.usecases;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.Value;

import org.exoplatform.jcr.benchmark.JCRTestBase;
import org.exoplatform.jcr.benchmark.JCRTestContext;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */
public class AddNtFileWithMetadataTest extends JCRTestBase {
  /*
   * This test calculates the time (ms or tps) of adding of one node of type nt:file 
   * (including addNode(), setProperty(), addMixin(), save() methods).
   * Required parameters:
   * jcr.lengthOfFile - file length in bytes, need to fill the content of jcr:data property;
   * jcr.lengthOfDcElementSetProperty - e.g 10 means that dc:title will be like "1234567890"; 
   * jcr.countOfDcElementSetProperties - must be less or equals 5, e.g 1 means "dc:title" property will be present only;
   *     
  */
  private byte[]   contentOfFile                 = null;

  private byte[]   contentOfDcElementSetProperty = null;

  private int      countOfDcElementSetProperties = 0;

  private String[] dcElementSetProperties        = { "dc:title", "dc:subject", "dc:description",
      "dc:publisher", "dc:resourceType"         };

  private Node     rootNode                      = null;

  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    contentOfFile = new byte[tc.getIntParam("jcr.lengthOfFile")];
    Arrays.fill(contentOfFile, (byte) 'F');
    contentOfDcElementSetProperty = new byte[tc.getIntParam("jcr.lengthOfDcElementSetProperty")];
    Arrays.fill(contentOfDcElementSetProperty, (byte) 'D');
    rootNode = context.getSession().getRootNode().addNode(context.generateUniqueName("rootNode"),
        "nt:unstructured");
    context.getSession().save();
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    Node nodeToAdd = rootNode.addNode(context.generateUniqueName("node"), "nt:file");
    Node contentNodeOfNodeToAdd = nodeToAdd.addNode("jcr:content", "nt:resource");
    contentNodeOfNodeToAdd.setProperty("jcr:data", new ByteArrayInputStream(contentOfFile));
    contentNodeOfNodeToAdd.setProperty("jcr:mimeType", "application/octet-stream");
    contentNodeOfNodeToAdd.setProperty("jcr:lastModified", Calendar.getInstance());
    contentNodeOfNodeToAdd.addMixin("dc:elementSet");
    contentNodeOfNodeToAdd.setProperty("dc:date", 
        createMultiValue(context, Calendar.getInstance()));
    for (int j = 0; j < countOfDcElementSetProperties; j++) {
      contentNodeOfNodeToAdd.setProperty(dcElementSetProperties[j], 
          createMultiValue(context, new String(contentOfDcElementSetProperty)));
    }
    context.getSession().save();
  }

  @Override
  public void doFinish(TestCase tc, JCRTestContext context) throws Exception {
    rootNode.remove();
    context.getSession().save();
  }

  private Value[] createMultiValue(JCRTestContext context, String sValue) throws Exception {
    Value[] values = new Value[1];
    values[0] = context.getSession().getValueFactory().createValue(sValue);
    return values;
  }

  private Value[] createMultiValue(JCRTestContext context, Calendar date) throws Exception {
    Value[] values = new Value[1];
    values[0] = context.getSession().getValueFactory().createValue(date);
    return values;
  }

}
