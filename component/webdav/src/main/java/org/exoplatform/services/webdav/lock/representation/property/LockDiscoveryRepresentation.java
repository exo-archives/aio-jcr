/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.lock.representation.property;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.lock.Lock;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.lock.ExtendedLock;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.representation.HrefRepresentation;
import org.exoplatform.services.webdav.common.representation.property.WebDavPropertyRepresentation;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class LockDiscoveryRepresentation extends WebDavPropertyRepresentation {
  
  private static Log log = ExoLogger.getLogger("jcr.LockDiscoveryRepresentation");
  
  public static final String TAGNAME = "lockdiscovery";
  
  public static final String XML_ACTIVELOCK = "activelock";
  
  public static final String XML_LOCKTYPE = "locktype";
  
  public static final String XML_WRITE = "write";
  
  public static final String XML_LOCKSCOPE = "lockscope";
  
  public static final String XML_EXCLUSIVE = "exclusive";
  
  public static final String XML_DEPTH = "depth";
  
  public static final String XML_OWNER = "owner";
  
  public static final String XML_TIMEOUT = "timeout";
  
  public static final String XML_LOCKTOKEN = "locktoken";
  
  private boolean locked = false;
  
  private String owner = "";
  
  private long timeOut;
  
  private String lockToken = "";
  
  @Override
  public String getTagName() {
    return TAGNAME;
  }
  
  public String getLockToken() {
    return lockToken;
  }
  
  public void setLocked(boolean locked) {
    this.locked = locked;
  }
  
  public void setLockOwner(String owner) {
    this.owner = owner;
  }
  
  public void setLockToken(String lockToken) {
    this.lockToken = lockToken;
  }

  public void read(Node node) {
    try {
      if (node.isLocked()) {
        locked = true;
        owner = node.getLock().getLockOwner();
        
        Lock lock = node.getLock();
        ExtendedLock lockExt = (ExtendedLock)lock;
        
        timeOut = lockExt.getTimeToDeath();
      }
      
      status = WebDavStatus.OK;      
    } catch (RepositoryException exc) {
    }
  }
  
  @Override
  protected void writeContent(XMLStreamWriter xmlWriter) throws XMLStreamException {    
    if (!locked) {
      return;
    }
    
    /*
     * locktype
     * lockscope
     * depth
     * owner
     * timeout
     * locktoken 
     */
    
    xmlWriter.writeStartElement(getNameSpace(), XML_ACTIVELOCK);
    
    xmlWriter.writeStartElement(getNameSpace(), XML_LOCKTYPE);
      xmlWriter.writeStartElement(getNameSpace(), XML_WRITE);
      xmlWriter.writeEndElement();
    xmlWriter.writeEndElement();
    
    xmlWriter.writeStartElement(getNameSpace(), XML_LOCKSCOPE);
      xmlWriter.writeStartElement(getNameSpace(), XML_EXCLUSIVE);
      xmlWriter.writeEndElement();
    xmlWriter.writeEndElement();
    
    xmlWriter.writeStartElement(getNameSpace(), XML_DEPTH);
      xmlWriter.writeCharacters("Infinity");
    xmlWriter.writeEndElement();
    
    xmlWriter.writeStartElement(getNameSpace(), XML_OWNER);
      xmlWriter.writeCharacters(owner);
    xmlWriter.writeEndElement();
    
    xmlWriter.writeStartElement(getNameSpace(), XML_TIMEOUT);
      if (timeOut < 0) {
        xmlWriter.writeCharacters("Infinity");
      } else {
        xmlWriter.writeCharacters("" + timeOut);
      }
    xmlWriter.writeEndElement();

    if (!"".equals(lockToken)) {
      xmlWriter.writeStartElement(getNameSpace(), XML_LOCKTOKEN);        
      HrefRepresentation hrefRepresentation = new HrefRepresentation(lockToken);
      hrefRepresentation.write(xmlWriter);                
      xmlWriter.writeEndElement();
    }
    
  xmlWriter.writeEndElement();
        
  }

}
