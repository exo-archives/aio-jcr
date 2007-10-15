using System;
using System.Collections;
using System.Collections.Generic;
using System.Text;
using System.Xml;

namespace exo_jcr.webdav.csclient.DavProperties
{
    public class SupportedQueryGrammarSetProperty : WebDavProperty
    {

        private static String SEARCH_BASICSEARCH = "basicsearch";

        private static String SEARCH_SQL = "sql";

        private static String SEARCH_XPATH = "xpath";

        private ArrayList searchTypes = new ArrayList();

        public SupportedQueryGrammarSetProperty() : base(DavProperty.SUPPORTEDQUERYGRAMMARSET)
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

                        if (reader.Name.EndsWith("D:" + DavProperty.SUPPORTEDQUERYGRAMMAR))
                        {
                            parseSupportedQueryGrammar(reader);
                        }

                        break;

                    case XmlNodeType.EndElement:
                        if (reader.Name.EndsWith(DavProperty.SUPPORTEDQUERYGRAMMARSET))
                        {
                            return;
                        }
                        throw new XmlException("Malformed response at line " + reader.LineNumber + ":" + reader.LinePosition, null);
                }

            }
        }

        private void parseSupportedQueryGrammar(XmlTextReader reader)
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

                        if (reader.Name.EndsWith("D:" + DavProperty.GRAMMAR))
                        {
                            parseGrammar(reader);
                        }

                        break;

                    case XmlNodeType.EndElement:
                        if (reader.Name.EndsWith(DavProperty.SUPPORTEDQUERYGRAMMAR))
                        {
                            return;
                        }
                        throw new XmlException("Malformed response at line " + reader.LineNumber + ":" + reader.LinePosition, null);
                }

            }
        }


        private void parseGrammar(XmlTextReader reader)
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
                        {
                            if (reader.Name.EndsWith("D:" + DavProperty.BASICSEARCH))
                            {
                                searchTypes.Add(SEARCH_BASICSEARCH);
                                break;
                            }

                            if (reader.Name.EndsWith("exo:sql"))
                            {
                                searchTypes.Add(SEARCH_SQL);
                                break;
                            }

                            if (reader.Name.EndsWith("exo:xpath"))
                            {
                                searchTypes.Add(SEARCH_XPATH);
                                break;
                            }
                        }
                        break;

                    case XmlNodeType.EndElement:
                        if (reader.Name.EndsWith(DavProperty.GRAMMAR))
                        {
                            return;
                        }
                        throw new XmlException("Malformed response at line " + reader.LineNumber + ":" + reader.LinePosition, null);
                }

            }
        }

        public ArrayList getSearchTypes()
        {
            return searchTypes;
        }

    }
}
