using System;
using System.Collections.Generic;
using System.Text;

using exo_jcr.webdav.csclient.Request;
using exo_jcr.webdav.csclient.Commands;
using exo_jcr.webdav.csclient;

namespace exo_jcr.clienttest.Tests
{
    public class PropPatchTest
    {


        public void test()
        {

            try
            {
                DavContext context = new DavContext("localhost", 8080, "/jcr-webdav/repository", "admin", "admin");

                PropFindCommand propFind = new PropFindCommand(context);
                propFind.setResourcePath("/production/webdav.pdf");

                propFind.addRequiredProperty(DavProperty.CHECKEDIN);
                propFind.addRequiredProperty(DavProperty.CHECKEDOUT);
                propFind.addRequiredProperty(DavProperty.CHILDCOUNT);
                propFind.addRequiredProperty(DavProperty.GETCONTENTLENGTH);
                propFind.addRequiredProperty(DavProperty.GETCONTENTTYPE);
                propFind.addRequiredProperty(DavProperty.CREATIONDATE);
                propFind.addRequiredProperty(DavProperty.HASCHILDREN);
                propFind.addRequiredProperty(DavProperty.DISPLAYNAME);
                propFind.addRequiredProperty(DavProperty.ISCOLLECTION);
                propFind.addRequiredProperty(DavProperty.ISFOLDER);
                propFind.addRequiredProperty(DavProperty.ISROOT);
                propFind.addRequiredProperty(DavProperty.ISVERSIONED);
                propFind.addRequiredProperty(DavProperty.GETLASTMODIFIED);
                propFind.addRequiredProperty(DavProperty.RESOURCETYPE);
                propFind.addRequiredProperty(DavProperty.SUPPORTEDLOCK);
                propFind.addRequiredProperty(DavProperty.SUPPORTEDQUERYGRAMMARSET);
                propFind.addRequiredProperty(DavProperty.SUPPORTEDMETHODSET);
                propFind.addRequiredProperty(DavProperty.VERSIONHISTORY);
                propFind.addRequiredProperty(DavProperty.VERSIONNAME);

                propFind.addRequiredProperty("dc:title");
                propFind.addRequiredProperty("dc:creator");
                propFind.addRequiredProperty("dc:subject");
                propFind.addRequiredProperty("dc:description");
                propFind.addRequiredProperty("dc:publisher");
                propFind.addRequiredProperty("dc:contributor");
                propFind.addRequiredProperty("dc:date");
                propFind.addRequiredProperty("dc:resourceType");
                propFind.addRequiredProperty("dc:format");
                propFind.addRequiredProperty("dc:identifier");
                propFind.addRequiredProperty("dc:source");
                propFind.addRequiredProperty("dc:language");
                propFind.addRequiredProperty("dc:relation");
                propFind.addRequiredProperty("dc:coverage");
                propFind.addRequiredProperty("dc:rights");


                int status = propFind.execute();
                Console.WriteLine("STATUS: " + status.ToString());

            } catch (Exception exc) {
                Console.WriteLine("Unhandled exception. " + exc.Message + " " + exc.StackTrace);
            }



        }


    }
}
