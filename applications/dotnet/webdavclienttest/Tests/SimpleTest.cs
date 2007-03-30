using System;
using System.Collections.Generic;
using System.Text;

using exo_jcr.webdav.csclient;
using exo_jcr.webdav.csclient.Commands;

namespace exo_jcr.clienttest.Tests
{
    class SimpleTest : AbstractTest
    {

        public void test()
        {
            Console.WriteLine("Simple test...");

            try
            {
                testMkCol();
                testPropFind();
                testCopy();
                testMove();
            } catch (Exception exc) {
                Console.WriteLine("Error: " + exc.Message);
            }

        }

        #region testMkCol
        private void testMkCol()
        {
            Console.WriteLine("<<< testMkCol...");

            String srcPath = "/production/test_folder_testMkCol";

            MkColCommand mkCol = new MkColCommand(getContextAuthorized());
            mkCol.setResourcePath(srcPath);

            if (mkCol.execute() != DavStatus.CREATED) {
                Console.WriteLine(">>> FAILURE!!! mkCol.execute()");
                return;
            }


            DeleteCommand delete = new DeleteCommand(getContextAuthorized());
            delete.setResourcePath(srcPath);

            if (delete.execute() != DavStatus.NO_CONTENT) {
                Console.WriteLine(">>> FAILURE!!! delete.execute()");
            }

            Console.WriteLine("<<< done.");
        }
        #endregion

        #region testPropFind
        private void testPropFind()
        {
            Console.WriteLine("<<< testPropFind...");

            String srcPath = "/production/test_folder_testPropFind";

            MkColCommand mkCol = new MkColCommand(getContextAuthorized());
            mkCol.setResourcePath(srcPath);

            if (mkCol.execute() != DavStatus.CREATED) {
                Console.WriteLine(">>> FAILURE!!! mkCol.execute()");
                return;
            }

            PropFindCommand propFind = new PropFindCommand(getContextAuthorized());
            propFind.setResourcePath(srcPath);

            if (propFind.execute() != DavStatus.MULTISTATUS) {
                Console.WriteLine(">>> FAILURE!!! propFind.execute()");
                return;
            }

            DeleteCommand delete = new DeleteCommand(getContextAuthorized());
            delete.setResourcePath(srcPath);

            if (delete.execute() != DavStatus.NO_CONTENT) {
                Console.WriteLine(">>> FAILURE!!! delete.execute()");
            }

            Console.WriteLine("<<< done.");
        }
        #endregion

        #region testCopy
        private void testCopy()
        {
            Console.WriteLine("<<< testCopy...");

            String srcName = "/production/test_folder_src_testCopy";
            String destName = "/production/test_folder_dest_testCopy";

            MkColCommand mkCol = new MkColCommand(getContextAuthorized());
            mkCol.setResourcePath(srcName);

            if (mkCol.execute() != DavStatus.CREATED)
            {
                Console.WriteLine(">>> FAILURE!!! mkCol.execute()");
                return;
            }

            CopyCommand copy = new CopyCommand(getContextAuthorized());
            copy.setResourcePath(srcName);
            copy.setDestinationPath(destName);

            if (copy.execute() != DavStatus.CREATED)
            {
                Console.WriteLine(">>> FAILURE!!! copy.execute()");
                return;
            }

            {
                PropFindCommand propFind = new PropFindCommand(getContextAuthorized());
                propFind.setResourcePath(srcName);

                if (propFind.execute() != DavStatus.MULTISTATUS)
                {
                    Console.WriteLine(">>> FAILURE!!! propFind.execute()");
                    return;
                }
            }

            {
                PropFindCommand propFind = new PropFindCommand(getContextAuthorized());
                propFind.setResourcePath(destName);

                if (propFind.execute() != DavStatus.MULTISTATUS)
                {
                    Console.WriteLine(">>> FAILURE!!! propFind.execute()");
                    return;
                }
            }

            {
                DeleteCommand delete = new DeleteCommand(getContextAuthorized());
                delete.setResourcePath(srcName);

                if (delete.execute() != DavStatus.NO_CONTENT)
                {
                    Console.WriteLine(">>> FAILURE!!! delete.execute()");
                    return;
                }
            }

            {
                DeleteCommand delete = new DeleteCommand(getContextAuthorized());
                delete.setResourcePath(destName);

                if (delete.execute() != DavStatus.NO_CONTENT)
                {
                    Console.WriteLine(">>> FAILURE!!! delete.execute()");
                    return;
                }
            }

            Console.WriteLine("<<< done.");
        }
        #endregion

        #region testMove
        private void testMove()
        {
            Console.WriteLine("<<< testMove...");

            String srcName = "/production/test_folder_src_testCopy";
            String destName = "/production/test_folder_dest_testCopy";

            MkColCommand mkCol = new MkColCommand(getContextAuthorized());
            mkCol.setResourcePath(srcName);

            if (mkCol.execute() != DavStatus.CREATED)
            {
                Console.WriteLine(">>> FAILURE!!! mkCol.execute()");
                return;
            }

            MoveCommand move = new MoveCommand(getContextAuthorized());
            move.setResourcePath(srcName);
            move.setDestinationPath(destName);

            if (move.execute() != DavStatus.CREATED)
            {
                Console.WriteLine(">>> FAILURE!!! move.execute()");
                return;
            }

            {
                PropFindCommand propFind = new PropFindCommand(getContextAuthorized());
                propFind.setResourcePath(srcName);

                if (propFind.execute() != DavStatus.NOT_FOUND)
                {
                    Console.WriteLine(">>> FAILURE!!! propFind.execute()");
                    return;
                }
            }

            {
                PropFindCommand propFind = new PropFindCommand(getContextAuthorized());
                propFind.setResourcePath(destName);

                if (propFind.execute() != DavStatus.MULTISTATUS) {
                    Console.WriteLine(">>> FAILURE!!! propFind.execute()");
                    return;
                }
            }

            {
                DeleteCommand delete = new DeleteCommand(getContextAuthorized());
                delete.setResourcePath(destName);

                if (delete.execute() != DavStatus.NO_CONTENT)
                {
                    Console.WriteLine(">>> FAILURE!!! delete.execute()");
                    return;
                }
            }

            Console.WriteLine("<<< done.");
        }
        #endregion

    }
}
