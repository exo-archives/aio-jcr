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
package org.exoplatform.services.jcr.ext.metadata;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Map.Entry;

import javax.jcr.PathNotFoundException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.ValueFormatException;

import org.apache.commons.chain.Context;
import org.apache.commons.logging.Log;
import org.exoplatform.commons.utils.QName;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.command.action.Action;
import org.exoplatform.services.document.DocumentReaderService;
import org.exoplatform.services.document.HandlerNotFoundException;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionDatas;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.JCRName;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.PropertyImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author Gennady Azarenkov
 * @version $Id$
 */

public class AddMetadataAction implements Action {

  private static Log log = ExoLogger.getLogger("ext.AddMetadataAction");

  public boolean execute(Context ctx) throws Exception {

    PropertyImpl property = (PropertyImpl) ctx.get("currentItem");
    NodeImpl parent = (NodeImpl) property.getParent();
    if (!parent.isNodeType("nt:resource"))
      throw new Exception("incoming node is not nt:resource type");

    InputStream data = null;
    String mimeType;

    try {
      if (property.getInternalName().equals(Constants.JCR_DATA)) {
        data = ((TransientPropertyData) property.getData()).getValues().get(0).getAsStream();
        try {
          mimeType = parent.getProperty("jcr:mimeType").getString();
        } catch (PathNotFoundException e) {
          return false;
        }
      } else if (property.getInternalName().equals(Constants.JCR_MIMETYPE)) {
        mimeType = property.getString();
        try {
          PropertyImpl propertyImpl = (PropertyImpl) parent.getProperty("jcr:data");
          data = ((TransientPropertyData) propertyImpl.getData()).getValues().get(0).getAsStream();
        } catch (PathNotFoundException e) {
          return false;
        }
      } else {
        return false;
      }

      if (!parent.isNodeType("dc:elementSet"))
        parent.addMixin("dc:elementSet");

      DocumentReaderService readerService = (DocumentReaderService) ((ExoContainer) ctx.get("exocontainer")).getComponentInstanceOfType(DocumentReaderService.class);
      if (readerService == null)
        throw new NullPointerException("No DocumentReaderService configured for current container");

      Properties props = new Properties();
      try {
        props = readerService.getDocumentReader(mimeType).getProperties(data);
      } catch (HandlerNotFoundException e) {
        log.debug(e.getMessage());
      }
      
      Iterator entries = props.entrySet().iterator();
      while (entries.hasNext()) {
        Entry entry = (Entry) entries.next();
        QName qname = (QName) entry.getKey();
        JCRName jcrName = property.getSession()
                                  .getLocationFactory()
                                  .createJCRName(new InternalQName(qname.getNamespace(),
                                                                   qname.getName()));

        PropertyDefinitionDatas pds = parent.getSession()
                                            .getWorkspace()
                                            .getNodeTypesHolder()
                                            .findPropertyDefinitions(jcrName.getInternalName(),
                                                                     ((NodeData) parent.getData()).getPrimaryTypeName(),
                                                                     ((NodeData) parent.getData()).getMixinTypeNames());
        if (pds.getDefinition(true) != null) {
          Value[] values = { createValue(entry.getValue(), property.getSession().getValueFactory()) };
          parent.setProperty(jcrName.getAsString(), values);
        } else {
          parent.setProperty(jcrName.getAsString(), createValue(entry.getValue(),
                                                                property.getSession()
                                                                        .getValueFactory()));
        }
      }

      return false;
    } finally {
      if (data != null)
        data.close();
    }
  }

  private Value createValue(Object obj, ValueFactory factory) throws ValueFormatException {
    if (obj instanceof String)
      return factory.createValue((String) obj);
    else if (obj instanceof Calendar)
      return factory.createValue((Calendar) obj);
    else if (obj instanceof Date) {
      Calendar cal = Calendar.getInstance();
      cal.setTime((Date) obj);
      return factory.createValue(cal);
    } else {
      throw new ValueFormatException("Unsupported value type " + obj.getClass());
    }
  }

}
