using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;


using exo_jcr.webdav.csclient.Search;
using exo_jcr.webdav.csclient.Commands;
using exo_jcr.webdav.csclient;


namespace exo_jcr.clienttest.Tests
{
    class SearchTest : AbstractTest
    {

        public void test()
        {
            try
            {
                searchSQL();
                searchXPath();

            }
            catch (Exception e)
            {
                Console.WriteLine("Exception: " + e.Message);
                Console.WriteLine(e.StackTrace);
            }
        
        }


        private void searchSQL() {
            Console.WriteLine("TEST SQL Search :");
            SearchCommand searchcomm = new SearchCommand(getContextAuthorized());
            searchcomm.setResourcePath("/production");

            String stringquery = "select * from nt:file";
            Console.WriteLine("Executing :" + stringquery);
            
            SQLQuery query = new SQLQuery();
            query.setQuery(stringquery);
            searchcomm.setQuery(query);

            if (searchcomm.execute() != DavStatus.MULTISTATUS)
            {
                Console.WriteLine("FAILURE searchSQL");
                return;
            }
            else Console.WriteLine("Ok");
            


 
        }

        private void searchXPath() {


            Console.WriteLine("TEST XPath Search :");
            SearchCommand searchcomm = new SearchCommand(getContextAuthorized());
            searchcomm.setResourcePath("/production");

            String stringquery = "//element(*, nt:file)";
            Console.WriteLine("Executing :" + stringquery);

            XPathQuery query = new XPathQuery();
            query.setQuery(stringquery);
            searchcomm.setQuery(query);
            if (searchcomm.execute() != DavStatus.MULTISTATUS)
            {
                Console.WriteLine("FAILURE searchXPath");
                return;
            }
            else Console.WriteLine("Ok");

        }


    }
}
