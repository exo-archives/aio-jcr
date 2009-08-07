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
import org.exoplatform.services.jcr.impl.storage.value.cas.RecordAlreadyExistsException;
import org.exoplatform.services.jcr.impl.storage.value.cas.ValueContentAddressStorage;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author Gennady Azarenkov
 * @version $Id$
 */

public class CASableSimpleFileIOChannel extends SimpleFileIOChannel {

  static private final Log                 LOG = ExoLogger.getLogger("jcr.CASableSimpleFileIOChannel");

  private final CASableIOSupport           cas;

  private final ValueContentAddressStorage vcas;

  public CASableSimpleFileIOChannel(File rootDir,
                                    FileCleaner cleaner,
                                    String storageId,
                                    ValueContentAddressStorage vcas,
                                    String digestAlgo) {
    super(rootDir, cleaner, storageId);

    this.vcas = vcas;
    this.cas = new CASableIOSupport(this, digestAlgo);
  }

  @Override
  public void write(String propertyId, ValueData value) throws IOException {
    // calc digest hash
    // TODO optimize with NIO
    // we need hash at first to know do we have to store file or just use one existing (with same
    // hash)
    File temp = new File(rootDir, propertyId + value.getOrderNumber() + ".cas-temp");
    FileDigestOutputStream out = cas.openFile(temp);
    writeOutput(out, value);

    // close stream
    out.close();

    // add reference to content
    try {
      vcas.add(propertyId, value.getOrderNumber(), out.getDigestHash());
      // rename to VCAS-named
      cas.saveFile(out);
    } catch (RecordAlreadyExistsException e) {
      if (!temp.delete()) {
        LOG.warn("Can't delete cas-temp file. Added to file cleaner. " + temp.getAbsolutePath());
        cleaner.addFile(temp);
      }
      throw new RecordAlreadyExistsException("Write error: " + e, e);
    }
  }

  /**
   * Delete given property value.<br/>
   * Special logic implemented for Values CAS. As the storage may have one file (same hash) for
   * multiple properties/values.<br/>
   * The implementation assumes that delete operations based on {@link getFiles()} method result.
   * 
   * @see getFiles()
   * 
   * @param propertyId
   *          - property id to be deleted
   */
  @Override
  public boolean delete(String propertyId) throws IOException {
    try {
      return super.delete(propertyId);
    } finally {
      vcas.delete(propertyId);
    }
  }

  @Override
  protected File getFile(String propertyId, int orderNumber) throws IOException {
    return super.getFile(vcas.getIdentifier(propertyId, orderNumber),
                         CASableIOSupport.HASHFILE_ORDERNUMBER);
  }

  /**
   * Returns storage files list by propertyId.<br/>
   * 
   * NOTE: Files list used for <strong>delete</strong> operation. The list will not contains files
   * shared with other properties!
   * 
   * @see ValueContentAddressStorage.getIdentifiers()
   * 
   * @param propertyId
   * @return actual files on file system related to given propertyId
   */
  @Override
  protected File[] getFiles(String propertyId) throws IOException {
    List<String> hids = vcas.getIdentifiers(propertyId, true); // return only own ids
    File[] files = new File[hids.size()];
    for (int i = 0; i < hids.size(); i++)
      files[i] = super.getFile(hids.get(i), CASableIOSupport.HASHFILE_ORDERNUMBER);

    return files;
  }
}
