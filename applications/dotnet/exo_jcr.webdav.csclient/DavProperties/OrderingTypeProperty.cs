using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;


namespace exo_jcr.webdav.csclient.DavProperties
{
    class OrderingTypeProperty : WebDavProperty
    {

        public OrderingTypeProperty() : base(DavProperty.ORDERINGTYPE)
        {
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
                        if (reader.Name.EndsWith("D:" + DavProperty.HREF))
                        {
                            parseHref(reader);
                        }

                        break;

                    case XmlNodeType.EndElement:
                        if (reader.Name.EndsWith(DavProperty.ORDERINGTYPE))
                        {
                            return;
                        }
                        throw new XmlException("Malformed response at line " + reader.LineNumber + ":" + reader.LinePosition, null);
                }

            }


        }

        private void parseHref(XmlTextReader reader)
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
                        break;

                    case XmlNodeType.EndElement:
                        if (reader.Name.EndsWith("D:" + DavProperty.HREF))
                        {
                            return;
                        }
                        
                        throw new XmlException("Malformed response at line " + reader.LineNumber + ":" + reader.LinePosition, null);
                }

            }

        }


    }
}
