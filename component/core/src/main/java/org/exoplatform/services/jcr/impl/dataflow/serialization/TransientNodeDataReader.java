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

import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.serialization.ObjectReader;
import org.exoplatform.services.jcr.dataflow.serialization.SerializationConstants;
import org.exoplatform.services.jcr.dataflow.serialization.UnknownClassIdException;
import org.exoplatform.services.jcr.datamodel.IllegalNameException;
import org.exoplatform.services.jcr.datamodel.IllegalPathException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: TransientNodeDataReader.java 111 2008-11-11 11:11:11Z serg $
 */
public class TransientNodeDataReader {

  /**
   * Read and set TransientNodeData data.
   * 
   * @param in ObjectReader.
   * @return TransientNodeData object.
   * @throws UnknownClassIdException If read Class ID is not expected or do not
   *           exist.
   * @throws IOException If an I/O error has occurred.
   */
  public TransientNodeData read(ObjectReader in) throws UnknownClassIdException,
                                                            IOException {
    // read id
    int key;
    if ((key = in.readInt()) != SerializationConstants.TRANSIENT_NODE_DATA) {
      throw new UnknownClassIdException("There is unexpected class [" + key + "]");
    }

    // TransientItemData
    QPath qpath;
    try {
      String sQPath = in.readString();
      qpath = QPath.parse(sQPath);
    } catch (final IllegalPathException e) {
      throw new IOException("Deserialization error. " + e) {

        /**
         * {@inheritDoc}
         */
        @Override
        public Throwable getCause() {
          return e;
        }
      };
    }

    String identifier = in.readString();

    String parentIdentifier = null;
    if (in.readInt() == SerializationConstants.NOT_NULL_DATA) {
      parentIdentifier = in.readString();
    }

    int persistedVersion = in.readInt();
    // --------------

    int orderNum = in.readInt();

    // primary type
    InternalQName primaryTypeName;
    try {
      primaryTypeName = InternalQName.parse(in.readString());
    } catch (final IllegalNameException e) {
      throw new IOException(e.getMessage()) {
        private static final long serialVersionUID = 3489809179234435267L;

        /**
         * {@inheritDoc}
         */
        @Override
        public Throwable getCause() {
          return e;
        }
      };
    }

    // mixins
    int count = in.readInt();
    InternalQName[] mixinTypeNames = new InternalQName[count];
    for (int i = 0; i < count; i++) {
      try {
        mixinTypeNames[i] = InternalQName.parse(in.readString());
      } catch (final IllegalNameException e) {
        throw new IOException(e.getMessage()) {
          private static final long serialVersionUID = 3489809179234435268L; // eclipse

          // gen

          /**
           * {@inheritDoc}
           */
          @Override
          public Throwable getCause() {
            return e;
          }
        };
      }
    }

    // acl
    ACLReader rdr = new ACLReader();
    AccessControlList acl = rdr.read(in);

    return new TransientNodeData(qpath,
                                 identifier,
                                 persistedVersion,
                                 primaryTypeName,
                                 mixinTypeNames,
                                 orderNum,
                                 parentIdentifier,
                                 acl);

  }

}
