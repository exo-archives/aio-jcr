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

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.webdav.resource.CollectionResource;
import org.exoplatform.services.jcr.webdav.resource.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.resource.IllegalResourceTypeException;
import org.exoplatform.services.jcr.webdav.resource.Resource;
import org.exoplatform.services.jcr.webdav.xml.PropertyWriteUtil;
import org.exoplatform.services.jcr.webdav.xml.PropstatGroupedRepresentation;
import org.exoplatform.services.jcr.webdav.xml.WebDavNamespaceContext;
import org.exoplatform.services.rest.transformer.SerializableEntity;

/**
 * Created by The eXo Platform SARL .<br/>
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class PropFindResponseEntity implements SerializableEntity {
  
	protected XMLStreamWriter xmlStreamWriter;

	protected OutputStream outputStream;

	protected final WebDavNamespaceContext namespaceContext;

	protected final Resource rootResource;

	protected Set<QName> propertyNames;

	protected final int depth;

	protected final boolean propertyNamesOnly;

	public PropFindResponseEntity(int depth, Resource rootResource,
			Set<QName> propertyNames, boolean propertyNamesOnly) {
		this.rootResource = rootResource;
		this.namespaceContext = rootResource.getNamespaceContext();
		this.propertyNames = propertyNames;
		this.depth = depth;
		this.propertyNamesOnly = propertyNamesOnly;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.exoplatform.services.rest.transformer.SerializableEntity#writeObject(java.io.OutputStream)
	 */
	public void writeObject(OutputStream stream) throws IOException {
		this.outputStream = stream;
		try {
			this.xmlStreamWriter = XMLOutputFactory.newInstance()
					.createXMLStreamWriter(outputStream, Constants.DEFAULT_ENCODING);
			xmlStreamWriter.setNamespaceContext(namespaceContext);
			xmlStreamWriter.setDefaultNamespace("DAV:");

			xmlStreamWriter.writeStartDocument();
			xmlStreamWriter.writeStartElement("D", "multistatus", "DAV:");
			xmlStreamWriter.writeNamespace("D", "DAV:");
			
			xmlStreamWriter.writeAttribute("xmlns:b", "urn:uuid:c2f41010-65b3-11d1-a29f-00aa00c14882/");

			traverseResources(rootResource, 0);

			// D:multistatus
			xmlStreamWriter.writeEndElement();
			xmlStreamWriter.writeEndDocument();

			// rootNode.accept(this);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		} 
	}

	private void traverseResources(Resource resource, int counter)
			throws XMLStreamException, RepositoryException, 
			IllegalResourceTypeException, URISyntaxException {

		xmlStreamWriter.writeStartElement("DAV:", "response");

		xmlStreamWriter.writeStartElement("DAV:", "href");		
		xmlStreamWriter.writeCharacters(resource.getIdentifier().toASCIIString());
		xmlStreamWriter.writeEndElement();

		PropstatGroupedRepresentation propstat = 
			new PropstatGroupedRepresentation(resource, propertyNames, propertyNamesOnly);

		PropertyWriteUtil.writePropStats(xmlStreamWriter, propstat.getPropStats());

		xmlStreamWriter.writeEndElement();

    if(resource.isCollection()) {
      if (counter < depth) {
        CollectionResource collection = (CollectionResource)resource;
        for(Resource child : collection.getResources()) {
          traverseResources(child, counter + 1);
        }        
      }
    }

	}		

}
