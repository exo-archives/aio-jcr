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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.storage.value.cas.ValueContentAddressStorage;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

/**
 * Created by The eXo Platform SAS        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class CASableSimpleFileIOChannel extends SimpleFileIOChannel {
  
  private ValueContentAddressStorage vcas;
  private String digestAlgo;


  public CASableSimpleFileIOChannel(File rootDir, FileCleaner cleaner,
      String storageId, ValueContentAddressStorage vcas, String digestAlgo) {
    super(rootDir, cleaner, storageId);
    this.vcas = vcas;
    this.digestAlgo = digestAlgo;
  }
  

  @Override
  public void write(String propertyId, ValueData value) throws IOException {
    
    // TODO an algorithm? ("SHA", MD5)
    MessageDigest md;
    try {
      md = MessageDigest.getInstance(digestAlgo);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      throw new IOException(e.getMessage());
    }
    
    File tempfile = File.createTempFile("tmp", "tmp");
    FileOutputStream out = new FileOutputStream(tempfile);
    byte[] id = null;
    
    if (value.isByteArray()) {
      byte[] buff = value.getAsByteArray();
      out.write(buff);
      
      md.update(buff);
      id = md.digest();
      
    } else {
      byte[] buffer = new byte[FileIOChannel.IOBUFFER_SIZE];
      int len;
      InputStream in = value.getAsStream();
      while ((len = in.read(buffer)) > 0) {
        out.write(buffer, 0, len);
        
        md.update(buffer);
        id = md.digest();
      }
    }
    out.close();
    String identifier = new String(id);
    File outfile = new File(rootDir, identifier);
    // actually add content if not existed
    if(!outfile.exists())
      tempfile.renameTo(outfile);
    else
      tempfile.delete(); // should be ok without file cleaner
    // add reference to content anyway
    vcas.add(propertyId, value.getOrderNumber(), identifier);
  }

  

  @Override
  public boolean delete(String propertyId) throws IOException {
    vcas.delete(propertyId);
    return super.delete(propertyId);
  }


  @Override
  public ValueData read(String propertyId, int orderNumber, int maxBufferSize)
      throws IOException {
    return readValue(getFile(propertyId, orderNumber), orderNumber, maxBufferSize, false);
  }


  @Override
  protected File[] getFiles(String propertyId) {
    List <File> fileList = new ArrayList <File>();
    for(String identifier : vcas.getIdentifiers(propertyId))  
      fileList.add(new File(rootDir, identifier));
    
    File[] files = new File[fileList.size()];
    int i=0;
    for(File file : fileList)
      files[i++] = file;
      
    return files;
  }


  @Override
  protected File getFile(String propertyId, int orderNumber) {
    String identifier = vcas.getIdentifier(propertyId, orderNumber);
    return new File(rootDir, identifier);
  }


}

