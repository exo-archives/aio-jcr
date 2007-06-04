/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import javax.mail.internet.MimeMultipart;
import javax.mail.MessagingException;
import javax.mail.util.ByteArrayDataSource;
import javax.activation.DataSource;
import java.io.InputStream;
import java.io.IOException;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class MultipartEntity extends MimeMultipart {
  
  private MultipartEntity(DataSource ds) throws MessagingException {
    super(ds);
  }
  
  public static MultipartEntity getInstance(InputStream in, String type) throws IOException {
    ByteArrayDataSource byteArrayDS = new ByteArrayDataSource(in, type);
    try {
      return new MultipartEntity(byteArrayDS);
    } catch (MessagingException me) {
      return null;
    }
  }

}
