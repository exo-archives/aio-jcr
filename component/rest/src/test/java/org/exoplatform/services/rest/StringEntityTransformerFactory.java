/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import org.exoplatform.services.rest.transformer.EntityTransformerFactory;
import org.exoplatform.services.rest.transformer.EntityTransformer;
import org.exoplatform.services.rest.transformer.StringEntityTransformer;
/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class StringEntityTransformerFactory implements EntityTransformerFactory {
  
  public EntityTransformer newTransformer() {
    return new StringEntityTransformer();
  }

}
