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

package org.exoplatform.services.jcr.webdav.xml;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class WebDavNamespaceContext implements NamespaceContext {
  
  /*
   * Key: NameSpace
   * Value: Prefix
   * 
   */
  private HashMap<String, String> prefixes = new HashMap<String, String>();    

  private HashMap<String, String> namespaces = new HashMap<String, String>(); 
  
  private final NamespaceRegistry namespaceRegistry;

  public WebDavNamespaceContext(Session session) throws RepositoryException {
  	this.namespaceRegistry = session.getWorkspace().getNamespaceRegistry(); 
    prefixes.put("DAV:", "D");
    namespaces.put("D", "DAV:");
  }
  
	public QName createQName(String strName) {
		String[] parts = strName.split(":");
		if(parts.length > 1) 
			return new QName(getNamespaceURI(parts[0]), parts[1], parts[0]);
		else
			return new QName(parts[0]);
	}
	
	public static String createName(QName qName) {
		return qName.getPrefix() + ":" + qName.getLocalPart();
	}

  /* (non-Javadoc)
   * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
   */
  public String getNamespaceURI(String prefix) {
  	String uri = null;
		try {
			uri = namespaceRegistry.getURI(prefix);
		} catch (NamespaceException e) {
			uri = namespaces.get(prefix);
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
    return uri;
  }

  /* (non-Javadoc)
   * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
   */
  public String getPrefix(String namespaceURI) {
  	String prefix = null;
		try {
			prefix = namespaceRegistry.getPrefix(namespaceURI);
		} catch (NamespaceException e) {
			prefix = prefixes.get(namespaceURI);
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
    return prefix;

  }

  /* (non-Javadoc)
   * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
   */
  public Iterator<String> getPrefixes(String namespaceURI) {
    List<String> list = new ArrayList<String>();
    list.add(getPrefix(namespaceURI));
    return list.iterator();
  }  
  
}
