/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.transformer;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Write given Object OututStream using JAXB transformation. Java Architecture
 * for XML Binding (JAXB) allows create and edit XML using familiar Java
 * objects.<br/> JAXB is particularly useful when the specification is complex
 * and changing. This class can marshal Java Object to XML as stream.<br/>
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class JAXBOutputTransformer extends OutputEntityTransformer {

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.rest.transformer.OutputEntityTransformer#writeTo(java.lang.Object,
   *      java.io.OutputStream)
   */
  @Override
  public final void writeTo(Object entity, OutputStream entityDataStream) throws IOException {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(entity.getClass());
      jaxbContext.createMarshaller().marshal(entity, entityDataStream);
    } catch (JAXBException jaxbe) {
      throw new IOException("Can't transform Object to OutputStream: " + jaxbe);
    }
  }

}
