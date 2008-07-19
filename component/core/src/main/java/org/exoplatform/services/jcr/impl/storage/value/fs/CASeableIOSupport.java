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
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.impl.storage.value.cas.RecordAlreadyExistsException;
import org.exoplatform.services.jcr.impl.storage.value.cas.RecordNotFoundException;
import org.exoplatform.services.jcr.impl.storage.value.cas.ValueContentAddressStorage;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * CAS IO support covers some work will be produced in target FileIOChannels to make them CASeable.<br/> - add value - delete value -
 * 
 * Date: 15.07.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class CASeableIOSupport {

  private static Log            LOG                  = ExoLogger.getLogger("jcr.CASeableIOSupport");

  public static final int       HASHFILE_ORDERNUMBER = 0;

  protected final FileIOChannel channel;

  //protected final ValueContentAddressStorage vcas;
  protected final String        digestAlgo;

  CASeableIOSupport(FileIOChannel channel, ValueContentAddressStorage vcas, String digestAlgo) {
    this.channel = channel;
    //this.vcas = vcas;
    this.digestAlgo = digestAlgo;
  }

  /**
   * Open digester output.<br/> Digester output will write into given file and calc hash for a content.
   * 
   * @param file - destenation file
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
   * @param propertyId - proeprty id
   * @param orderNumber - value number
   * @param dout - digester write output (File writter)
   * @throws IOException
   * @throws RecordAlreadyExistsException
   */
  void saveFile(FileDigestOutputStream dout) throws IOException, RecordAlreadyExistsException {

    // work with digest
    File hashFile = new File(channel.rootDir, channel.makeFilePath(dout.getDigestHash(), 0));
    //File hashFile = new File(rootDir, hashId);

    // actually add content if not existed
    // TODO created file with name inherited from this channel super class
    // if same hash exists, the file should be removed
    // if doesn't the file will be renamed to hash name.
    if (hashFile.exists())
      // remove file if same exists
      dout.getFile().delete(); // should be ok without file cleaner
    else
    // rename propetynamed file to hashnamed one
    if (!dout.getFile().renameTo(hashFile))
      LOG.warn("File " + dout.getFile().getAbsolutePath() + " can't be renamed to hashnamed " + hashFile.getAbsolutePath());
  }

  //  /**
  //   * Construct file name for given hash id using particular FileIOChannel.
  //   * 
  //   * @param hashId
  //   * @return
  //   */
  //  private String makeFilePath(final String hashId) {
  //    return "h-" + channel.makeFilePath(hashId, 0);  // TODO remove h-
  //  }
  //  
  //  /**
  //   * Construct file name for value by property id and order number.
  //   * 
  //   * @param propertyId
  //   * @param orderNumber
  //   * @return
  //   * @throws RecordNotFoundException
  //   */
  //  String makeFilePath(final String propertyId, final int orderNumber) throws RecordNotFoundException {
  //    return makeFilePath(vcas.getIdentifier(propertyId, orderNumber));
  //  }
  //  
  //  /**
  //   * Construct files names array for property.
  //   * 
  //   * @param propertyId
  //   * @return
  //   */
  //  String[] getFilesPaths(final String propertyId) {
  //    List <String> fileList = new ArrayList <String>();
  //    for(String hashId : vcas.getIdentifiers(propertyId))  
  //      fileList.add(makeFilePath(hashId));
  //    
  //    String[] files = new String[fileList.size()];
  //    fileList.toArray(files);
  //    return files;
  //  }

}
