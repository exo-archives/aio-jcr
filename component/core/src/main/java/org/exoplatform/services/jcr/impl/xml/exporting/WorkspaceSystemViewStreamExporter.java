/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.impl.xml.exporting;

import java.io.IOException;
import java.util.List;

import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.core.ExtendedPropertyType;
import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class WorkspaceSystemViewStreamExporter extends SystemViewStreamExporter {
  /**
   * Class logger.
   */
  private final Log log = ExoLogger.getLogger("jcr.WorkspaceSystemViewStreamExporter");

  public WorkspaceSystemViewStreamExporter(XMLStreamWriter writer,
                                           SessionImpl session,
                                           ItemDataConsumer dataManager,
                                           ValueFactoryImpl systemValueFactory,
                                           boolean skipBinary,
                                           boolean noRecurse) throws NamespaceException,
      RepositoryException {
    super(writer, session, dataManager, systemValueFactory, skipBinary, noRecurse);
  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.services.jcr.impl.xml.exporting.SystemViewStreamExporter#entering(org.exoplatform
   * .services.jcr.datamodel.NodeData, int)
   */
  @Override
  protected void entering(NodeData node, int level) throws RepositoryException {
    super.entering(node, level);
    try {
      writer.writeAttribute(Constants.NS_EXO_PREFIX,
                            Constants.NS_EXO_URI,
                            Constants.SV_ID,
                            node.getIdentifier());

    } catch (XMLStreamException e) {
      throw new RepositoryException(e);
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.services.jcr.impl.xml.exporting.SystemViewStreamExporter#entering(org.exoplatform
   * .services.jcr.datamodel.PropertyData, int)
   */
  @Override
  protected void entering(PropertyData property, int level) throws RepositoryException {
    try {
      writer.writeStartElement(Constants.NS_SV_PREFIX, Constants.SV_PROPERTY, SV_NAMESPACE_URI);

      writer.writeAttribute(Constants.NS_SV_PREFIX,
                            SV_NAMESPACE_URI,
                            Constants.SV_NAME,
                            getExportName(property, false));

      writer.writeAttribute(Constants.NS_SV_PREFIX,
                            SV_NAMESPACE_URI,
                            Constants.SV_TYPE,
                            ExtendedPropertyType.nameFromValue(property.getType()));

      writer.writeAttribute(Constants.NS_EXO_PREFIX,
                            Constants.NS_EXO_URI,
                            Constants.SV_ID,
                            property.getIdentifier());

      List<ValueData> values = property.getValues();
      for (ValueData valueData : values) {

        writer.writeStartElement(Constants.NS_SV_PREFIX, Constants.SV_VALUE, SV_NAMESPACE_URI);

        writeValueData(valueData, property.getType());

        writer.writeEndElement();
      }
    } catch (XMLStreamException e) {
      throw new RepositoryException("Can't export value to string: " + e.getMessage(), e);
    } catch (IllegalStateException e) {
      throw new RepositoryException("Can't export value to string: " + e.getMessage(), e);
    } catch (IOException e) {
      throw new RepositoryException("Can't export value to string: " + e.getMessage(), e);
    }
  }
}
