/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

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
