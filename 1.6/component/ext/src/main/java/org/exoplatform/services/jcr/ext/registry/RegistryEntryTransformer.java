/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.ext.registry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import javax.xml.transform.TransformerException;

import org.exoplatform.services.rest.transformer.EntityTransformer;
import org.exoplatform.services.rest.transformer.DummyEntityTransformer;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class RegistryEntryTransformer implements EntityTransformer {

  public RegistryEntry readFrom(InputStream entityDataStream) throws IOException {
    try {
      return RegistryEntry.parse(entityDataStream);
    } catch (ParserConfigurationException pce) {
      throw new IOException("Can't read from input stream " + pce);
    } catch (SAXException saxe) {
      throw new IOException("Can't read from input stream " + saxe);
    }
  }

  public void writeTo(Object entity, OutputStream entityDataStream) throws IOException {
    RegistryEntry regEntry = (RegistryEntry)entity;
    DummyEntityTransformer dummyTransformer = new DummyEntityTransformer();
    try {
      dummyTransformer.writeTo(regEntry.getAsInputStream(), entityDataStream);
    } catch (TransformerException tre) {
      throw new IOException("Can't get RegistryEntry as stream " + tre);
    }
  }

}
