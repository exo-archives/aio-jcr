/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.impl.dataflow.serialization;

import java.io.IOException;
import java.util.List;

import org.exoplatform.services.jcr.dataflow.serialization.ObjectWriter;
import org.exoplatform.services.jcr.dataflow.serialization.SerializationConstants;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: TransientPropertyDataWriter.java 111 2008-11-11 11:11:11Z serg $
 */
public class TransientPropertyDataWriter {

  /**
   * Write to stream all necessary object data.
   * 
   * @param out SerializationOutputStream.
   * @throws IOException If an I/O error has occurred.
   */
  public void write(ObjectWriter out, TransientPropertyData propData) throws IOException {
    // write id
    out.writeInt(SerializationConstants.TRANSIENT_PROPERTY_DATA);

    // TransientItemData
    out.writeString(propData.getQPath().getAsString());
    out.writeString(propData.getIdentifier());

    if (propData.getParentIdentifier() != null) {
      out.writeByte(SerializationConstants.NOT_NULL_DATA);
      out.writeString(propData.getParentIdentifier());
    } else
      out.writeByte(SerializationConstants.NULL_DATA);

    out.writeInt(propData.getPersistedVersion());
    // -------------------

    out.writeInt(propData.getType());
    out.writeBoolean(propData.isMultiValued());

    List<ValueData> values = propData.getValues();
    if (values != null) {
      out.writeByte(SerializationConstants.NOT_NULL_DATA);
      int listSize = values.size();
      out.writeInt(listSize);
      TransientValueDataWriter wr = new TransientValueDataWriter();
      for (int i = 0; i < listSize; i++) {
        wr.write(out, (TransientValueData) values.get(i));
      }
    } else {
      out.writeByte(SerializationConstants.NULL_DATA);
    }

  }

}
