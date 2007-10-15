/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.resource.resourcedata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class JcrPropertyData extends AbstractResourceData {
  
  private static Log log = ExoLogger.getLogger("jcr.JcrPropertyData");
  
  public JcrPropertyData(Property property) throws RepositoryException, IOException {    
    name = property.getName();
    iscollection = false;
    
    lastModified = Calendar.getInstance().getTime().toString();
    contentType = "application/occetstream";
    
    if (property.getDefinition().isMultiple()) {
      fillMultipleData(property);
    } else {
      fillSingleData(property);
    }    
  }
  
  protected void fillSingleData(Property property) throws RepositoryException, IOException {
    if (isStringValue(property.getValue())) {
      resourceInputStream = new ByteArrayInputStream(property.getValue().getString().getBytes());
      resourceLenght = resourceInputStream.available();      
    } else {
      resourceLenght = property.getLength();
      resourceInputStream = property.getValue().getStream();
    }    
  }
  
  protected void fillMultipleData(Property property) throws RepositoryException, IOException {
    Value []values = property.getValues();
    
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    
    for (int i = 0; i < values.length; i++) {
      Value propertyValue = values[i];
      
      if (isStringValue(propertyValue)) {
        outStream.write(propertyValue.getString().getBytes());
        if (i != (values.length - 1)) {
          outStream.write("\r\n".getBytes());
        }
      } else {
        log.info("TYPE: " + propertyValue.getType());
      }
      
    }
    
    resourceInputStream = new ByteArrayInputStream(outStream.toByteArray());
    resourceLenght = resourceInputStream.available();
  }
  
  protected boolean isStringValue(Value value) {
    if (value.getType() == PropertyType.STRING ||
        value.getType() == PropertyType.NAME) {
      
      return true;
    }
    return false;
  }

}
