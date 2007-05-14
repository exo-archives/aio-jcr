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

                property = new WebDavProperty(propertyName);
                break;
            }

            property.init(reader);
            
            return property;
        }

    }
}
