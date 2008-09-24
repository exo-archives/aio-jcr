package org.exoplatform.services.jcr.core.nodetype;

import java.util.ArrayList;

/**
 * Simple store list class with NodeTypeValue beans. For JiBX binding process only.
 * 
 * @author <a href="mailto:peterit@rambler.ru">Petro Nedonosko</a>
 */
public class NodeTypeValuesList {

  private ArrayList nodeTypeValuesList = null;

  public NodeTypeValuesList() {
  }

  public ArrayList getNodeTypeValuesList() {
    return nodeTypeValuesList;
  }

  public void setNodeTypeValuesList(ArrayList nodeTypeValuesList) {
    this.nodeTypeValuesList = nodeTypeValuesList;
  }

}
