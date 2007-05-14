/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
using System;
using System.Collections.Generic;
using System.Text;
using System.Net;
using System.Net.Sockets;
using System.IO;
using System.Xml;
using System.Collections;
using exo_jcr.webdav.csclient.Request;

/**
 * Created by The eXo Platform SARL
 * Authors : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 *         : Max Shaposhnik <uy7c@yahoo.com>
 * @version $Id:
 */

namespace exo_jcr.webdav.csclient.Commands
{
    public class WebDavTypedCommand : WebDavCommand
    {

        private String nodeType;
        
        public WebDavTypedCommand(DavContext context) : base(context)
        {
        }

        public override int execute()
        {
            if (nodeType != null)
            {
                byte[] bnodeType = getBytes(nodeType);
                addRequestHeader(HttpHeaders.NODETYPE, System.Convert.ToBase64String(bnodeType));
            }
            return base.execute();
        }

        public void setNodeType(String nodeType)
        {
            this.nodeType = nodeType;
        }

    }

}
