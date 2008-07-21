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
import java.util.HashSet;
import java.util.List;

import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.storage.value.cas.ValueContentAddressStorage;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

/**
 * Created by The eXo Platform SAS        .
 * @author Gennady Azarenkov
 * @version $Id$
 */

public class CASableSimpleFileIOChannel extends SimpleFileIOChannel {
  
  //private static Log LOG = ExoLogger.getLogger("jcr.CASableSimpleFileIOChannel");
  
  private final CASeableIOSupport cas;
  
  private final ValueContentAddressStorage vcas;
  
  public CASableSimpleFileIOChannel(File rootDir, FileCleaner cleaner,
      String storageId, ValueContentAddressStorage vcas, String digestAlgo) {
    super(rootDir, cleaner, storageId);
    
    this.vcas = vcas;
    this.cas = new CASeableIOSupport(this, vcas, digestAlgo);
  }
  

//  @Override
//  public void write(String propertyId, ValueData value) throws IOException {
//    
//    // TODO an algorithm? ("SHA", MD5)
//    MessageDigest md;
//    try {
//      md = MessageDigest.getInstance(digestAlgo);
//    } catch (NoSuchAlgorithmException e) {
//      LOG.error("Can't wriet using " + digestAlgo + " algorithm, " + e, e);
//      throw new IOException(e.getMessage());
//    }
//    
//    // TODO create file in actual location but with special name
//    File tempfile = File.createTempFile("tmp", "tmp");
//    FileOutputStream out = new FileOutputStream(tempfile);
//    byte[] id = null;
//    
//    if (value.isByteArray()) {
//      byte[] buff = value.getAsByteArray();
//      out.write(buff);
//      
//      md.update(buff);
//      id = md.digest();
//      
//    } else {
//      
//      byte[] buffer = new byte[FileIOChannel.IOBUFFER_SIZE];
//      int len;
//      InputStream in = value.getAsStream();
//      while ((len = in.read(buffer)) > 0) {
//        out.write(buffer, 0, len);
//        
//        md.update(buffer);
//      }
//      
//      id = md.digest();
//    }
//    out.close();
//    
//    String identifier = new String(id);
//    File outfile = new File(rootDir, identifier);
//    
//    // actually add content if not existed
//    // TODO don't use renameTo 
//    if(!outfile.exists())
//      tempfile.renameTo(outfile);
//    else
//      tempfile.delete(); // should be ok without file cleaner
//    // add reference to content anyway
//    vcas.add(propertyId, value.getOrderNumber(), identifier);
//  }
  
  @Override
  public void write(String propertyId, ValueData value) throws IOException {
    // calc dogest hash
    // TODO optimize with NIO
    // we need hash at first to know do we have to store file or just use one existing (with same hash)
    FileDigestOutputStream out = cas.openFile(new File(rootDir, propertyId + value.getOrderNumber() + ".cas-temp"));
    writeOutput(out, value);
    
    // close stream
    out.close();
   
    // rename to hashnamed
    cas.saveFile(out);
    
    // add reference to content anyway
    vcas.add(propertyId, value.getOrderNumber(), out.getDigestHash());
  }
  
  /**
   * Delete given property value.<br/>
   * Special logic implemented for Values CAS. 
   * As the storage may have one file (same hash) for multiple properties/values.
   * Before the actual delete it will check if other properties/values are mapped to this content.
   * If are mapped the call just remove the mapping for the property with given id. 
   * If it's unique mapping the file will be deleted too.
   * 
   * @param propertyId - property id to be deleted
   */
  @Override
  public boolean delete(String propertyId) throws IOException {
    try {
      if (!vcas.hasSharedContent(propertyId))
       return super.delete(propertyId);
      
      return true;
    } finally {
      vcas.delete(propertyId);
    }
  }

  @Override
  protected File getFile(String propertyId, int orderNumber) throws IOException {
    return super.getFile(vcas.getIdentifier(propertyId, orderNumber), CASeableIOSupport.HASHFILE_ORDERNUMBER);
  }
  
  @Override
  protected File[] getFiles(String propertyId) throws IOException {
//    List <File> fileList = new ArrayList <File>();
//    for(String identifier : vcas.getIdentifiers(propertyId))  
//      fileList.add(new File(rootDir, identifier));// TODO getFile
//    
//    File[] files = new File[fileList.size()];
//    int i=0;
//    for(File file : fileList)
//      files[i++] = file;
//      
//    return files;
    
    List<String> hids = vcas.getIdentifiers(propertyId);
    File[] files = new File[hids.size()];
    for (int i=0; i<hids.size(); i++)
      files[i] = super.getFile(hids.get(i), CASeableIOSupport.HASHFILE_ORDERNUMBER);  
    
    return files;
  }
}

