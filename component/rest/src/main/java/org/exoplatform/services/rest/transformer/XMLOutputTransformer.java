/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.transformer;

import java.io.IOException;
import java.io.OutputStream;

//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
import java.io.*;


import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * This type of transformer can write XML in output stream.
 *  
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class XMLOutputTransformer extends OutputEntityTransformer {

  private long length = 0;
  
	/* (non-Javadoc)
	 * @see org.exoplatform.services.rest.transformer.OutputEntityTransformer#writeTo(java.lang.Object, java.io.OutputStream)
	 */
	@Override
	public void writeTo(Object entity, OutputStream entityDataStream)
			throws IOException {
    Document entity_ = (Document)entity;
    try {
      TransformerFactory.newInstance().newTransformer().transform(new DOMSource(entity_),
          new StreamResult(entityDataStream));
    } catch (TransformerException tre) {
      throw new IOException("Can't write to output stream " + tre);
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
