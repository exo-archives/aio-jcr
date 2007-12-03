/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

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
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class JcrPropertyData extends AbstractResourceData {
  
  private static Log log = ExoLogger.getLogger("jcr.JcrPropertyData");
  
  public JcrPropertyData(Property property) throws RepositoryException, IOException {    
    name = property.getName();
    iscollection = false;
    
    lastModified = Calendar.getInstance().getTime().toString();
    contentType = "application/octet-stream";
    
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
