/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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
