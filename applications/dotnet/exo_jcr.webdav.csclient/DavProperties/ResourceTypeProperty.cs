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

namespace exo_jcr.webdav.csclient.DavProperties
{
    public class ResourceTypeProperty : WebDavProperty
    {

        public static int RESOURCE = 0;
        public static int COLLECTION = 1;

        private int resourceType = RESOURCE;

        public ResourceTypeProperty() : base(DavProperty.RESOURCETYPE) {
        }

        public override void init(XmlTextReader reader)
        {
            if (reader.IsEmptyElement) {
                return;
            }

            while (reader.Read()) {

                switch (reader.NodeType) {
                    case XmlNodeType.Element:

                        if (reader.Name.EndsWith("D:" + DavProperty.COLLECTION)) {
                            resourceType = COLLECTION;
                        }

                        break;

                    case XmlNodeType.EndElement:
                        if (reader.Name.EndsWith(DavProperty.RESOURCETYPE)) {
                            return;
                        }
                        throw new XmlException("Malformed response at line " + reader.LineNumber + ":" + reader.LinePosition, null);
                }

            }
        }

        public int getResourceType()
        {
            return resourceType;
        }


    }
}
