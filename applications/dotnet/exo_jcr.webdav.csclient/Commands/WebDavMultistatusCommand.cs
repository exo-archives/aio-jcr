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
using System.Collections;
using System.IO;
using exo_jcr.webdav.csclient.Response;
using exo_jcr.webdav.csclient.Request;

/**
 * Created by The eXo Platform SARL
 * Authors : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 *         : Max Shaposhnik <uy7c@yahoo.com>
 * @version $Id:
 */

namespace exo_jcr.webdav.csclient.Commands
{
    public abstract class WebDavMultistatusCommand : WebDavCommand {

        protected Multistatus multistatus;

        public WebDavMultistatusCommand(DavContext context): base(context){
            isNeedXmlRequest = true;
        }

        public virtual void toXml(XmlTextWriter writer)
        {
        }

        public override byte[] generateXmlRequest()
        {
            MemoryStream xmlBuffer = new MemoryStream();

            XmlTextWriter writer = new XmlTextWriter(xmlBuffer, Encoding.UTF8);

            writer.WriteStartDocument();

            toXml(writer);

            writer.Flush();

            string responseString = Encoding.UTF8.GetString(xmlBuffer.GetBuffer(), 0, (int)xmlBuffer.Length);
            responseString = responseString.Substring(1);
            return getBytes(responseString);            
        }

        public override void finalizeExecuting()
        {
            if (getStatus() == DavStatus.MULTISTATUS)
            {
                parseXmlResponse(getResponseBody());
            }
        }

        public bool parseXmlResponse(byte[] response)
        {
            XmlTextReader reader = new XmlTextReader(new MemoryStream(response));
            reader.Namespaces = true;

            while (reader.Read())
            {
                switch (reader.NodeType)
                {
                    case XmlNodeType.Element:
                        if (reader.Name.EndsWith(DavProperty.MULTISTATUS))
                        {
                            multistatus = new Multistatus(reader);
                        }
                        break;
                }
            }

            return true;
        }

        public Multistatus getMultistatus()
        {
            return multistatus;
        }

    }
}
