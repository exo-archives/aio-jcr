using System;
using System.Collections.Generic;
using System.Text;

using exo_jcr.webdav.csclient;
using exo_jcr.webdav.csclient.Request;

namespace exo_jcr.clienttest.Tests
{
    abstract class AbstractTest
    {

        public static String HOST = "localhost";
        public static int PORT = 8080;
        public static String SERVLET_PATh = "/jcr-webdav/repository";

        public static String USER = "admin";
        public static String PASS = "admin";

        public DavContext getContext()
        {
            return new DavContext(HOST, PORT, SERVLET_PATh);
        }

        public DavContext getContextAuthorized()
        {
            return new DavContext(HOST, PORT, SERVLET_PATh, USER, PASS);
        }

    }
}
