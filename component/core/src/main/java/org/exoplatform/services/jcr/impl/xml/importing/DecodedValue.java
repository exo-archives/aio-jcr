/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS. All rights reserved.          *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml.importing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.ws.commons.util.Base64.Decoder;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */

/**
 * Temporary class for swapping values and decode binary values during import.
 * 
 * @author ksm
 */
public class DecodedValue {
  private BufferedDecoder decoder;

  private StringBuffer    stringBuffer;

  public DecodedValue() {
    super();
    stringBuffer = new StringBuffer();
  }

  /**
   * @return Base64 decoder. It is write decoded incoming data into the
   *         temporary file
   * @throws IOException
   */
  public Decoder getBinaryDecoder() throws IOException {
    if (decoder == null) {
      decoder = new BufferedDecoder();
      stringBuffer = null;
    }
    return decoder;
  }

  /**
   * @return InputStream from decoded file
   * @throws IOException
   */
  public InputStream getInputStream() throws IOException {
    if (decoder == null) {
      return new ByteArrayInputStream(new byte[0]);
    }

    return decoder.getInputStream();
  }

  public StringBuffer getStringBuffer() {
    return stringBuffer;
  }

  /**
   * Removes all temporary variables and files
   * 
   * @throws IOException
   */
  public void remove() throws IOException {

    if (decoder != null) {
      decoder.remove();
      decoder = null;
    }
  }

  // TODO change name
  @Override
  public String toString() {
    if (decoder != null) {
      return decoder.toString();
    }

    return stringBuffer.toString();
  }
}