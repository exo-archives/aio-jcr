/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.search.basicsearch;

import java.util.Vector;

import org.exoplatform.services.webdav.search.Search;
import org.exoplatform.services.webdav.search.SearchConst;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: BasicSearch.java 12525 2007-02-02 12:26:47Z gavrikvetal $
 */

public class BasicSearch implements Search {

  //private static Log log = ExoLogger.getLogger("jcr.BasicSearch");

  private String query;

  public boolean init(Node node) {
    return false;
    
//    try {
//      
//      Node selectNode = DavUtil.getChildNode(node, DavProperty.SELECT);
//      log.info("SELECT NODE: " + selectNode);
//      
//      Node allPropNode = DavUtil.getChildNode(selectNode, DavProperty.ALLPROP);
//      if (allPropNode != null) {
//        log.info("REQUIRED ALL PROPERTIES..........");
//      } else {
//        Node propNode = DavUtil.getChildNode(selectNode, DavProperty.PROP);
//                
//        NodeList propertyNodes = propNode.getChildNodes();
//        for (int i = 0; i < propertyNodes.getLength(); i++) {
//          Node curNode = propertyNodes.item(i);
//          
//          log.info("----------------------------");
//          log.info("NAMESPACE: " + curNode.getNamespaceURI());
//          log.info("LOCALNAME: " + curNode.getLocalName());
//        }
//        
//      }
//      
//    } catch (Exception exc) {
//      log.info("Unhandled exception. " + exc.getMessage(), exc);
//    }
//    
//    
//	  Vector<String> nodeTypes = new Vector<String>();	  
//	  
//    nodeTypes.add("nt:base");
	  
    //nodeTypes.add("nt:folder");
	  //nodeTypes.add("nt:file");        

//    try {
//       String searchRequestStr = getSerializedSearchRequest(node);
//       DASLTag parser = new DASLTag(new StringReader(searchRequestStr));
//       DASLDocument doc = parser.DASLDocument();
//       DASLConvertor dc = new DASLConvertor(nodeTypes);
//       dc.setFromElements(nodeTypes);
//       SaxValidator sv = new SaxValidator(searchRequestStr);
//       sv.validate();
//       dc.setDASLPrefix(sv.getDASLPrefix());
//       query = dc.convertQuery(doc);
//       return true;
//    } catch (Throwable exc) {
//      log.info("Unhandled exception. " + exc.getMessage(), exc);
//      return false;
//    }

  }

//  private String getSerializedSearchRequest(org.w3c.dom.Node node) throws Exception {
//    ByteArrayOutputStream out = new ByteArrayOutputStream();
//
//    Document document = node.getOwnerDocument();
//
//    OutputFormat format = new OutputFormat("xml", "UTF-8", true);
//    XMLSerializer serializer = new XMLSerializer(out, format);
//    serializer.setNamespaces(true);
//    serializer.asDOMSerializer().serialize(document);
//
//    return new String(out.toByteArray());
//  }

  public String getQueryLanguage() {
    return SearchConst.SearchType.SQL;
  }

  public String getQuery() {
    return query;
  }

  public Vector<String> getRequiredPropertyList() {
    return new Vector<String>();
  }

}
