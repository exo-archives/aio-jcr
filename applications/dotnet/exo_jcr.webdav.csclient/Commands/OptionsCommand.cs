/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
using System;
using System.Collections.Generic;
using System.Text;
using exo_jcr.webdav.csclient.Request;
using exo_jcr.webdav.csclient.Response;

/**
 * Created by The eXo Platform SARL
 * Authors : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 *         : Max Shaposhnik <uy7c@yahoo.com>
 * @version $Id:
 */

namespace exo_jcr.webdav.csclient.Commands
{
    public class OptionsCommand : WebDavPropertyRequestCommand
    {

        public OptionsCommand(DavContext context) : base(context)
        {
            CommandName = DavCommands.OPTIONS;
        }


    }
}
