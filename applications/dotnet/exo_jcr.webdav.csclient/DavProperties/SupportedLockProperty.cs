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

/**
 * Created by The eXo Platform SARL
 * Authors : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 *         : Max Shaposhnik <uy7c@yahoo.com>
 * @version $Id:
 */

namespace exo_jcr.webdav.csclient.DavProperties
{
    public class SupportedLockProperty : WebDavProperty
    {

        public static int SCOPE_SHARED = 0;
        public static int SCOPE_EXCLUSIVE = 1;

        public static int TYPE_WRITE = 0;
        public static int TYPE_READ = 1;

        private int lockScope = SCOPE_SHARED;
        
        private int lockType = TYPE_WRITE;

        public SupportedLockProperty() : base(DavProperty.SUPPORTEDLOCK)
        {
        }

        public int getLockScope()
        {
            return lockScope;
        }

        public int getLockType()
        {
            return lockType;
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

                        if (reader.Name.EndsWith("D:" + DavProperty.LOCKENTRY))
                        {
                            parseLockEntry(reader);
                        }

                        break;

                    case XmlNodeType.EndElement:
                        if (reader.Name.EndsWith(DavProperty.SUPPORTEDLOCK))
                        {
                            return;
                        }
                        throw new XmlException("Malformed response at line " + reader.LineNumber + ":" + reader.LinePosition, null);
                }

            }
        }

        private void parseLockEntry(XmlTextReader reader)
        {
            while (reader.Read()) {
                switch(reader.NodeType) {
                    case XmlNodeType.Element:
                        if (reader.Name.EndsWith("D:" + DavProperty.LOCKSCOPE)) {
                            parseLockScope(reader);
                            break;
                        }
                        if (reader.Name.EndsWith("D:" + DavProperty.LOCKTYPE)) {
                            parseLockType(reader);
                            break;
                        }                        
                        break;

                    case XmlNodeType.EndElement:
                        if (reader.Name.EndsWith(DavProperty.LOCKENTRY))
                        {
                            return;
                        }
                        throw new XmlException("Malformed response at line " + reader.LineNumber + ":" + reader.LinePosition, null);
                }
            }
        }

        private void parseLockScope(XmlTextReader reader)
        {
            while (reader.Read()) {
                switch(reader.NodeType) {
                    case XmlNodeType.Element:
                        if (reader.Name.EndsWith("D:" + DavProperty.EXCLUSIVE)) {
                            lockScope = SCOPE_EXCLUSIVE;
                            break;
                        }

                        if (reader.Name.EndsWith("D:" + DavProperty.SHARED)) {
                            lockScope = SCOPE_SHARED;
                            break;
                        }
                        
                        break;

                    case XmlNodeType.EndElement:
                        if (reader.Name.EndsWith(DavProperty.LOCKSCOPE))
                        {
                            return;
                        }
                        throw new XmlException("Malformed response at line " + reader.LineNumber + ":" + reader.LinePosition, null);                        
                }
            }
        }

        private void parseLockType(XmlTextReader reader)
        {
            while (reader.Read()) {
                switch(reader.NodeType) {
                    case XmlNodeType.Element:
                        if (reader.Name.EndsWith("D:" + DavProperty.WRITE))
                        {
                            lockType = TYPE_WRITE;
                            break;
                        }

                        if (reader.Name.EndsWith("D:" + DavProperty.READ))
                        {
                            lockType = TYPE_READ;
                            break;
                        }
                        
                        break;

                    case XmlNodeType.EndElement:
                        if (reader.Name.EndsWith(DavProperty.LOCKTYPE))
                        {
                            return;
                        }
                        throw new XmlException("Malformed response at line " + reader.LineNumber + ":" + reader.LinePosition, null);
                }
            }
        }

    }
}
