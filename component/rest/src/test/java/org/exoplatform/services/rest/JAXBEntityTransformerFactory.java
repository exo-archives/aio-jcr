/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import javax.xml.bind.JAXBException;

import org.exoplatform.services.rest.transformer.JAXBEntityTransformer;
import org.exoplatform.services.rest.transformer.EntityTransformerFactory;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class JAXBEntityTransformerFactory implements EntityTransformerFactory {

  private static final String packageName = "org.exoplatform.services.rest.generated";
  
  public JAXBEntityTransformer newTransformer() {
    try {
      return new JAXBEntityTransformer(packageName);
    } catch(JAXBException jaxbe) {
      return null;
    }
  }

}
