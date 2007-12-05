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
using exo_jcr.webdav.csclient.DavProperties;

/**
 * Created by The eXo Platform SARL
 * Authors : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 *         : Max Shaposhnik <uy7c@yahoo.com>
 * @version $Id:
 */

namespace exo_jcr.webdav.csclient.Response
{
    public class DavResponse
    {

        private Href href;

        private ArrayList _properties = new ArrayList();

        public DavResponse(XmlTextReader reader)
        {
            while (reader.Read())
            {
                switch (reader.NodeType)
                {
                    case XmlNodeType.Element:
                        if (reader.Name.EndsWith(DavProperty.HREF))
                        {
                            href = new Href(reader);
                            break;
                        }

                        if (reader.Name.EndsWith(DavProperty.PROPSTAT)) {
                            parsePropertyStatus(reader);
                        }

                        break;
                    
                    case XmlNodeType.EndElement:
                        if (reader.Name.EndsWith(DavProperty.RESPONSE)) {
                            return;
                        }
                        throw new XmlException("Malformed response at line " + reader.LineNumber + ":" + reader.LinePosition, null);
                        
                }
            }

        }

        private void parsePropertyStatus(XmlTextReader reader)
        {
            Hashtable curProperties = new Hashtable();

            while (reader.Read()) {

                switch(reader.NodeType) {

                    case XmlNodeType.Element:
                        if (reader.Name.EndsWith(DavProperty.PROP)) {
                            parsePropertyList(reader, curProperties);
                            break;
                        }

                        if (reader.Name.EndsWith(DavProperty.STATUS)) {
                            int status = parseStatus(reader);
                            
                            foreach (DictionaryEntry cur in curProperties) {
                                String propertyName = cur.Key.ToString();
                                WebDavProperty curProperty = (WebDavProperty)cur.Value;
                                curProperty.setStatus(status);
                                _properties.Add(curProperty);
                            }
            
                            break;
                        }

                        break;

                    case XmlNodeType.EndElement:
                        if (reader.Name.EndsWith(DavProperty.PROPSTAT)) {
                            return;
                        }
                        throw new XmlException("Malformed response at line " + reader.LineNumber + ":" + reader.LinePosition, null);
                }
            }
            return;
        }

        private void parsePropertyList(XmlTextReader reader, Hashtable propertyTable)
        {
            while (reader.Read()) {
                switch (reader.NodeType) {

                    case XmlNodeType.Element:
                        String propertyName = reader.Name;

                        WebDavProperty property = (WebDavProperty)propertyTable[propertyName];

                        if (property == null)
                        {
                            property = PropertyFactory.parseProperty(reader);
                            propertyTable.Add(propertyName, property);
                        }
                        else
                        {
                            if (!property.isMultivalue()) {
                                property.setIsMultivalue();
                            }                            
                            property.init(reader);
                        }

                        break;

                    case XmlNodeType.EndElement:
                        if (reader.Name.EndsWith(DavProperty.PROP)) {
                            return;
                        }
                        break;
                        //throw new XmlException("Malformed response at line " + reader.LineNumber + ":" + reader.LinePosition, null);

                }
            }

        }

        private int parseStatus(XmlTextReader reader) {
            int status = DavStatus.NOT_FOUND;

            while (reader.Read()) {
                switch (reader.NodeType) {
                    case XmlNodeType.Text:
                    case XmlNodeType.CDATA:
                        String []values = reader.Value.Split(' ');
                    
                        status = Convert.ToInt32(values[1]);
                        break;
                    
                    case XmlNodeType.EndElement:
                        if (reader.Name.EndsWith(DavProperty.STATUS)) {
                            return status;
                        }
                        
                        throw new XmlException("Malformed response at line " + reader.LineNumber + ":" + reader.LinePosition, null);
                }
            }

            return 0;
        }

        public Href getHref()
        {
            return href;
        }

        public ArrayList getProperties()
        {
            return _properties;
        }

        public WebDavProperty getProperty(String propertyName)
        {
            for (int i = 0; i < _properties.Count; i++)
            {
                WebDavProperty property = (WebDavProperty)_properties[i];
                if (property.getPropertyName().Equals(propertyName)) {
                    return property;
                }
            }
            return null;
        }

    }
}
