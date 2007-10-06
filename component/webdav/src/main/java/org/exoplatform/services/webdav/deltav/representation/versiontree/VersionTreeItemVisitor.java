/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.representation.versiontree;

import java.util.ArrayList;
import java.util.HashMap;

import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.representation.read.PropListItemVisitor;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class VersionTreeItemVisitor extends PropListItemVisitor {
  
  private static Log log = ExoLogger.getLogger("jcr.VersionTreeItemVisitor");
  
  private int currentLevel;
  
  public VersionTreeItemVisitor(WebDavService webDavService, boolean breadthFirst, int maxLevel, String rootHref, HashMap<String, ArrayList<String>> properties, XMLStreamWriter xmlWriter) {
    super(webDavService, breadthFirst, maxLevel, rootHref, properties, xmlWriter);
  }

  public void visit(Property property) throws RepositoryException {
  }

  public void visit(Node node) throws RepositoryException {
    // depth-first traversal
    entering(node, currentLevel);
    if (maxLevel == -1 || currentLevel < maxLevel) {
        currentLevel++;
        PropertyIterator propIter = node.getProperties();
        while (propIter.hasNext()) {
          propIter.nextProperty().accept(this);
        }
        NodeIterator nodeIter = node.getNodes();
        while (nodeIter.hasNext()) {
          nodeIter.nextNode().accept(this);
        }
        currentLevel--;
    }
    leaving(node, currentLevel);
  }

}
