/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;
using exo_jcr.webdav.csclient.Request;
using exo_jcr.webdav.csclient.Response;
using exo_jcr.webdav.csclient.Search;
using exo_jcr.webdav.csclient;

/**
 * Created by The eXo Platform SARL
 * Authors : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 *         : Max Shaposhnik <uy7c@yahoo.com>
 * @version $Id:
 */

namespace exo_jcr.webdav.csclient.Commands
{
    public class SearchCommand : WebDavMultistatusCommand {


        private DavQuery query;

        public SearchCommand(DavContext context) : base(context)
        {
            CommandName = DavCommands.SEARCH;
           
        }


        public override void toXml(XmlTextWriter writer)
        {
            writer.WriteStartElement(DavConstants.PREFIX, "searchrequest", DavConstants.NAMESPACE);
            query.toXml(writer);
            writer.WriteEndElement();

        }


        public void setQuery(DavQuery query)
        {
            this.query = query;
        }
    }
}
