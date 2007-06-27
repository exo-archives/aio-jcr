/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.jcr.benchmark.jcrapi;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import javax.jcr.Node;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.exoplatform.jcr.benchmark.JCRTestBase;
import org.exoplatform.jcr.benchmark.JCRTestContext;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */
public class ReadFileWithMetadataTest extends JCRTestBase {

  private static boolean initialized = false;
  private static List <String> names = new ArrayList<String>(); 
  
  private Random rand = new Random();
  private int nodesNum = 100;
  private Node root;
  private ValueFactory valueFactory;

  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    
    root = context.getSession().getRootNode();
    valueFactory = context.getSession().getValueFactory();
    int fileLength = 4096;
    
    if(initialized)
      return;
    initialized = true;
    
    if(tc.hasParam("jcr.nodes"))
      nodesNum = tc.getIntParam("jcr.nodes");
    // TODO
    //int levelsNum = tc.getIntParam("jcr.levels");
    
    if(tc.hasParam("jcr.fileLength"))
      fileLength = tc.getIntParam("jcr.fileLength");
    
    byte[] content = new byte[fileLength];
    Arrays.fill(content, (byte)0);
    
    for(int i=0; i<nodesNum; i++) {
      Node newNode = root.addNode(context.generateUniqueName("node"), "nt:file");
      
      Node contentNode = newNode.addNode("jcr:content", "nt:resource");
      
      contentNode.setProperty("jcr:data", new ByteArrayInputStream(content));
      
      contentNode.setProperty("jcr:mimeType", "application/octet-stream");
      contentNode.setProperty("jcr:lastModified", Calendar.getInstance());

      contentNode.addMixin("dc:elementSet");
      contentNode.setProperty("dc:title", createMultiValue("0123456789"));
      contentNode.setProperty("dc:subject", createMultiValue("0123456789"));
      contentNode.setProperty("dc:description", createMultiValue("0123456789"));
      contentNode.setProperty("dc:publisher", createMultiValue("0123456789"));
      contentNode.setProperty("dc:date", createMultiValue(Calendar.getInstance()));
      contentNode.setProperty("dc:resourceType", createMultiValue("0123456789"));

      names.add(newNode.getName());
    }
    root.save();

  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {

    int index = rand.nextInt(nodesNum);

    try {
      Node node = root.getNode(names.get(index));
      Node contentNode = node.getNode("jcr:content");
      contentNode.getProperty("jcr:mimeType").getString();

      contentNode.getProperty("jcr:lastModified").getDate();

      contentNode.getProperty("dc:title").getValues()[0].getString();
      contentNode.getProperty("dc:subject").getValues()[0].getString();
      contentNode.getProperty("dc:description").getValues()[0].getString();
      contentNode.getProperty("dc:publisher").getValues()[0].getString();
      contentNode.getProperty("dc:date").getValues()[0].getString();
      contentNode.getProperty("dc:resourceType").getValues()[0].getString();
      
      InputStream stream = contentNode.getProperty("jcr:data").getStream();
      int length = 0;
      int len;
      byte buf[] = new  byte[4096];
      while ((len = stream.read(buf)) > 0)
        length += len;

      
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }
  
  
  
  private Value[] createMultiValue(String sValue) {
    Value[] values = new Value[1];
    values[0] = valueFactory.createValue(sValue);
    
    return values;
  } 
  
  private Value[] createMultiValue(Calendar date) {
    Value[] values = new Value[1];
    values[0] = valueFactory.createValue(date);
    
    return values;
  }

}
