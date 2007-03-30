/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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

namespace exo_jcr.webdav.csclient.Search
{
    public class SQLQuery : DavQuery
    {
        private String query;

        public SQLQuery()
        {
        }

        public void setQuery(String query)
        {
            this.query = query;
        }

        public void toXml(XmlTextWriter writer)
        {
            writer.WriteStartElement(Constants.SQL_PREFIX, "sql",Constants.SQL_NAMESPACE);
            writer.WriteValue(query);
            writer.WriteEndElement();
        }

    }
}
