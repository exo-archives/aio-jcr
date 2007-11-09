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

        private Hashtable namespacedProperties = new Hashtable();

        private Hashtable prefixes = new Hashtable();

        private int depth = 0;

        public WebDavPropertyRequestCommand(DavContext context) : base(context)
        {
        }

        public void addRequiredProperty(String propertyName)
        {
            addRequiredProperty("D:" + propertyName, "D", "DAV:");
        }

        public void addRequiredProperty(String prefixedName, String prefix, String nameSpace)
        {
            String propertyName = prefixedName.Substring(((String)(prefix + ":")).Length);

            if (!prefixes.Contains(nameSpace)) {
                prefixes.Add(nameSpace, prefix);
            }

            ArrayList properties = null;
            if (namespacedProperties.Contains(nameSpace))
            {
                properties = (ArrayList)namespacedProperties[nameSpace];
            }
            else
            {
                properties = new ArrayList();
                namespacedProperties.Add(nameSpace, properties);
            }

            properties.Add(propertyName);
        }

        public void setDepth(int depth)
        {
            this.depth = depth;
        }

        public override void toXml(XmlTextWriter writer)
        {

            writer.WriteStartElement(DavConstants.PREFIX, xmlName, DavConstants.NAMESPACE);

            if (namespacedProperties.Count == 0)
            {
                writer.WriteStartElement("allprop", DavConstants.NAMESPACE);
                writer.WriteEndElement();
            }
            else
            {

                writer.WriteStartElement("prop", DavConstants.NAMESPACE);

                foreach (DictionaryEntry entry in namespacedProperties) {
                    String nameSpace = (String)entry.Key;

                    String prefix = (String)prefixes[nameSpace];

                    ArrayList properties = (ArrayList)entry.Value;

                    for (int i = 0; i < properties.Count; i++ )
                    {
                        String propertyName = (String)properties[i];
                        if ("DAV:".Equals(nameSpace))
                        {
                            writer.WriteStartElement(prefix, propertyName, nameSpace);
                            writer.WriteEndElement();
                        }
                        else
                        {
                            writer.WriteStartElement(prefix, propertyName, nameSpace);

                            writer.WriteEndElement();
                        }
                    }

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
