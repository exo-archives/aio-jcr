/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.resource.resourcedata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.common.resource.WebDavResource;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class CollectionResourceData extends AbstractResourceData {
  
//  private static Log log = ExoLogger.getLogger("jcr.CollectionResourceData");
  
  public CollectionResourceData(WebDavResource resource) throws RepositoryException, IOException {
    iscollection = true;
    
    name = resource.getName();
    contentType = "text/html";
    lastModified = "" + Calendar.getInstance();
    
    resourceInputStream = getHtmlInputStream(); 
    resourceLenght = resourceInputStream.available();
    
  }
  
  protected InputStream getHtmlInputStream() {
    String html = "<html></html>";    
    return new ByteArrayInputStream(html.getBytes());
  }
  
//  protected InputStream getHtmlInputStream() throws IOException {    
//    File sourceFile = new File("d://testfile.html");
//    FileInputStream fInStream = new FileInputStream(sourceFile);
//    
//    byte []fileData = new byte[fInStream.available()];
//    int readed = fInStream.read(fileData);
//    log.info("READED FROM FILE: " + readed);
//    fInStream.close();
//    
//    return new ByteArrayInputStream(fileData);
//  }
  
}
