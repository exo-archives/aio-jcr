/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
using System;
using System.Collections.Generic;
using System.Text;
using exo_jcr.webdav.csclient.Request;

/**
 * Created by The eXo Platform SARL
 * Authors : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 *         : Max Shaposhnik <uy7c@yahoo.com>
 * @version $Id:
 */

namespace exo_jcr.webdav.csclient.Commands
{
    public  class MoveCommand : WebDavCommand {

        protected String destinationPath = "";

        public MoveCommand(DavContext context): base(context)
        {
            CommandName = DavCommands.MOVE;
        }

        public override int execute()
        {
            addRequestHeader(HttpHeaders.DESTINATION, destinationPath);
            int status = base.execute();
            return status;
        }

        public void setDestinationPath(String destinationPath)
        {
            this.destinationPath = "http://" + context.Host + ":" + context.Port + context.ServletPath + destinationPath;
        }

    }
}
