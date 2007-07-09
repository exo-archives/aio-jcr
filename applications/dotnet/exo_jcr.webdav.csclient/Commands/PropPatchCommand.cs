/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
using System;
using System.Collections;
using System.Collections.Generic;
using System.Text;
using System.Xml;
using exo_jcr.webdav.csclient.Request;
using exo_jcr.webdav.csclient.Response;
using exo_jcr.webdav.csclient;

/**
 * Created by The eXo Platform SARL
 * Authors : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 *         : Max Shaposhnik <uy7c@yahoo.com>
 * @version $Id:
 */

namespace exo_jcr.webdav.csclient.Commands
{
    public class PropPatchCommand : WebDavMultistatusCommand
    {        
        private Hashtable propSet = new Hashtable();

        private ArrayList propRemove =  new ArrayList();
        private Hashtable nameSpaces =  new Hashtable();

        public PropPatchCommand(DavContext context) : base(context)
        {
            CommandName = DavCommands.PROPPATCH;
        }

        public override void toXml(XmlTextWriter writer) {            
            writer.WriteStartElement(DavConstants.PREFIX, "propertyupdate", DavConstants.NAMESPACE);
            foreach (DictionaryEntry de in nameSpaces) {
                String name = de.Value.ToString();
                writer.WriteAttributeString("xmlns:" + name, name + ":");
            }

            writer.WriteStartElement(DavConstants.REMOVE , DavConstants.NAMESPACE);
            writer.WriteStartElement(DavConstants.PROP, DavConstants.NAMESPACE);
            foreach (String a in propRemove)
            {
                writer.WriteStartElement(a);
                writer.WriteEndElement();
            }
            writer.WriteEndElement();
            writer.WriteEndElement();


            writer.WriteStartElement(DavConstants.SET, DavConstants.NAMESPACE);
            writer.WriteStartElement(DavConstants.PROP, DavConstants.NAMESPACE);
            foreach (DictionaryEntry b in propSet)
            {
                String propertyName = b.Key.ToString();
                
                ArrayList values = (ArrayList)b.Value;
                for (int i = 0; i < values.Count; i++)
                {
                    writer.WriteStartElement(propertyName);
                    writer.WriteString((String)values[i]);
                    writer.WriteEndElement();
                }
            }
            writer.WriteEndElement();
            writer.WriteEndElement();


            writer.WriteEndElement();
        
        }

        public bool registerNameSpace(String propertyName)
        {
            if (propertyName.IndexOf(":") > 0)
            {
                String nameSpace = propertyName.Substring(0, propertyName.IndexOf(":"));
                if (!nameSpaces.ContainsKey(nameSpace))
                    nameSpaces.Add(nameSpace, nameSpace);
                
                return true;
            } else {
                return false;
            }
        
        }


        public void setProperty(String propertyName, String propertyValue)
        {
            if (propSet.ContainsKey(propertyName))
            {
                ArrayList values = (ArrayList)propSet[propertyName];
                values.Add(propertyValue);
            }
            else {
                ArrayList values = new ArrayList();
                values.Add(propertyValue);

                if (registerNameSpace(propertyName))
                {
                    propSet.Add(propertyName, values);
                }
                else
                {
                    propSet.Add(DavConstants.PREFIX + ":" + propertyName, values);
                }
            }

        }

        public void removeProperty(String propertyName)
        {
            if (registerNameSpace(propertyName))
            {
                propRemove.Add(propertyName);
            }
            else
            {
                propRemove.Add(DavConstants.PREFIX + ":" + propertyName);
            }
        }

    }
}
