/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.representation.versiontree;

import javax.jcr.Item;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.common.representation.WebDavNameSpaceContext;
import org.exoplatform.services.webdav.common.representation.XmlResponseRepresentation;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class VersionTreeResponseRepresentation extends XmlResponseRepresentation {
  
  private static Log log = ExoLogger.getLogger("jcr.VersionTreeResponseRepresentation");
  
  public VersionTreeResponseRepresentation(Item node) throws RepositoryException {
    super(new WebDavNameSpaceContext((ManageableRepository)node.getSession().getRepository()));
    log.info("construct......");
  }

  @Override
  protected void write(XMLStreamWriter writer) throws XMLStreamException, RepositoryException {
  }

}
