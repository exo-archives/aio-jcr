/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
using System;
using System.Collections.Generic;
using System.Collections;
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
    public abstract class AbstractQuery : DavQuery
    {

        protected ArrayList properties = new ArrayList();

        public void addSelectProperty(String propertyName)
        {
            properties.Add(propertyName);
        }

        public abstract void toXml(XmlTextWriter writer);

    }
}
