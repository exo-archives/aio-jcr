/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;
using System.Collections;

/**
 * Created by The eXo Platform SARL
 * Authors : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 *         : Max Shaposhnik <uy7c@yahoo.com>
 * @version $Id:
 */

namespace exo_jcr.webdav.csclient.Response
{
    public class Multistatus
    {

        protected ArrayList responses = new ArrayList();

        public Multistatus(XmlTextReader reader) {
            while (reader.Read()) {
                switch (reader.NodeType) {
                    case XmlNodeType.Element:
                        if (reader.Name.EndsWith(DavProperty.RESPONSE)) {
                            responses.Add(new DavResponse(reader));
                        }
                        break;
                    case XmlNodeType.EndElement:
                        if (reader.Name.EndsWith(DavProperty.MULTISTATUS)) {
                            return;
                        }
                        throw new XmlException("Malformed response at line " + reader.LineNumber + ":" + reader.LinePosition, null);
                }
            }
        }

        public ArrayList getResponses()
        {
            return responses;
        }

    }
}
