/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.jcr.benchmark.usecases;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Random;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;

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
   * This test will read randomly one of 1 million nodes of type nt:file that
   * has been created beforehand
   * /download/node0..8/node0..49/node0..49/0..7-0..49-0..49-0..49.txt. Digits
   * are genereted randomly using levelXNodesCount parameters.
   */
  private Random rand   = new Random();

  private int    level1 = 0;

  private int    level2 = 0;

  private int    level3 = 0;

  private int    level4 = 0;

  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    level1 = tc.getIntParam("jcr.level1NodesCount");
    level2 = tc.getIntParam("jcr.level2NodesCount");
    level3 = tc.getIntParam("jcr.level3NodesCount");
    level4 = tc.getIntParam("jcr.level4NodesCount");
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    int level1Index = rand.nextInt(level1);
    int level2Index = rand.nextInt(level2);
    int level3Index = rand.nextInt(level3);
    int level4Index = rand.nextInt(level4);
    String path = "download/node" + level1Index + "/node" + level2Index + "/node" + level3Index
        + "/" + level1Index + "-" + level2Index + "-" + level3Index + "-" + level4Index + ".txt";
    Node node = context.getSession().getRootNode().getNode(path);
    Node contentNode = node.getNode("jcr:content");
    try {
      contentNode.getProperty("jcr:mimeType").getString();
      contentNode.getProperty("jcr:lastModified").getDate();
      contentNode.getProperty("dc:title").getValues()[0].getString();
      contentNode.getProperty("dc:subject").getValues()[0].getString();
      contentNode.getProperty("dc:creator").getValues()[0].getString();
      InputStream stream = contentNode.getProperty("jcr:data").getStream();
      int length = 0;
      int len;
      byte buf[] = new byte[1024];
      while ((len = stream.read(buf)) > 0)
        length += len;
    } catch (PathNotFoundException e) {
      System.out.println("[error] can not find property for node : " + contentNode.getPath());
      e.printStackTrace();
    }
  }

  @Override
  public void doFinish(TestCase tc, JCRTestContext context) throws Exception {
  }

}
