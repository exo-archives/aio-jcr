/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

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
