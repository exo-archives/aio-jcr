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
