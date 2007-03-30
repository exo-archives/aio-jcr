/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;

using exo_jcr.webdav.csclient.Response;

/**
 * Created by The eXo Platform SARL
 * Authors : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 *         : Max Shaposhnik <uy7c@yahoo.com>
 * @version $Id:
 */

namespace exo_jcr.webdav.csclient.DavProperties
{
    public class CheckedInProperty : WebDavProperty
    {

        private bool checkedIn = false;

        private Href href;

        public CheckedInProperty() : base(DavProperty.CHECKEDIN)
        {
        }

        public bool isCheckedIn()
        {
            return checkedIn;
        }

        public Href getHref()
        {
            return href;
        }

        public override void init(XmlTextReader reader)
        {
            if (reader.IsEmptyElement)
            {
                return;
            }

            while (reader.Read())
            {

                switch (reader.NodeType)
                {
                    case XmlNodeType.Element:

                        if (reader.Name.EndsWith("D:" + DavProperty.HREF))
                        {
                            href = new Href(reader);
                        }

                        break;

                    case XmlNodeType.EndElement:
                        if (reader.Name.EndsWith(DavProperty.CHECKEDIN))
                        {
                            return;
                        }
                        throw new XmlException("Malformed response at line " + reader.LineNumber + ":" + reader.LinePosition, null);
                }

            }
        }


    }
}
