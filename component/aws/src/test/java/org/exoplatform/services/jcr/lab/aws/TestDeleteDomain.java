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
package org.exoplatform.services.jcr.lab.aws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.jcr.aws.storage.value.s3.S3ValueIOUtil;

import junit.framework.TestCase;

import com.amazonaws.sdb.AmazonSimpleDB;
import com.amazonaws.sdb.AmazonSimpleDBClient;
import com.amazonaws.sdb.AmazonSimpleDBConfig;
import com.amazonaws.sdb.AmazonSimpleDBException;
import com.amazonaws.sdb.model.CreateDomainRequest;
import com.amazonaws.sdb.model.CreateDomainResponse;
import com.amazonaws.sdb.model.DeleteDomainRequest;
import com.amazonaws.sdb.model.DeleteDomainResponse;
import com.amazonaws.sdb.model.GetAttributesRequest;
import com.amazonaws.sdb.model.GetAttributesResponse;
import com.amazonaws.sdb.model.ListDomainsRequest;
import com.amazonaws.sdb.model.ListDomainsResponse;
import com.amazonaws.sdb.model.ListDomainsResult;

/**
 * TestDeleteDomain - utility for SDB domains cleanup (for TESTs & DEBUG).
 * 
 */
public class TestDeleteDomain extends TestCase {

  /**
   * accessKey.
   */
  private String           accessKey;

  /**
   * secretKey.
   */
  private String           secretKey;

  /**
   * sdbService.
   */
  protected AmazonSimpleDB sdbService;

  /**
   * {@inheritDoc}
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    if (sdbService == null) {
      AmazonSimpleDBConfig config = new AmazonSimpleDBConfig();
      config.setSignatureVersion("0");

      String myAccessKey = System.getProperty("exo.aws.access.key");
      String mySecretKey = System.getProperty("exo.aws.secret.key");

      if (myAccessKey == null || myAccessKey.length() <= 0)
        fail("Access key required.");
      else
        accessKey = myAccessKey;

      if (mySecretKey == null || mySecretKey.length() <= 0)
        fail("Secret key required.");
      else
        secretKey = mySecretKey;

      try {
        sdbService = new AmazonSimpleDBClient(myAccessKey, mySecretKey, config);
      } catch (Throwable e) {
        e.printStackTrace();
        fail(e.getMessage());
      }

      // print list
      System.out.println("Available domains: ");
      for (String dn : getDomainsList()) {
        System.out.println("\t" + dn);
      }
    }
  }

  // utility methods

  protected ListDomainsResult getDomains(AmazonSimpleDB service, String nextToken, int maxDomains) throws AmazonSimpleDBException {
    ListDomainsRequest request = new ListDomainsRequest(maxDomains, nextToken);
    ListDomainsResponse response = service.listDomains(request);

    if (response.isSetListDomainsResult()) {
      return response.getListDomainsResult();
    } else
      return null;
  }

  /**
   * Return list of domain names. If no one domains found then an empty list will be returned.
   * 
   * @return list of names
   * @throws AmazonSimpleDBException
   */
  protected List<String> getDomainsList() throws AmazonSimpleDBException {
    String nextToken = "";

    List<String> names = new ArrayList<String>();
    ListDomainsResult result = getDomains(sdbService, nextToken, 10);
    while (nextToken != null) {
      nextToken = null;

      if (result != null) {
        List<String> domainNamesList = result.getDomainName();
        names.addAll(domainNamesList);

        nextToken = result.getNextToken();
      }
    }

    return names;
  }

  protected CreateDomainResponse createDomain(AmazonSimpleDB service, String domainName) throws AmazonSimpleDBException {
    CreateDomainRequest request = new CreateDomainRequest(domainName);
    return service.createDomain(request);
  }

  protected DeleteDomainResponse deleteDomain(AmazonSimpleDB service, String domainName) throws Exception {
    DeleteDomainRequest request = new DeleteDomainRequest(domainName);

    return service.deleteDomain(request);
  }

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

  // /////////////////////// TEST

  /**
   * testDeleteDomain.
   * 
   * @throws Exception
   *           error
   */
  public void testDeleteDomain() throws Exception {

    sdbService.deleteDomain(new DeleteDomainRequest("ecm-test-system"));
    sdbService.deleteDomain(new DeleteDomainRequest("ecm-test-collboration"));
    sdbService.deleteDomain(new DeleteDomainRequest("ecm-test-backup"));
  }

  /**
   * testDeleteBuckets.
   * 
   * @throws Exception
   *           error
   */
  public void testDeleteBuckets() throws Exception {

    try {
      // delete all objects
      String[] list = S3ValueIOUtil.getBucketList("jcr-test", accessKey, secretKey, "");
      for (String sl : list) {
        if (S3ValueIOUtil.deleteValue("jcr-test", accessKey, secretKey, sl)) {
          System.out.println("delete S3 value: " + sl);
        } else
          System.out.println("WARN cannot delete value " + sl);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      // delete bucket
      S3ValueIOUtil.deleteBucket("jcr-test", accessKey, secretKey);
      System.out.println("delete S3 bucket: jcr-test");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
