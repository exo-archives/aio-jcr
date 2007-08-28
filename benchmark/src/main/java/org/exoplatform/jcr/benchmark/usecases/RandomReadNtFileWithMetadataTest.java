/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.jcr.benchmark.usecases;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import javax.jcr.Node;
import javax.jcr.Value;

import org.exoplatform.jcr.benchmark.JCRTestBase;
import org.exoplatform.jcr.benchmark.JCRTestContext;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */
public class RandomReadNtFileWithMetadataTest extends JCRTestBase {
  /*
   * This test calculates the time (ms or tps) of random reading of node of type nt:file 
   * (including getNode(), getProperty() methods).
   * Required parameters:
   * jcr.lengthOfFile - file length in bytes, need to fill the content of jcr:data property;
   * jcr.lengthOfDcElementSetProperty - e.g 10 means that dc:title will be like "1234567890"; 
   * jcr.countOfDcElementSetProperties - must be less or equals 5, e.g 1 means "dc:title" property will be present only;
   *     
  */
  private Random       rand                          = new Random();

  private byte[]       contentOfFile                 = null;

  private byte[]       contentOfDcElementSetProperty = null;

  private int          countOfDcElementSetProperties = 0;

  private String[]     dcElementSetProperties        = { "dc:title", "dc:subject",
      "dc:description", "dc:publisher", "dc:resourceType" };

  private List<String> names                         = new ArrayList<String>();

  private Node         rootNode                      = null;

  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    contentOfFile = new byte[tc.getIntParam("jcr.lengthOfFile")];
    Arrays.fill(contentOfFile, (byte) 'F');
    contentOfDcElementSetProperty = new byte[tc.getIntParam("jcr.lengthOfDcElementSetProperty")];
    Arrays.fill(contentOfDcElementSetProperty, (byte) 'D');
    rootNode = context.getSession().getRootNode().addNode(context.generateUniqueName("rootNode"),
        "nt:unstructured");
    for (int i = 0; i < tc.getIntParam("japex.runIterations"); i++) {
      String nodeName = context.generateUniqueName("node");
      Node nodeToAdd = rootNode.addNode(nodeName, "nt:file");
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
      names.add(nodeName);
    }
    context.getSession().save();
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    int index = rand.nextInt(tc.getIntParam("japex.runIterations"));
    Node node = rootNode.getNode(names.get(index));
    Node contentNode = node.getNode("jcr:content");
    contentNode.getProperty("jcr:mimeType").getString();
    contentNode.getProperty("jcr:lastModified").getDate();
    for (int j = 0; j < countOfDcElementSetProperties; j++) {
      contentNode.getProperty(dcElementSetProperties[j]).getValues()[0].getString();
    }
    InputStream stream = contentNode.getProperty("jcr:data").getStream();
    int length = 0;
    int len;
    byte buf[] = new byte[10240];
    while ((len = stream.read(buf)) > 0)
      length += len;
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
