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
