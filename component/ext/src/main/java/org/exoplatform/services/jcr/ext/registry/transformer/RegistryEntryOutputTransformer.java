/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.ext.registry.transformer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.xml.transform.TransformerException;

import org.exoplatform.services.jcr.ext.registry.RegistryEntry;
import org.exoplatform.services.rest.transformer.OutputEntityTransformer;
import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class RegistryEntryOutputTransformer extends OutputEntityTransformer {

  private long length = 0;
  
	@Override
	public void writeTo(Object entity, OutputStream entityDataStream)
			throws IOException {
		
		RegistryEntry regEntry = (RegistryEntry) entity;
		PassthroughOutputTransformer transformer = new PassthroughOutputTransformer();
		try {
			transformer.writeTo(regEntry.getAsInputStream(), entityDataStream);
		} catch (TransformerException tre) {
			throw new IOException("Can't get RegistryEntry as stream " + tre);
		}
	}
	
  /* (non-Javadoc)
   * @see org.exoplatform.services.rest.transformer.OutputEntityTransformer#getContentLength(java.lang.Object)
   */
  public long getContentLength(Object entity) {
    try {
      countContentLenght(entity);
    } catch(IOException ioe) {
      return -1;
    }
    return (length > 0) ? length : -1;
  }


  private void countContentLenght(Object entity)
      throws IOException {
    
    final Object entity_ = entity;
    final PipedOutputStream pou = new PipedOutputStream();
    final PipedInputStream pin = new PipedInputStream(pou);

    new Thread() {
      public void run() {
        try {
          writeTo(entity_, pou);
          pou.flush();
          pou.close();
        } catch (Exception e) {
          length = 0;
        }
      }
    }.start();
    
    int rd = -1;
    byte[] buff = new byte[1024];
    while ((rd = pin.read(buff)) != -1)
      length+=rd;
    pin.close();

  }
	

}
