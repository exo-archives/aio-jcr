/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */

package org.exoplatform.services.jcr.api.reading;


import java.util.Calendar;

import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.util.TraversingItemVisitor;

import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.jcr.impl.core.value.StringValue;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: TestItemVisitor.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class TestItemVisitor extends JcrAPIBaseTest {

  public void testItemVisiting() throws RepositoryException {

    Node root = session.getRootNode();
    Node file = root.addNode("childNode", "nt:folder").addNode("childNode2", "nt:file");
    Node contentNode = file.addNode("jcr:content", "nt:resource");
    contentNode.setProperty("jcr:data", session.getValueFactory().createValue("this is the content", PropertyType.BINARY));
    contentNode.setProperty("jcr:mimeType", session.getValueFactory().createValue("text/html"));
    contentNode.setProperty("jcr:lastModified", session.getValueFactory().createValue(Calendar.getInstance()));

    session.save();

    ItemVisitor visitor = new MockVisitor();
    contentNode.accept(visitor);
    contentNode.getProperty("jcr:data").accept(visitor);

    visitor = new MockVisitor2();
    root.getNode("childNode").accept(visitor);
    assertEquals(((MockVisitor2)visitor).getI(),((MockVisitor2)visitor).getJ());
    assertEquals(3, ((MockVisitor2)visitor).getI());
    log.debug("VISITOR -- "+((MockVisitor2)visitor).isReached());
    assertTrue(((MockVisitor2)visitor).isReached());
    
    root.getNode("childNode").remove();
    session.save();
  }

  public class MockVisitor implements ItemVisitor {

    public void visit(Property property) throws RepositoryException {
      assertEquals(property.getName(), "jcr:data");
    }

    public void visit(Node node) throws RepositoryException {
      assertEquals(node.getName(), "jcr:content");
    }

  }


  public class MockVisitor2 extends TraversingItemVisitor {

    private boolean reached = false;
    private int i = 0;
    private int j = 0;

    protected void entering(Property property, int level) throws RepositoryException {
      if("jcr:data".equals(property.getName()))
        reached = true;
    }

    protected void entering(Node node, int level) throws RepositoryException {
      i++;
      assertTrue(isInList(node.getName()));
    }

    protected void leaving(Property property, int level) throws RepositoryException {
    }

    protected void leaving(Node node, int level) throws RepositoryException {
      j++;
    }

    private boolean isInList(String name){
      if("childNode".equals(name) || "childNode2".equals(name) || "jcr:content".equals(name) ||
          "jcr:data".equals(name) || "jcr:mimeType".equals(name)){
        return true;
      }
      return false;
    }

    public int getI() {
      return i;
    }

    public int getJ() {
      return j;
    }

    public boolean isReached() {
      return reached;
    }
  }

}
