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

package org.exoplatform.services.webdav.common.document;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public abstract class AbstractXmlSerializable implements XmlSerializable {

  public abstract Element createElement(Document document);
  
  public abstract void serializeBody(Element element);
  
  public Element serialize(Document document) {    
    Element multistatusElement = createElement(document);
    document.appendChild(multistatusElement);    
    serializeBody(multistatusElement);    
    return multistatusElement;
  }

  public Element serialize(Element parentElement) {
    Document doc = parentElement.getOwnerDocument();     
    Element multistatusElement = createElement(doc);    
    parentElement.appendChild(multistatusElement);    
    serializeBody(multistatusElement);
    return multistatusElement;
  }  
  
}
