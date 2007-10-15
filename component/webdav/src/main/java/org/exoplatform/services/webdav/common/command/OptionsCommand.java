/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.command;

import java.util.ArrayList;

import org.exoplatform.services.webdav.DavConst;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: OptionsCommand.java 12304 2007-01-25 10:23:57Z gavrikvetal $
 */

public class OptionsCommand extends WebDavCommand {
  
  protected boolean process() {
    String allowedCommands = "";
    
    ArrayList<String> availableCommands = davContext().getAvailableCommands();
    for (int i = 0; i < availableCommands.size(); i++) {
      String curCommand = availableCommands.get(i);
      allowedCommands += curCommand;
      if (i < (availableCommands.size() - 1)) {
        allowedCommands += ", ";
      }
    }
    
    davResponse().setHeader(DavConst.Headers.ALLOW, allowedCommands);
    
    davResponse().setHeader(DavConst.Headers.DAV, DavConst.DAV_HEADER);    
    davResponse().setHeader(DavConst.Headers.DASL, DavConst.DASL_VALUE);    
    davResponse().setHeader(DavConst.Headers.MSAUTHORVIA, DavConst.DAV_MSAUTHORVIA);
    
    return true;    
  } 

}


/*

 >> Request:

   OPTIONS /somecollection/ HTTP/1.1
   Host: example.org

    >> Response:

   HTTP/1.1 200 OK
   Allow: OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE, COPY, MOVE
   Allow: MKCOL, PROPFIND, PROPPATCH, LOCK, UNLOCK, ORDERPATCH
   DAV: 1, 2, ordered-collections

   The DAV header in the response indicates that the resource
   /somecollection/ is level 1 and level 2 compliant, as defined in
   [RFC2518].  In addition, /somecollection/ supports ordering.  The
   Allow header indicates that ORDERPATCH requests can be submitted to
   /somecollection/.
 


 */
