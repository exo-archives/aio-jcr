/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.dataflow.persistent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.exoplatform.services.jcr.impl.dataflow.AbstractValueData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id$
 */

public class ByteArrayPersistedValueData extends AbstractValueData {
  

  protected byte[] data;

  public ByteArrayPersistedValueData(byte[] data, int orderNumber) {
    super(orderNumber);
    this.data = data;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.AbstractValueData#getAsStream()
   */
  public InputStream getAsStream() throws IOException {
    return new ByteArrayInputStream(data);
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.AbstractValueData#getAsByteArray()
   */
  public byte[] getAsByteArray() throws IllegalStateException {
    return data;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.AbstractValueData#getLength()
   */
  public long getLength() {
    return data.length;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.AbstractValueData#isByteArray()
   */
  public boolean isByteArray() {
    return true;
  }

  
  @Override
  public TransientValueData createTransientCopy() {
    return new TransientValueData(orderNumber, data, 
        null, null, null, -1, null, false);

  }

}
