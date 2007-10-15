/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.request.documents;

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.common.property.factory.PropertyCollector;
import org.exoplatform.services.webdav.common.property.factory.PropertyDefine;
import org.exoplatform.services.webdav.common.property.factory.PropertyFactory;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.deltav.resource.VersionResource;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: CommonPropDoc.java 12635 2007-02-07 12:57:47Z gavrikvetal $
 */

public class CommonPropDocument implements RequestDocument {
  
  protected ArrayList<PropertyDefine> defines = new ArrayList<PropertyDefine>();
  
  protected boolean isNeedSearchProperties = true;
  
  private PropertyFactory propertyFactory;
  
  public void initFactory(PropertyFactory propertyFactory) {
    this.propertyFactory = propertyFactory;
  }
  
  public boolean init(Document requestDocument, PropertyFactory propertyFactory) {
    this.propertyFactory = propertyFactory;
    return false;
  }
  
  public ArrayList<PropertyDefine> getDefines() {
    return defines;
  }
  
  public boolean isNeedSearchProperties() {
    return isNeedSearchProperties;
  }

  public ArrayList<PropertyDefine> searchPropertiesForResource(WebDavResource resource) throws RepositoryException {
    ArrayList<PropertyDefine> curDefines = new ArrayList<PropertyDefine>();
    
    curDefines.addAll(loadPreSetForResource(resource));
    
    PropertyCollector collector = new PropertyCollector(propertyFactory);
    curDefines.addAll(collector.searchPropertiesForResource(resource));
    
    return curDefines;
  }
  
  public ArrayList<PropertyDefine> loadPreSetForResource(WebDavResource resource) throws RepositoryException {
    String nodeTypeName = "*";

    WebDavResource calculatedResource = resource;
    if (resource instanceof VersionResource) {
      calculatedResource = ((VersionResource)resource).getOwnResource();
    }
    
    if (calculatedResource instanceof AbstractNodeResource) {
      Node node = ((AbstractNodeResource)calculatedResource).getNode();      
      nodeTypeName = node.getPrimaryNodeType().getName();      
    }
    
    return propertyFactory.getDefines(nodeTypeName); 
  }

}
