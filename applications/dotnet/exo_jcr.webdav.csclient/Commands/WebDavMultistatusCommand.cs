/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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
            Console.WriteLine("RS:" + responseString);
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
