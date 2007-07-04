/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.transformer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;


/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class JAXBEntityTransformer implements EntityTransformer {

  private JAXBContext jaxbContext;

  public JAXBEntityTransformer(String contextPath) throws JAXBException{
    jaxbContext = JAXBContext.newInstance(contextPath);
  }
  
  public JAXBEntityTransformer(Class... classes) throws JAXBException{
    jaxbContext = JAXBContext.newInstance(classes);
  }

  public Object readFrom(InputStream entityDataStream) throws IOException {
    try {
      return jaxbContext.createUnmarshaller().unmarshal(entityDataStream);
    } catch(JAXBException jaxbe) {
      throw new IOException("Can't transform InputStream to Object: " + jaxbe);
    }
  }

  public void writeTo(Object entity, OutputStream entityDataStream) throws IOException {
    try{
      jaxbContext.createMarshaller().marshal(entity, entityDataStream);
    } catch(JAXBException jaxbe) {
      throw new IOException("Can't transform Object to OutputStream: " + jaxbe);
    }
  }

}
