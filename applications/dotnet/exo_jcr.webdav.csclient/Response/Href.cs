/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;

/**
 * Created by The eXo Platform SARL
 * Authors : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 *         : Max Shaposhnik <uy7c@yahoo.com>
 * @version $Id:
 */

namespace exo_jcr.webdav.csclient.Response
{
    public class Href
    {

        private String href;

        public Href(XmlTextReader reader)
        {
            while (reader.Read()) {
                switch (reader.NodeType)
                {
                    case XmlNodeType.Text:
                    case XmlNodeType.CDATA:
                        {
                            href = reader.Value;
                            
                            href = href.Replace("%3a", ":");

                            break;
                        }
                    case XmlNodeType.EndElement:
                        {
                            if (reader.Name.EndsWith(DavProperty.HREF))
                            {
                                return;
                            }

                            throw new XmlException("Malformed response at line " + reader.LineNumber + ":" + reader.LinePosition, null);                            
                        }
                }
            }
        }

        public String getHref()
        {
            return href;
        }

    }
}
