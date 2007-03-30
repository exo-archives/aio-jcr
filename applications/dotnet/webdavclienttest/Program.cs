using System;
using System.Collections.Generic;
using System.Text;
using System.Collections;
using System.Windows.Forms;


using exo_jcr.webdav.csclient;
using exo_jcr.webdav.csclient.Commands;
using exo_jcr.webdav.csclient.Response;
using exo_jcr.webdav.csclient.DavProperties;
using exo_jcr.webdav.csclient.Search;

using exo_jcr.webdav.csclient.Request;
using exo_jcr.clienttest.Tests;

namespace exo_jcr.clienttest
{
    class Program {
        /*
         * 
         *TESTS
         * 
         */

       static void testPropFind() {
            DavContext context = new DavContext("localhost", 8080,"/jcr-webdav/repository", "admin", "admin");

            PropFindCommand propFind = new PropFindCommand(context);
            propFind.setResourcePath("/production/replication.pdf");

            propFind.addRequiredProperty(DavProperty.DISPLAYNAME);

            propFind.addRequiredProperty("dc:creator");

            propFind.addRequiredProperty(DavProperty.SUPPORTEDLOCK);

            int status = propFind.execute();
            Console.WriteLine("STATUS: " + status.ToString());

            byte[] bb = propFind.getResponseBody();
            String ss = System.Text.Encoding.UTF8.GetString(bb);
            Console.WriteLine(ss);

            if (status == DavStatus.MULTISTATUS) {
                Multistatus multistatus = propFind.getMultistatus();
                ArrayList responses = multistatus.getResponses();

                for (int i = 0; i < responses.Count; i++)
                {
                    DavResponse response = (DavResponse)responses[i];
                    Console.WriteLine("HREF: [" + response.getHref().getHref() + "]");

                    {
                        WebDavProperty property = response.getProperty(DavProperty.DISPLAYNAME);
                        if (property != null)
                        {
                            Console.WriteLine("DISPLAY NAME PRESENT : " + ((DisplayNameProperty)property).getDisplayName() + " STATUS: " + property.getStatus());
                        }
                    }

                    {
                        WebDavProperty property = response.getProperty(DavProperty.RESOURCETYPE);
                        if (property != null)
                        {
                            Console.WriteLine("RESOURCE TYPE PRESENT : " + ((ResourceTypeProperty)property).getResourceType().ToString() + " STATUS: " + property.getStatus());
                        }
                    }

                    {
                        WebDavProperty property = response.getProperty(DavProperty.GETCONTENTTYPE);
                        if (property != null)
                        {
                            Console.WriteLine("CONTENT TYPE PRESENT : " + ((ContentTypeProperty)property).getContentType() + " STATUS: " + property.getStatus());
                        }
                    }

                    {
                        WebDavProperty property = response.getProperty(DavProperty.GETLASTMODIFIED);
                        if (property != null)
                        {
                            Console.WriteLine("CONTENT TYPE PRESENT : " + ((LastModifiedProperty)property).getLastModified() + " STATUS: " + property.getStatus());
                        }
                    }

                    {
                        WebDavProperty property = response.getProperty(DavProperty.SUPPORTEDLOCK);
                        if (property != null)
                        {
                            Console.WriteLine("SUPPORTED LOCK PRESENT. STATUS: " + property.getStatus());
                            Console.WriteLine("LOCK SCOPE: " + ((SupportedLockProperty)property).getLockScope().ToString());
                            Console.WriteLine("LOCK TYPE: " + ((SupportedLockProperty)property).getLockType().ToString());
                        }
                    }

                }

                Console.WriteLine("RESPONSES: " + responses.Count.ToString());
            }

        }


        static void testDelete()
        {
            DavContext context = new DavContext("localhost", 8080, "/jcr-webdav/repository");

            DeleteCommand del = new DeleteCommand(context);
            del.setResourcePath("/production/1/1.txt");
            int status = del.execute();
            Console.WriteLine("STATUS: " + status.ToString());
        }

        static void testGet()
        {
            DavContext context = new DavContext("localhost", 8080, "/jcr-webdav/repository");

            GetCommand get = new GetCommand(context);
            get.setResourcePath("/production/1/1.zip");
            int status = get.execute();
            Console.WriteLine("STATUS: " + status.ToString());
            Console.ReadKey();
        }

