/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.ext.registry.transformer;

import org.exoplatform.services.rest.transformer.XMLEntityTransformer;
import org.exoplatform.services.rest.transformer.EntityTransformerFactory;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class XMLEntityTransformerFactory implements EntityTransformerFactory {

  public XMLEntityTransformer newTransformer() {
    return new XMLEntityTransformer();
  }

}
