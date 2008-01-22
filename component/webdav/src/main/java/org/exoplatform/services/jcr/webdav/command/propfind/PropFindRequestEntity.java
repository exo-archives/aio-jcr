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

package org.exoplatform.services.jcr.webdav.command.propfind;

import javax.xml.namespace.QName;

import org.exoplatform.services.jcr.webdav.resource.HierarchicalProperty;

/**
 * Created by The eXo Platform SARL .<br/> 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class PropFindRequestEntity {
	
	protected HierarchicalProperty input;

	public PropFindRequestEntity(HierarchicalProperty input) {
		this.input = input;
	}

	public String getType() {
	  
	  if (input == null) {
	    return "allprop";
	  }
	  
		QName name = input.getChild(0).getName();
		if(name.getNamespaceURI().equals("DAV:"))
		  return name.getLocalPart();
		else
			return null;
	}
	
//	public List<QName> getPropNames() {
//		List<QName> props = new ArrayList<QName>();
//		if(getType() != null && getType().equals("prop")) {
//			for(XMLProperty prop : input.getChild(0).getChildren()) {
//				props.add(prop.getName());
//			}
//		}
//		return props;
//	}

}
