/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
using System;
using System.Collections.Generic;
using System.Text;
using System.Collections;
using System.Xml;
using exo_jcr.webdav.csclient.Request;

/**
 * Created by The eXo Platform SARL
 * Authors : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 *         : Max Shaposhnik <uy7c@yahoo.com>
 * @version $Id:
 */

namespace exo_jcr.webdav.csclient.Commands
{
    public class WebDavPropertyRequestCommand : WebDavMultistatusCommand
    {
        protected String xmlName = DavDocuments.PROPFIND;

        private ArrayList properties = new ArrayList();
        private Hashtable nameSpaces = new Hashtable();

        private int depth = 0;

        public WebDavPropertyRequestCommand(DavContext context) : base(context)
        {
        }

        public bool registerNameSpace(String propertyName)
        {
            if (propertyName.IndexOf(":") > 0)
            {
                String nameSpace = propertyName.Substring(0, propertyName.IndexOf(":"));
                if (!nameSpaces.ContainsKey(nameSpace))
                    nameSpaces.Add(nameSpace, nameSpace);

                return true;
            }
            else
            {
                return false;
            }

        }

        public void addRequiredProperty(String propertyName)
        {
            if (registerNameSpace(propertyName))
            {
                properties.Add(propertyName);
            }
            else
            {
                properties.Add(DavConstants.PREFIX + ":" + propertyName);
            }            
        }

        public void setDepth(int depth)
        {
            this.depth = depth;
        }

        public override void toXml(XmlTextWriter writer)
        {
            writer.WriteStartElement(DavConstants.PREFIX, xmlName, DavConstants.NAMESPACE);

            if (properties.Count == 0)
            {
                writer.WriteStartElement("allprop", DavConstants.NAMESPACE);
                writer.WriteEndElement();
            }
            else
            {
                writer.WriteStartElement("prop", DavConstants.NAMESPACE);
                foreach (DictionaryEntry de in nameSpaces)
                {
                    String name = de.Value.ToString();
                    writer.WriteAttributeString("xmlns:" + name, name + ":");
                }

                for (int i = 0; i < properties.Count; i++)
                {
                    String curProperty = (String)properties[i];
                    if (curProperty.IndexOf(":") < 0) {
                        writer.WriteStartElement(curProperty, DavConstants.NAMESPACE);                        
                    } else {
                        writer.WriteStartElement(curProperty);                        
                    }
                    writer.WriteEndElement();
                }

                writer.WriteEndElement();
            }

            writer.WriteEndElement();
        }

        public override int execute()
        {
            addRequestHeader(HttpHeaders.DEPTH, depth.ToString());
            return base.execute();
        }

    }
}
