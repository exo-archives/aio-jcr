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
package org.exoplatform.services.jcr.impl.xml;

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

  @Override
  public String toString() {
    if (decoder != null) {
      return decoder.toString();
    }

    return stringBuffer.toString();
  }
}