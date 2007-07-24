/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.transformer;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Create Object from given InputStream using JAXB transformation. Java
 * Architecture for XML Binding (JAXB) allows create and edit XML using familiar
 * Java objects.<br/> JAXB is particularly useful when the specification is
 * complex and changing. This class can unmarshal XML given a stream into Java
 * object.<br/>
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class JAXBInputTransformer extends InputEntityTransformer {

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.rest.transformer.InputEntityTransformer#readFrom(java.io.InputStream)
   */
  @Override
  public final Object readFrom(InputStream entityDataStream) throws IOException {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(entityType);
      return jaxbContext.createUnmarshaller().unmarshal(entityDataStream);
    } catch (JAXBException jaxbe) {
      throw new IOException("Can't transform InputStream to Object: " + jaxbe);
    }
  }

}
