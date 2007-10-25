/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.search.representation.property;

import javax.jcr.Node;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.webdav.common.representation.property.WebDavPropertyRepresentation;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class SupportedQueryGrammarSetRepresentation extends WebDavPropertyRepresentation {
  
//  private static Log log = ExoLogger.getLogger("jcr.SupportedQueryGrammarSetRepresentation");
  
  public static final String TAGNAME = "supported-query-grammar-set";
  
  @Override
  public String getTagName() {
    return TAGNAME;
  }

  public void read(Node node) {    
//    try {
//      String []supportedLanguages = node.getSession().getWorkspace().getQueryManager().getSupportedQueryLanguages();
//      
//      log.info("FOR WORKSPACE: " + node.getSession().getWorkspace().getName());
//      
//      for (int i = 0; i < supportedLanguages.length; i++) {
//        log.info("SupportedLanguage: " + supportedLanguages[i]);
//      }
//      
//      status = WebDavStatus.OK;
//    } catch (RepositoryException rexc) {
//      log.info("Unhandled exception. " + rexc.getMessage(), rexc);
//    }    
  }

  @Override
  protected void writeContent(XMLStreamWriter xmlWriter) throws XMLStreamException {
  }

}
