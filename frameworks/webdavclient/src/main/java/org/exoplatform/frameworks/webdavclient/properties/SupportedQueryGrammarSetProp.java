/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.properties;

import java.util.ArrayList;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.XmlUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class SupportedQueryGrammarSetProp extends CommonProp {

  private boolean basicSearchEnable = false;
  private ArrayList<String> grammars = new ArrayList<String>();
  
  public SupportedQueryGrammarSetProp() {
    this.propertyName = Const.DavProp.SUPPORTEDQUERYGRAMMARSET;
  }
  
  @Override
  public boolean init(Node node) {
    if (status != Const.HttpStatus.OK) {
      return false;
    }

    NodeList nodes = node.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node childNode = nodes.item(i);
      
      String childNodeName = childNode.getLocalName();
      if (childNodeName == null) {
        continue;
      }
      
      if (Const.DavProp.SUPPORTEDQUERYGRAMMAR.equalsIgnoreCase(childNodeName) &&
          Const.Dav.NAMESPACE.equals(childNode.getNamespaceURI())) {        
        Node grammarNode = XmlUtil.getChildNode(childNode, Const.DavProp.GRAMMAR);
        
        NodeList grammarNodes = grammarNode.getChildNodes();
        for (int gi = 0; gi < grammarNodes.getLength(); gi++) {
          Node queryGrammarNode = grammarNodes.item(gi);
          
          String queryName = queryGrammarNode.getLocalName();
          if (queryName == null) {
            continue;
          }
                    
          if (Const.DavProp.BASICSEARCH.equalsIgnoreCase(queryName) &&
              Const.Dav.NAMESPACE.equals(queryGrammarNode.getNamespaceURI())) {           
            basicSearchEnable = true;
            continue;
          }
          
          grammars.add(queryName);
        }
        
      }
      
    }
    
    return true;
  }
  
  public boolean isBasicSearchEnabled() {
    return basicSearchEnable;
  }
  
  public ArrayList<String> getGrammars() {
    return grammars;
  }
  
}