        static void testPut()
        {
            DavContext context = new DavContext("localhost", 8080, "/jcr-webdav/repository");

            PutCommand put = new PutCommand(context);
            put.setResourcePath("/production/1/1.pdf");
            byte[] data = put.getBytes("HELLO DE UR3CMA MAX");
            put.setRequestBody(data);
            int status = put.execute();
            Console.WriteLine("STATUS: " + status.ToString());
            Console.ReadKey();
        }

        static string token = "";
        static void testLock()
        {
            DavContext context = new DavContext("localhost", 8080, "/jcr-webdav/repository");

            LockCommand lockcomm = new LockCommand(context);
            lockcomm.setResourcePath("/production/1/2.txt");
            int status = lockcomm.execute();
            token = lockcomm.getLockToken();
            
            Console.WriteLine("STATUS: " + status.ToString());
            Console.WriteLine("TOKEN: " + token);
            Console.ReadKey();
        }


        static void testUnLock()
        {
            DavContext context = new DavContext("localhost", 8080, "/jcr-webdav/repository");

            UnLockCommand unlockcomm = new UnLockCommand(context);
            unlockcomm.setResourcePath("/production/1/2.txt");
            unlockcomm.setLockToken(token);
            int status = unlockcomm.execute();
            Console.WriteLine("STATUS: " + status.ToString());
            //Console.WriteLine("TOKEN: " + token);
            Console.ReadKey();
        }

        static void testVC()
        {
            DavContext context = new DavContext("localhost", 8080, "/jcr-webdav/repository");

            VersionControlCommand vccomm = new VersionControlCommand(context);
            vccomm.setResourcePath("/production/1/1.zip");
            int status = vccomm.execute();
            Console.WriteLine("STATUS: " + status.ToString());
            Console.ReadKey();
        }

        static void testCheckIn()
        {
            DavContext context = new DavContext("localhost", 8080, "/jcr-webdav/repository");

            CheckInCommand ChIncomm = new CheckInCommand(context);
            ChIncomm.setResourcePath("/production/1/1.zip");
            int status = ChIncomm.execute();
            Console.WriteLine("STATUS: " + status.ToString());
            Console.ReadKey();
        }

        static void testCheckOut()
        {
            DavContext context = new DavContext("localhost", 8080, "/jcr-webdav/repository");

            CheckOutCommand ChOutcomm = new CheckOutCommand(context);
            ChOutcomm.setResourcePath("/production/1/1.zip");
            int status = ChOutcomm.execute();
            Console.WriteLine("STATUS: " + status.ToString());
            Console.ReadKey();
        }

        static void testUnCheckOut()
        {
            DavContext context = new DavContext("localhost", 8080, "/jcr-webdav/repository");

            UnCheckOutCommand UnChOutcomm = new UnCheckOutCommand(context);
            UnChOutcomm.setResourcePath("/production/1/1.zip");
            int status = UnChOutcomm.execute();
            Console.WriteLine("STATUS: " + status.ToString());
            Console.ReadKey();
        }


        static void testReport()
        {
            DavContext context = new DavContext("localhost", 8080, "/jcr-webdav/repository");

            ReportCommand reportcomm = new ReportCommand(context);
            reportcomm.setResourcePath("/production/preved.jpg");
            int status = reportcomm.execute();
            Console.WriteLine("STATUS: " + status.ToString());
            Console.ReadKey();
        }

        static void testSearch()
        {
            DavContext context = new DavContext("localhost", 8080, "/jcr-webdav/repository");

            SearchCommand searchcomm = new SearchCommand(context);
            searchcomm.setResourcePath("/production");


            //SQLQuery query = new SQLQuery();
            //query.setQuery("select * from nt:file");

            XPathQuery query = new XPathQuery();
            query.setQuery("//element(*, nt:file)");


            searchcomm.setQuery(query);
            int status = searchcomm.execute();
            Console.WriteLine("STATUS: " + status.ToString());
            


            if (status == DavStatus.MULTISTATUS)
            {
                Multistatus multistatus = searchcomm.getMultistatus();
                ArrayList responses = multistatus.getResponses();

                for (int i = 0; i < responses.Count; i++)
                {
                    DavResponse response = (DavResponse)responses[i];
                    Console.WriteLine("HREF: [" + response.getHref().getHref() + "]");
                   
                }

            }


            Console.ReadLine();

        }



