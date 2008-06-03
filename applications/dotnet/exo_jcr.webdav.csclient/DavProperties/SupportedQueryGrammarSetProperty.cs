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
