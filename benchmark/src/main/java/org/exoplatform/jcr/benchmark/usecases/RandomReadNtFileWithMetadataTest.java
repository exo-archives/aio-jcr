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
import javax.jcr.NodeIterator;
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

  private Random       rand                          = new Random();

  private byte[]       contentOfFile                 = null;

  private byte[]       contentOfDcElementSetProperty = null;

  private int          countOfDcElementSetProperties = 0;

  private String[]     dcElementSetProperties        = { "dc:title", "dc:subject",
      "dc:description", "dc:publisher", "dc:resourceType" };

  private List<String> names                         = new ArrayList<String>();

  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    // required params: jcr.lengthOfFile, jcr.lengthOfDcElementSetProperty,
    // jcr:countOfDcElementSetProperties
    contentOfFile = new byte[tc.getIntParam("jcr.lengthOfFile")];
    Arrays.fill(contentOfFile, (byte) 0);
    contentOfDcElementSetProperty = new byte[tc.getIntParam("jcr.lengthOfDcElementSetProperty")];
    Arrays.fill(contentOfDcElementSetProperty, (byte) 1);
    for (int i = 0; i < tc.getIntParam("japex.runIterations"); i++) {
      String nodeName = context.generateUniqueName("node");
      Node nodeToAdd = context.getSession().getRootNode().addNode(nodeName, "nt:file");
      Node contentNodeOfNodeToAdd = nodeToAdd.addNode("jcr:content", "nt:resource");
      contentNodeOfNodeToAdd.setProperty("jcr:data", new ByteArrayInputStream(contentOfFile));
      contentNodeOfNodeToAdd.setProperty("jcr:mimeType", "application/octet-stream");
      contentNodeOfNodeToAdd.setProperty("jcr:lastModified", Calendar.getInstance());
      contentNodeOfNodeToAdd.addMixin("dc:elementSet");
      contentNodeOfNodeToAdd.setProperty("dc:date", createMultiValue(context, Calendar.getInstance()));
      for (int j = 0; j < countOfDcElementSetProperties; j++) {
        contentNodeOfNodeToAdd.setProperty(dcElementSetProperties[j], createMultiValue(context,
            new String(contentOfDcElementSetProperty)));
      }
      context.getSession().save();
      names.add(nodeName);
    }
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    int index = rand.nextInt(tc.getIntParam("japex.runIterations"));
    Node node = context.getSession().getRootNode().getNode(names.get(index));
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
    // delete all the created nodes
    Node rootNode = context.getSession().getRootNode();
    if (rootNode.hasNodes()) {
      // clean test root
      for (NodeIterator children = rootNode.getNodes(); children.hasNext();) {
        Node node = children.nextNode();
        if (!node.getPath().startsWith("/jcr:system")) {
          node.remove();
        }
      }
    }
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
