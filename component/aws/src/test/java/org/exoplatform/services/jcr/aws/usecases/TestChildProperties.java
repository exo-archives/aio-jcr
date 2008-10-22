/**
 * 
 */
/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.jcr.aws.usecases;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.aws.storage.sdb.SDBConstants;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.log.ExoLogger;

import com.amazonaws.sdb.AmazonSimpleDB;
import com.amazonaws.sdb.AmazonSimpleDBClient;
import com.amazonaws.sdb.AmazonSimpleDBConfig;
import com.amazonaws.sdb.AmazonSimpleDBException;
import com.amazonaws.sdb.model.Attribute;
import com.amazonaws.sdb.model.GetAttributesRequest;
import com.amazonaws.sdb.model.GetAttributesResponse;
import com.amazonaws.sdb.model.Item;
import com.amazonaws.sdb.model.QueryWithAttributesRequest;
import com.amazonaws.sdb.model.QueryWithAttributesResponse;
import com.amazonaws.sdb.model.QueryWithAttributesResult;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 22.10.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: TestChildProperties.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public class TestChildProperties extends TestCase {

  /**
   * Test logger.
   */
  protected static final Log    LOG             = ExoLogger.getLogger("jcr.TestChildProperties");

  /**
   * SDB domain name.
   */
  protected static final String SDB_DOMAIN_NAME = "ecm-demo-system";

  /**
   * SimpleDB client (Amazon stuff). For checks.
   */
  protected AmazonSimpleDB      sdbClient;

  /**
   * jcrRoot.
   */
  protected NodeData            jcrRoot;

  /**
   * Read SDB Item.
   * 
   * @param service
   *          SDB service
   * @param domainName
   *          SDB domain name
   * @param item
   *          item name
   * @return GetAttributesResponse
   * @throws AmazonSimpleDBException
   *           in case of SDB error
   */
  protected GetAttributesResponse readItem(AmazonSimpleDB service, String domainName, String item) throws AmazonSimpleDBException {
    GetAttributesRequest request = new GetAttributesRequest().withDomainName(domainName)
                                                             .withItemName(item);

    return service.getAttributes(request);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void setUp() throws Exception {

    String myAccessKey = System.getProperty("exo.aws.access.key");
    String mySecretKey = System.getProperty("exo.aws.secret.key");

    if (myAccessKey == null || myAccessKey.length() <= 0)
      fail("Access key required.");

    if (mySecretKey == null || mySecretKey.length() <= 0)
      fail("Secret key required.");

    // Amazon SDB client
    try {
      AmazonSimpleDBConfig config = new AmazonSimpleDBConfig();
      config.setSignatureVersion("0");

      sdbClient = new AmazonSimpleDBClient(myAccessKey, mySecretKey, config);
    } catch (Throwable e) {
      LOG.error("setup error", e);
      fail(e.getMessage());
    }
  }

  /**
   * testSelect.
   * 
   * @throws Exception
   */
  public void testSelect() throws Exception {
    String queryPattern = "['PID' = '%s'] intersection ['IClass' = '" + 2 + "'] intersection ['ID' != 'D']";
    //  
    String qpid = "252c58a90afcef204794f2e8c1d9a0d7";

    String query = String.format(queryPattern, qpid);
    QueryWithAttributesRequest request = new QueryWithAttributesRequest().withDomainName(SDB_DOMAIN_NAME)
                                                                         .withQueryExpression(query)
                                                                         .withAttributeName(SDBConstants.ID,
                                                                                            SDBConstants.PID,
                                                                                            SDBConstants.NAME,
                                                                                            SDBConstants.ICLASS,
                                                                                            SDBConstants.IDATA);

    QueryWithAttributesResponse resp = sdbClient.queryWithAttributes(request);

    if (resp.isSetQueryWithAttributesResult()) {
      QueryWithAttributesResult res = resp.getQueryWithAttributesResult();

      for (Item item : res.getItem()) {
        String id = null;
        String pid = null;
        String name = null;
        String iclass = null;
        String idata = null;

        for (Attribute attr : item.getAttribute()) {
          if (attr.getName().equals(SDBConstants.ID))
            id = attr.getValue();
          else if (attr.getName().equals(SDBConstants.PID))
            pid = attr.getValue();
          else if (attr.getName().equals(SDBConstants.NAME))
            name = attr.getValue();
          else if (attr.getName().equals(SDBConstants.ICLASS))
            iclass = attr.getValue();
          else if (attr.getName().equals(SDBConstants.IDATA))
            idata = attr.getValue();
        }

        System.out.println("Item " + id + " " + pid + " " + name + " " + iclass + " " + idata);
      }
    } else
      fail();

  }

}
