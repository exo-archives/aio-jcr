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

/**
 * Created by The eXo Platform SARL
 * Authors : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 *         : Max Shaposhnik <uy7c@yahoo.com>
 * @version $Id:
 */

namespace exo_jcr.webdav.csclient
{

    public class DavProperty {

        public static String DISPLAYNAME = "displayname";
        public static String GETCONTENTTYPE = "getcontenttype";
        public static String GETCONTENTLENGTH = "getcontentlength";
        public static String GETLASTMODIFIED = "getlastmodified";
        public static String RESOURCETYPE = "resourcetype";
        public static String SUPPORTEDLOCK = "supportedlock";
        public static String LOCKENTRY = "lockentry";
        public static String LOCKSCOPE = "lockscope";
        public static String EXCLUSIVE = "exclusive"; 
        public static String SHARED = "shared";
        public static String LOCKTYPE = "locktype";
        public static String WRITE = "write";
        public static String READ = "read";
        public static String CREATIONDATE = "creationdate";
        public static String COLLECTION = "collection";
        public static String MULTISTATUS = "multistatus";
        public static String RESPONSE = "response";
        public static String HREF = "href";
        public static String PROPSTAT = "propstat";
        public static String PROP = "prop";
        public static String STATUS = "status";
        public static String CHECKEDIN = "checked-in";
        public static String CHECKEDOUT = "checked-out";
        public static String CHILDCOUNT = "childcount";
        public static String ISCOLLECTION = "iscollection";
        public static String ISFOLDER = "isfolder";
        public static String ISROOT = "isroot";
        public static String ISVERSIONED = "isversioned";
        public static String SUPPORTEDMETHODSET = "supported-method-set";
        public static String ORDERINGTYPE = "ordering-type";
        
        public static String SUPPORTEDQUERYGRAMMARSET = "supported-query-grammar-set";
        public static String SUPPORTEDQUERYGRAMMAR = "supported-query-grammar";
        public static String GRAMMAR = "grammar";

        public static String VERSIONHISTORY = "version-history";
        public static String VERSIONNAME = "version-name";
        public static String HASCHILDREN = "haschildren";
        public static String CREATORDISPLAYNAME = "creator-displayname";

        public static String BASICSEARCH = "basicsearch";

    }

}
