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

/**
 * Created by The eXo Platform SARL
 * Authors : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 *         : Max Shaposhnik <uy7c@yahoo.com>
 * @version $Id:
 */

namespace exo_jcr.webdav.csclient.DavProperties
{
    public class WebDavProperty
    {

        private String propertyName;

        private String propertyValue;

        private bool _isMultivalue = false;
        private ArrayList propertyValues;

        private int status = DavStatus.NOT_FOUND;

        public WebDavProperty(String propertyName)
        {
            this.propertyName = propertyName;
        }

        public void setValue(String propertyValue)
        {
            if (_isMultivalue)
            {
                propertyValues.Add(propertyValue);
            }
            else
            {
                this.propertyValue = propertyValue;
            }            
        }

        public void setIsMultivalue()
        {
            _isMultivalue = true;
            propertyValues = new ArrayList();
            propertyValues.Add(propertyValue);
        }

        public bool isMultivalue()
        {
            return _isMultivalue;
        }

        public virtual void init(XmlTextReader reader)
        {
            if (reader.IsEmptyElement) {
                return;
            }
            while (reader.Read())
            {

                switch (reader.NodeType)
                {
                    case XmlNodeType.Element:

                        break;

                    case XmlNodeType.Text:
                        setValue(reader.Value);
                        break;

                    case XmlNodeType.EndElement:

                        Console.WriteLine("PROPERTYNAME: " + propertyName);

                        if (reader.Name.EndsWith(propertyName))
                        {
                            return;
                        }
                        throw new XmlException("Malformed response at line " + reader.LineNumber + ":" + reader.LinePosition, null);                                                
                }

            }
        }

        private void parseElement(XmlTextReader reader)
        {
            if (reader.IsEmptyElement)
            {
                return;
            }
            while (reader.Read())
            {

                switch (reader.NodeType)
                {
                    case XmlNodeType.Text:
                        setValue(reader.Value);
                        break;

                    case XmlNodeType.EndElement:
                        if (reader.Name.EndsWith(propertyName))
                        {
                            return;
                        }
                        throw new XmlException("Malformed response at line " + reader.LineNumber + ":" + reader.LinePosition, null);
                }

            }

        }

        public String getPropertyName()
        {
            return propertyName;
        }

        public void setStatus(int status)
        {
            this.status = status;
        }

        public int getStatus()
        {
            return status;
        }

        public String getTextContent()
        {
            return propertyValue;
        }

        public ArrayList getValues()
        {
            return propertyValues;
        }

    }
}
