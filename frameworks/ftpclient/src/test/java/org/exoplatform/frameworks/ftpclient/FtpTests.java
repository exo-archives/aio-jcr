/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.ftpclient;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.exoplatform.frameworks.ftpclient.cmdtests.CDUPTest;
import org.exoplatform.frameworks.ftpclient.cmdtests.CWDTest;
import org.exoplatform.frameworks.ftpclient.cmdtests.DELETest;
import org.exoplatform.frameworks.ftpclient.cmdtests.HELPTest;
import org.exoplatform.frameworks.ftpclient.cmdtests.LISTTest;
import org.exoplatform.frameworks.ftpclient.cmdtests.MKDTest;
import org.exoplatform.frameworks.ftpclient.cmdtests.MODETest;
import org.exoplatform.frameworks.ftpclient.cmdtests.NLSTTest;
import org.exoplatform.frameworks.ftpclient.cmdtests.NOOPTest;
import org.exoplatform.frameworks.ftpclient.cmdtests.PASVTest;
import org.exoplatform.frameworks.ftpclient.cmdtests.PORTTest;
import org.exoplatform.frameworks.ftpclient.cmdtests.PWDTest;
import org.exoplatform.frameworks.ftpclient.cmdtests.QUITTest;
import org.exoplatform.frameworks.ftpclient.cmdtests.RESTTest;
import org.exoplatform.frameworks.ftpclient.cmdtests.RETRTest;
import org.exoplatform.frameworks.ftpclient.cmdtests.RMDTest;
import org.exoplatform.frameworks.ftpclient.cmdtests.RNFRTest;
import org.exoplatform.frameworks.ftpclient.cmdtests.RNTOTest;
import org.exoplatform.frameworks.ftpclient.cmdtests.SIZETest;
import org.exoplatform.frameworks.ftpclient.cmdtests.STATTest;
import org.exoplatform.frameworks.ftpclient.cmdtests.STORTest;
import org.exoplatform.frameworks.ftpclient.cmdtests.STRUTest;
import org.exoplatform.frameworks.ftpclient.cmdtests.SYSTTest;
import org.exoplatform.frameworks.ftpclient.cmdtests.TYPETest;
import org.exoplatform.frameworks.ftpclient.cmdtests.USERPASSTest;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class FtpTests extends TestCase {

  public static TestSuite suite() {
    Log log = new Log("FtpTests");
    log.info("Preparing FTP tests...");
    
    TestSuite suite = new TestSuite("jcr.ftp tests");
    
    suite.addTestSuite(NOOPTest.class);
    suite.addTestSuite(HELPTest.class);
    suite.addTestSuite(QUITTest.class);
    suite.addTestSuite(USERPASSTest.class);
    suite.addTestSuite(MODETest.class);
    suite.addTestSuite(TYPETest.class);
    suite.addTestSuite(SYSTTest.class);
    suite.addTestSuite(STRUTest.class);
    suite.addTestSuite(STATTest.class);
    suite.addTestSuite(PWDTest.class);
    suite.addTestSuite(CWDTest.class);
    suite.addTestSuite(CDUPTest.class);
    suite.addTestSuite(MKDTest.class);
    suite.addTestSuite(RMDTest.class);
    suite.addTestSuite(DELETest.class);
    suite.addTestSuite(PASVTest.class);
    suite.addTestSuite(PORTTest.class);
    suite.addTestSuite(LISTTest.class);
    suite.addTestSuite(NLSTTest.class);
    suite.addTestSuite(SIZETest.class);
    suite.addTestSuite(RNFRTest.class);
    suite.addTestSuite(RNTOTest.class);
    suite.addTestSuite(RESTTest.class);
    suite.addTestSuite(STORTest.class);
    suite.addTestSuite(RETRTest.class);

    return suite;    
  }
  
}
