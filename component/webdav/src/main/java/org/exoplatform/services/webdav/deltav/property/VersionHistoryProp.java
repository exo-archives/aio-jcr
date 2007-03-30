/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.deltav.property;

import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.property.dav.AbstractDAVProperty;
import org.exoplatform.services.webdav.common.resource.DavResource;
import org.exoplatform.services.webdav.common.response.DavStatus;
import org.exoplatform.services.webdav.deltav.resource.DeltaVResource;
import org.exoplatform.services.webdav.deltav.resource.VersionResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: VersionHistoryProp.java 12525 2007-02-02 12:26:47Z gavrikvetal $
 */

public class VersionHistoryProp extends AbstractDAVProperty {

  public VersionHistoryProp() {
    super(DavProperty.VERSIONHISTORY);
  }
  
  @Override
  protected boolean initialize(DavResource resource) throws RepositoryException {
    if (!(resource instanceof DeltaVResource) &&
        !(resource instanceof VersionResource)) {
      return false;
    }

    status = DavStatus.OK;
    return true;
  }
  
  @Override
  public void serialize(Document rootDoc, Element parentElement) {
    super.serialize(rootDoc, parentElement);
  }
  
}
