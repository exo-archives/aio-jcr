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
package org.exoplatform.services.jcr.impl.storage.value.fs;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.storage.value.ValueDataResourceHolder;
import org.exoplatform.services.jcr.impl.storage.value.ValueOperation;
import org.exoplatform.services.jcr.impl.storage.value.cas.RecordNotFoundException;
import org.exoplatform.services.jcr.impl.storage.value.cas.VCASException;
import org.exoplatform.services.jcr.impl.storage.value.cas.ValueContentAddressStorage;
import org.exoplatform.services.jcr.impl.storage.value.fs.operations.CASableDeleteValues;
import org.exoplatform.services.jcr.impl.storage.value.fs.operations.CASableWriteValue;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS Date: 22.07.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class CASableTreeFileIOChannel extends TreeFileIOChannel {

  static private final Log                 LOG = ExoLogger.getLogger("jcr.CASableTreeFileIOChannel");

  private final ValueContentAddressStorage vcas;

  private final CASableIOSupport           cas;

  CASableTreeFileIOChannel(File rootDir,
                           FileCleaner cleaner,
                           String storageId,
                           ValueDataResourceHolder resources,
                           ValueContentAddressStorage vcas,
                           String digestAlgo) {
    super(rootDir, cleaner, storageId, resources);

    this.vcas = vcas;
    this.cas = new CASableIOSupport(this, digestAlgo);
  }

  @Override
  protected File getFile(final String propertyId, final int orderNumber) throws IOException {
    return super.getFile(vcas.getIdentifier(propertyId, orderNumber),
                         CASableIOSupport.HASHFILE_ORDERNUMBER);
  }

  @Override
  protected File[] getFiles(final String propertyId) throws IOException {
    List<String> hids = vcas.getIdentifiers(propertyId, true); // return only
    // own ids
    File[] files = new File[hids.size()];
    for (int i = 0; i < hids.size(); i++)
      // TODO super.getFile calls mkdirs(tfile.getParentFile())
      files[i] = super.getFile(hids.get(i), CASableIOSupport.HASHFILE_ORDERNUMBER);

    return files;
  }

  /**
   * Delete given property value.<br/>
   * Special logic implemented for Values CAS. As the storage may have one file (same hash) for
   * multiple properties/values.<br/>
   * The implementation assumes that delete operations based on {@link getFiles()} method result.
   * 
   * @see getFiles()
   * @param propertyId
   *          - property id to be deleted
   */
  @Override
  public void delete(String propertyId) throws IOException {
    File[] files;
    try {
      files = getFiles(propertyId);
    } catch (RecordNotFoundException e) {
      // This is workaround for CAS VS. No records found for this value at the moment.
      // CASableDeleteValues saves VCAS record on commit, but it's possible the Property just
      // added in this transaction and not commited.

      // TODO 08.04.2009 Skip error now
      files = new File[0];
    }
    CASableDeleteValues o = new CASableDeleteValues(files,
                                                    resources,
                                                    cleaner,
                                                    tempDir,
                                                    propertyId,
                                                    vcas);
    o.execute();
    changes.add(o);
  }

  @Override
  public void write(String propertyId, ValueData value) throws IOException {
    CASableWriteValue o = new CASableWriteValue(value,
                                                resources,
                                                cleaner,
                                                tempDir,
                                                propertyId,
                                                vcas,
                                                cas);
    o.execute();
    changes.add(o);
  }
}
