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

namespace exo_jcr.webdav.csclient.DavProperties
{
    class OrderingTypeProperty : WebDavProperty
    {

        public OrderingTypeProperty() : base(DavProperty.ORDERINGTYPE)
        {
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
                            parseHref(reader);
                        }

                        break;

                    case XmlNodeType.EndElement:
                        if (reader.Name.EndsWith(DavProperty.ORDERINGTYPE))
                        {
                            return;
                        }
                        throw new XmlException("Malformed response at line " + reader.LineNumber + ":" + reader.LinePosition, null);
                }

            }


        }

        private void parseHref(XmlTextReader reader)
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
                        break;

                    case XmlNodeType.EndElement:
                        if (reader.Name.EndsWith("D:" + DavProperty.HREF))
                        {
                            return;
                        }
                        
                        throw new XmlException("Malformed response at line " + reader.LineNumber + ":" + reader.LinePosition, null);
                }

            }

        }


    }
}
