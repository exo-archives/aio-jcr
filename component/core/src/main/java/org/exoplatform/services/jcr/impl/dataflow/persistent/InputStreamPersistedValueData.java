/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.dataflow.persistent;

import java.io.IOException;
import java.io.InputStream;

import org.exoplatform.services.jcr.impl.dataflow.AbstractValueData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class InputStreamPersistedValueData extends AbstractValueData {

  protected final InputStream in;

  public InputStreamPersistedValueData(InputStream in, int orderNumber) { 
    super(orderNumber);
    this.in = in;
  }

  @Override
  public TransientValueData createTransientCopy() {
    return new TransientValueData(orderNumber, null, in, 
        null, null, -1, null, false);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.ValueData#getAsByteArray()
   */
  public byte[] getAsByteArray() throws IllegalStateException, IOException {
    byte[] buff = new byte[in.available()];
    in.read(buff);
    return buff;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.ValueData#getAsStream()
   */
  public InputStream getAsStream() throws IOException {
    return in;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.ValueData#getLength()
   */
  public long getLength() {
    try {
      return in.available();
    } catch(IOException ioe) {
      return -1;
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.ValueData#isByteArray()
   */
  public boolean isByteArray() {
    return false;
  }

}
