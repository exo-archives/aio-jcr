using System;
using System.Collections.Generic;
using System.Text;

using exo_jcr.webdav.csclient.Commands;
using exo_jcr.webdav.csclient;

namespace exo_jcr.clienttest.Tests
{
    class LockingTest : AbstractTest
    {

        public void test()
        {
            try
            {
                testSimpleLockUnLock();
            } catch (Exception exc)
            {
                Console.WriteLine("Exception: " + exc.Message);
            }
        }

        private void testSimpleLockUnLock()
        {
            Console.WriteLine("<<< testSimpleLockUnLock...");

            String srcName = "/production/test_folder_src_testSimpleLockUnLock";

            MkColCommand mkCol = new MkColCommand(getContextAuthorized());
            mkCol.setResourcePath(srcName);

            if (mkCol.execute() != DavStatus.CREATED) {
                Console.WriteLine("<<< FAULIRE!!! mkCol.execute()");
                return;
            }

            LockCommand lockCommand = new LockCommand(getContextAuthorized());
            lockCommand.setResourcePath(srcName);

            if (lockCommand.execute() != DavStatus.OK) {
                Console.WriteLine("<<< FAILURE!!! lockCommand.execute()");
                return;
            }

            String lockToken = lockCommand.getLockToken();            

            UnLockCommand unLock = new UnLockCommand(getContextAuthorized());
            unLock.setResourcePath(srcName);
            unLock.setLockToken(lockToken);

            if (unLock.execute() != DavStatus.NO_CONTENT) {
                Console.WriteLine("<<< FAULIRE!!! unLock.execute()");
                return;
            }

            DeleteCommand delete = new DeleteCommand(getContextAuthorized());
            delete.setResourcePath(srcName);
            
            if (delete.execute() != DavStatus.NO_CONTENT) {
                Console.WriteLine("<<< FAILURE!!! delete.execute()");
                return;
            }

            Console.WriteLine("<<< done.");
        }


    }
}
