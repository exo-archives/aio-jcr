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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.impl.storage.value.cas.RecordAlreadyExistsException;
import org.exoplatform.services.jcr.impl.storage.value.cas.VCASException;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * CAS IO support covers some work will be produced in target FileIOChannels to make them
 * CASeable.<br/> - add value - delete value -
 * 
 * Date: 15.07.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class CASableIOSupport {

  private static Log            LOG                  = ExoLogger.getLogger("jcr.CASeableIOSupport");

  public static final int       HASHFILE_ORDERNUMBER = 0;

  protected final FileIOChannel channel;

  protected final String        digestAlgo;

  CASableIOSupport(FileIOChannel channel, String digestAlgo) {
    this.channel = channel;
    this.digestAlgo = digestAlgo;
  }

  /**
   * Open digester output.<br/> Digester output will write into given file and calc hash for a
   * content.
   * 
   * @param file
   *          - destenation file
   * @return - digester output stream
   * @throws IOException
   */
  FileDigestOutputStream openFile(File file) throws IOException {
    MessageDigest md;
    try {
      md = MessageDigest.getInstance(digestAlgo);
    } catch (NoSuchAlgorithmException e) {
      LOG.error("Can't wriet using " + digestAlgo + " algorithm, " + e, e);
      throw new IOException(e.getMessage());
    }

    return new FileDigestOutputStream(file, md);
  }

  /**
   * Save content of digester output to storage and record hash id in address database.
   * 
   * @param propertyId
   *          - proeprty id
   * @param orderNumber
   *          - value number
   * @param dout
   *          - digester write output (File writter)
   * @throws IOException
   * @throws RecordAlreadyExistsException
   */
  void saveFile(FileDigestOutputStream dout) throws IOException, RecordAlreadyExistsException {

    // work with digest
    File vcasFile = new File(channel.rootDir, channel.makeFilePath(dout.getDigestHash(), 0));
    // TODO (same performed in TreeFileIOChannel.getFile()) make sure parent dir exists
    vcasFile.getParentFile().mkdirs();

    // Actually add content if not existed
    // Created file with name inherited from this channel super class.
    // If same hash exists, the file should be removed
    // if doesn't the file will be renamed to hash name.
    if (vcasFile.exists())
      // remove file if same exists
      dout.getFile().delete(); // should be ok without file cleaner
    else if (!dout.getFile().renameTo(vcasFile)) // rename propetynamed file to hashnamed one
      throw new VCASException("File " + dout.getFile().getAbsolutePath()
          + " can't be renamed to VCAS-named " + vcasFile.getAbsolutePath());
  }
}
