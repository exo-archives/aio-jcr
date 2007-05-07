/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.command;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.resource.resourcedata.ResourceData;
import org.exoplatform.services.webdav.common.response.DavStatus;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: GetCommand.java 12304 2007-01-25 10:23:57Z gavrikvetal $
 */

public class GetCommand extends WebDavCommand {
  
  @Override
  protected boolean process() throws Exception {
    WebDavResource resource = getResourceFactory().getSrcResource(false);
    
    ResourceData resourceData = resource.getResourceData();
    
    //DavResourceInfo info = resource.getInfo();
    
//    if (info.getContentType() != null) {
//      davResponse().setResponseHeader(DavConst.Headers.CONTENTTYPE, info.getContentType());
//    }

    if (resourceData.getContentType() != null) {
      davResponse().setResponseHeader(DavConst.Headers.CONTENTTYPE, resourceData.getContentType());
    }    
    
    InputStream resourceStream = resourceData.getContentStream();
    //InputStream resourceStream = info.getContentStream();
    
    long fileContentLengtn = resourceData.getContentLength();
    //long fileContentLengtn = info.getContentLength();
    
    long startRange = davRequest().getRangeStart();
    
    if (fileContentLengtn == 0) {
      davResponse().setStatus(DavStatus.OK);
      davResponse().setHeader(DavConst.Headers.CONTENTLENGTH, "0");
      return true;
    }

    if (startRange > fileContentLengtn - 1) {
      davResponse().setStatus(DavStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
      return false;
    }
    
    long endRange = davRequest().getRangeEnd();
    
    if (endRange > fileContentLengtn - 1) {
      davResponse().setStatus(DavStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
      return false;
    }
    
    if (startRange < 0) {
      startRange = 0;
    }
    if (endRange < 0) {
      endRange = fileContentLengtn - 1;
    }
    
    try {
      byte []buff = new byte[4096];

      long position = 0;
      boolean isPartialContent = false;
      
      while (position < startRange) {        
        isPartialContent = true;
        long needToRead = buff.length;
        if (needToRead > (startRange - position)) {
          needToRead = startRange - position;
        }
        
        long readed = resourceStream.read(buff, 0, (int)needToRead);

        if (readed < 0) {
          break;        
        }
        
        position += readed;
      }
      
      davResponse().setStatus(isPartialContent ? DavStatus.PARTIAL_CONTENT : DavStatus.OK);
      
      davResponse().setHeader(DavConst.Headers.ACCEPT_RANGES, "bytes");
      davResponse().setHeader(DavConst.Headers.CONTENTLENGTH, "" + (endRange - startRange + 1));
      davResponse().setHeader(DavConst.Headers.CONTENTRANGE, "bytes " + startRange + "-" + endRange + "/" + fileContentLengtn);
      
      while (position <= endRange) {
        long needToRead = buff.length;
        if (needToRead > (endRange - position + 1)) {
          needToRead = endRange - position + 1;
        }
        
        long readed = resourceStream.read(buff, 0, (int)needToRead);
        
        if (readed < 0) {
          break;
        }
        
        davResponse().writeToResponse(buff, (int)readed);
        position += readed;
      }

      return true;

    } catch (SocketException exc) {
      return false;
    } catch (IOException ioexc) {
      return false;
    }
  }
  
}
