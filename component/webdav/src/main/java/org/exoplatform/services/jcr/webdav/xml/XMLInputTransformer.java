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

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.exoplatform.services.jcr.webdav.resource.HierarchicalProperty;
import org.exoplatform.services.rest.transformer.InputEntityTransformer;

/**
 * Created by The eXo Platform SARL .<br/> 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class XMLInputTransformer extends InputEntityTransformer{
  
	private Stack <HierarchicalProperty> curProperty = new Stack <HierarchicalProperty>();
	private HierarchicalProperty rootProperty;

	@Override
	public Object readFrom(InputStream stream) throws IOException {
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLEventReader reader = factory.createXMLEventReader(stream);
			while (reader.hasNext()) {
			  XMLEvent event = reader.nextEvent();
			  switch (event.getEventType()) {
			  
			  case XMLEvent.START_ELEMENT:
			    StartElement element = event.asStartElement();
			    QName name = element.getName();			    
			    HierarchicalProperty prop = new HierarchicalProperty(name);
			    if(!curProperty.empty())
			    	curProperty.peek().addChild(prop);
			    else
			    	rootProperty = prop;
			    curProperty.push(prop);
			    break;
			  case XMLEvent.END_ELEMENT:
			  	curProperty.pop();
			    break;
			  case XMLEvent.CHARACTERS:
			    String chars = event.asCharacters().getData();
			    curProperty.peek().setValue(chars);
			    break;
			  default:
			    break;
			  }
			}
			
			return rootProperty;
		} catch (FactoryConfigurationError e) {
			throw new IOException(e.getMessage());
		} catch (XMLStreamException e) {
			return null;
		}
	}

}
