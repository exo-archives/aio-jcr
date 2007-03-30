/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.completed;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.commands.DavSearch;
import org.exoplatform.frameworks.davclient.completed.search.TestUtils;
import org.exoplatform.frameworks.davclient.documents.Multistatus;
import org.exoplatform.frameworks.davclient.search.SQLQuery;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class DevTest extends TestCase {

  private static Log log = ExoLogger.getLogger("jcr.DevTest");

  public void testSQLForFolder() throws Exception {
            log.info("testSQLForFolder...");

      DavSearch davSearch = new DavSearch(DavLocationConst.getLocationAuthorized());
      davSearch.setResourcePath("/production");

      SQLQuery query = new SQLQuery();

      String q = "select * from nt:folder "
              + "where jcr:path like '/123123[%]/%/%/' "
              + "and not jcr:path like '/123123[%]/%/%/%/'";
              //+ " and not jcr:path like '/123123[%]/%/%/%/%/' ";

      query.setQuery(q);

      davSearch.setQuery(query);

      assertEquals(Const.HttpStatus.MULTISTATUS, davSearch.execute());

      TestUtils.showMultistatus((Multistatus)davSearch.getMultistatus());

      String reply = new String(davSearch.getResponseDataBuffer());
      //log.info("REPLY: \r\n" + reply + "\r\n");

            log.info("done.");
          }

}
