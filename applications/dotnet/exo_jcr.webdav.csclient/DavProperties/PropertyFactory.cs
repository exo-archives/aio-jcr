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
    public class PropertyFactory
    {
        public static WebDavProperty parseProperty(XmlTextReader reader)
        {
            WebDavProperty property;

            String propertyName = reader.Name;

            while (true)
            {
                if (propertyName.EndsWith("D:" + DavProperty.DISPLAYNAME))
                {
                    property = new DisplayNameProperty();
                    break;
                }

                if (propertyName.EndsWith("D:" + DavProperty.RESOURCETYPE))
                {
                    property = new ResourceTypeProperty();
                    break;
                }

                if (propertyName.EndsWith("D:" + DavProperty.GETCONTENTTYPE))
                {
                    property = new ContentTypeProperty();
                    break;
                }

                if (propertyName.EndsWith("D:" + DavProperty.CREATIONDATE))
                {
                    property = new CreationDateProperty();
                    break;
                }

                if (propertyName.EndsWith("D:" + DavProperty.GETLASTMODIFIED))
                {
                    property = new LastModifiedProperty();
                    break;
                }

                if (propertyName.EndsWith("D:" + DavProperty.SUPPORTEDLOCK))
                {
                    property = new SupportedLockProperty();
                    break;
                }

                if (propertyName.EndsWith("D:" + DavProperty.CHECKEDIN)) {
                    property = new CheckedInProperty();
                    break;
                }

                if (propertyName.EndsWith("D:" + DavProperty.SUPPORTEDQUERYGRAMMARSET)) {
                    property = new SupportedQueryGrammarSetProperty();
                    break;
                }

                if (propertyName.EndsWith("D:" + DavProperty.GETCONTENTLENGTH))
                {
                    property = new ContentLenghtProperty();
                    break;
                }

                if (propertyName.EndsWith("D:" + DavProperty.ORDERINGTYPE)) {
                    property = new OrderingTypeProperty();
                    break;
                }

                property = new WebDavProperty(propertyName);
                break;
            }

            property.init(reader);
            
            return property;
        }

    }
}
