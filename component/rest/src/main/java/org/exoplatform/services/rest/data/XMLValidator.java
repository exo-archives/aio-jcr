/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.data;

import javax.xml.validation.Validator;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import org.exoplatform.container.xml.*;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class XMLValidator {
  private Map < String, Schema > schemas;

  public XMLValidator(InitParams params) throws SAXException {
    this.schemas = new HashMap < String, Schema >();
    SchemaFactory schfactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
    Iterator < ValueParam > i = params.getValueParamIterator();
    while (i.hasNext()) {
      ValueParam v = i.next();
      File schemaLocation = new File(v.getValue());
      schemas.put(v.getName(), schfactory.newSchema(schemaLocation));
    }
  }

  public String validate(InputStream in) {
    Source source = new StreamSource(in);
    Set < String > keys = schemas.keySet();
    String key = null;
    for (String k : keys) {
      Schema schema = schemas.get(k);
      Validator validator = schema.newValidator();
      try {
        validator.validate(source);
        key = k;
        break;
      } catch (SAXException saxe) {
        return null;
      } catch (IOException ioe) {
        return null;
      }
    }
    return key;
  }

  public String validate(Document xmldoc) throws TransformerException {
    ByteArrayOutputStream ou = new ByteArrayOutputStream();
    TransformerFactory.newInstance().newTransformer().transform(new DOMSource(xmldoc),
        new StreamResult(ou));
    return this.validate(new ByteArrayInputStream(ou.toByteArray()));
  }
}
