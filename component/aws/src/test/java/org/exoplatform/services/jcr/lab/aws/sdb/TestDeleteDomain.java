package org.exoplatform.services.jcr.lab.aws.sdb;

import java.util.ArrayList;
import java.util.List;

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
   * sdbService.
   */
  protected AmazonSimpleDB   sdbService;

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

      if (mySecretKey == null || mySecretKey.length() <= 0)
        fail("Secret key required.");

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

  ///////////////////////// TEST
  
  /**
   * testDeleteDomain.
   *
   * @throws Exception
   */
  public void testDeleteDomain() throws Exception {
    
    sdbService.deleteDomain(new DeleteDomainRequest("exo-aws-test-ws"));
    sdbService.deleteDomain(new DeleteDomainRequest("exo-aws-test-ws1"));
    sdbService.deleteDomain(new DeleteDomainRequest("exo-aws-test-ws2"));
    
  }
  
}