        static void testPropPatch()
        {
            DavContext context = new DavContext("localhost", 8080, "/jcr-webdav/repository", "admin", "admin");

            PropPatchCommand ppathccomm = new PropPatchCommand(context);
            ppathccomm.setResourcePath("/production/replication.pdf");

            ppathccomm.setProperty("dc:creator", "1111");
            ppathccomm.setProperty("dc:creator", "2222");
            ppathccomm.setProperty("dc:creator", "333333333333");
            ppathccomm.setProperty(DavProperty.DISPLAYNAME, "new");

            //ppathccomm.removeProperty("max:name");
            //ppathccomm.removeProperty("vetal:name");
            //ppathccomm.removeProperty(DavProperty.GETLASTMODIFIED);

            int status = ppathccomm.execute();
            Console.WriteLine("STATUS: " + status.ToString());

            byte[] bb = ppathccomm.getResponseBody();
            String ss = System.Text.Encoding.UTF8.GetString(bb);
            Console.WriteLine(ss);

            if (status == DavStatus.MULTISTATUS)
            {
                Multistatus multistatus = ppathccomm.getMultistatus();
                ArrayList responses = multistatus.getResponses();

                for (int i = 0; i < responses.Count; i++)
                {
                    DavResponse response = (DavResponse)responses[i];
                    Console.WriteLine("HREF: [" + response.getHref().getHref() + "]");

                    ArrayList properties = response.getProperties();

                    Console.WriteLine("PROPCOUNT: " + properties.Count.ToString());

                    for (int pi = 0; pi < properties.Count; pi++)
                    {
                        WebDavProperty property = (WebDavProperty)properties[pi];
                        Console.WriteLine("PROPERTY[" + property.getPropertyName() + "]");
                        if (property.isMultivalue())
                        {
                            ArrayList values = property.getValues();
                            foreach (String sv in values) {
                                Console.WriteLine("v: " + sv);
                            }
                            Console.WriteLine("--------------");

                        }
                        else
                        {
                            Console.WriteLine("VALUE: " + property.getTextContent());
                        }
                    }

                }

            }
            Console.ReadLine();
        }

        static void ttt() {
            DavContext context = new DavContext("localhost", 8080, "/jcr-webdav/repository");

            MkColCommand mkCol = new MkColCommand(context);
            mkCol.setResourcePath("/production/testproppatch");
            Console.WriteLine("STATUS: " + mkCol.execute().ToString());

            PropPatchCommand ppathccomm = new PropPatchCommand(context);
            ppathccomm.setResourcePath("/production/testproppatch");

            ppathccomm.setProperty("kfx:ooooooo", "1111");
            ppathccomm.setProperty("vetal:name", "2222");
            ppathccomm.setProperty(DavProperty.DISPLAYNAME, "new");

            ppathccomm.removeProperty("max:name");
            ppathccomm.removeProperty("vetal:name");
            ppathccomm.removeProperty(DavProperty.GETLASTMODIFIED);

            int status = ppathccomm.execute();
            Console.WriteLine("STATUS: " + status.ToString());
        }

        static void testNodeType()
        {
            DavContext context = new DavContext("localhost", 8080, "/jcr-webdav/repository", "admin", "admin");

            //MkColCommand mkCol = new MkColCommand(context);
            //mkCol.setResourcePath("/production/test_123_123321");
            //mkCol.setNodeType("webdav:folder");
            //Console.WriteLine("MKCOL STATUS: " + mkCol.execute().ToString());

            PutCommand put = new PutCommand(context);
            put.setResourcePath("/production/test_file.txt");
            put.setRequestBody("TEST FILE CONTENT");
            put.setNodeType("webdav:file");
            Console.WriteLine("PUT STATUS: " + put.execute().ToString());
        }



        static void testHead()
        {
            DavContext context = new DavContext("localhost", 8080, "/jcr-webdav/repository", "admin", "admin");
            HeadCommand headcomm = new HeadCommand(context);
            int status = headcomm.execute();
            Console.WriteLine("STATUS: " + status.ToString());
            Console.ReadKey();
        }


        static void Main(string[] args) {

            //new SimpleTest().test();
            //new LockingTest().test();
            //new VersionTest().test();
            new PropPatchTest().test();

            //testPropFind();            
            //testMkCol();
            //testGet();
            //testPut();
            //ttt();
            //testNodeType();
            //testLock();
            //testDelete();
            //testUnLock();
            //testMove();
            //testCopy();
            //testReport();
            //testSearch();
            //new SearchTest().test();
            //Console.ReadLine();
            //testPropPatch();
            //testHead();


        }

    }
}
