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
package org.exoplatform.services.jcr.impl.storage.value.fs.operations;

import java.io.File;
import java.io.IOException;

import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.storage.value.ValueDataResourceHolder;
import org.exoplatform.services.jcr.impl.storage.value.cas.RecordAlreadyExistsException;
import org.exoplatform.services.jcr.impl.storage.value.cas.VCASException;
import org.exoplatform.services.jcr.impl.storage.value.cas.ValueContentAddressStorage;
import org.exoplatform.services.jcr.impl.storage.value.fs.CASableIOSupport;
import org.exoplatform.services.jcr.impl.storage.value.fs.FileDigestOutputStream;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date: 03.04.2009
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class CASableWriteValue extends WriteValue {

  /**
   * CAS manager.
   */
  protected final ValueContentAddressStorage vcas;

  /**
   * CAS I/O support.
   */
  protected final CASableIOSupport           cas;

  /**
   * Affected Property Id.
   */
  protected final String                     propertyId;

  /**
   * Value order number.
   */
  protected final int                        orderNumb;

  /**
   * Temp file.
   */
  protected File                             tempFile;

  /**
   * CAS file.
   */
  protected File                             vcasFile;

  /**
   * Value CAS hash.
   */
  protected String                           vcasHash;

  /**
   * CASableWriteValue constructor.
   * 
   * @param value
   *          ValueData to be saved
   * @param resources
   *          ValueDataResourceHolder
   * @param cleaner
   *          FileCleaner
   * @param tempDir
   *          File
   * @param propertyId
   *          Affected Property Id.
   * @param vcas
   *          ValueContentAddressStorage
   * @param cas
   *          CASableIOSupport
   */
  public CASableWriteValue(ValueData value,
                           ValueDataResourceHolder resources,
                           FileCleaner cleaner,
                           File tempDir,
                           String propertyId,
                           ValueContentAddressStorage vcas,
                           CASableIOSupport cas) {
    super(null, value, resources, cleaner, tempDir);

    this.vcas = vcas;
    this.cas = cas;
    this.propertyId = propertyId;
    this.orderNumb = value.getOrderNumber();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute() throws IOException {
    makePerformed();

    // write calc digest hash
    // TODO optimize with NIO
    // we need hash at first to know do we have to store file or just use one
    // existing (with same hash)
    File temp = new File(tempDir, IdGenerator.generate() + "-" + propertyId + orderNumb
        + TEMP_FILE_EXTENSION);
    FileDigestOutputStream out = cas.openFile(temp);
    try {
      writeOutput(out, value);
    } finally {
      out.close();
    }

    // CAS hash
    vcasHash = out.getDigestHash();

    // add reference to content
    // get VCAS-named file
    vcasFile = cas.getFile(vcasHash);

    // lock CAS file location
    fileLock = new ValueFileLock(vcasFile);
    fileLock.lock();

    this.tempFile = temp;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void commit() throws IOException {
    if (fileLock != null)
      try {
        // write VCAS record first
        try {
          vcas.addValue(propertyId, orderNumb, vcasHash);
        } catch (RecordAlreadyExistsException e) {
          if (tempFile != null && tempFile.exists() && !tempFile.delete()) {
            LOG.warn("Can't delete CAS temp file. Added to file cleaner. "
                + tempFile.getAbsolutePath());
            cleaner.addFile(tempFile);
          }
          throw new RecordAlreadyExistsException("Write error: " + e, e);
        }

        if (!vcasFile.exists()) {
          // it's new CAS Value, we have to move temp to vcas location
          // use RENAME only, don't copy - as copy will means that destination already exists etc.

          // make sure parent dir exists
          vcasFile.getParentFile().mkdirs();
          if (!tempFile.renameTo(vcasFile)) // rename propetynamed file to hashnamed one
            throw new VCASException("File " + tempFile.getAbsolutePath()
                + " can't be renamed to VCAS-named " + vcasFile.getAbsolutePath());
        } // else - CASed Value already exists

        // set new spool file
        ((TransientValueData) value).setSpoolFile(vcasFile);

      } finally {
        // remove temp file
        tempFile.delete(); // should be ok without file cleaner

        fileLock.unlock();
      }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void rollback() throws IOException {
    if (fileLock != null)
      try {
        // delete temp file - it's new file add
        tempFile.delete(); // should be ok without file cleaner
      } finally {
        fileLock.unlock();
      }
  }

}
