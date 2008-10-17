package org.exoplatform.services.jcr.lab.aws.sdb;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.jcr.impl.util.ISO9075;

import junit.framework.TestCase;

import com.amazonaws.sdb.AmazonSimpleDB;
import com.amazonaws.sdb.AmazonSimpleDBClient;
import com.amazonaws.sdb.AmazonSimpleDBConfig;
import com.amazonaws.sdb.AmazonSimpleDBException;
import com.amazonaws.sdb.model.Attribute;
import com.amazonaws.sdb.model.CreateDomainRequest;
import com.amazonaws.sdb.model.CreateDomainResponse;
import com.amazonaws.sdb.model.DeleteDomainRequest;
import com.amazonaws.sdb.model.DeleteDomainResponse;
import com.amazonaws.sdb.model.GetAttributesRequest;
import com.amazonaws.sdb.model.GetAttributesResponse;
import com.amazonaws.sdb.model.GetAttributesResult;
import com.amazonaws.sdb.model.ListDomainsRequest;
import com.amazonaws.sdb.model.ListDomainsResponse;
import com.amazonaws.sdb.model.ListDomainsResult;
import com.amazonaws.sdb.model.PutAttributesRequest;
import com.amazonaws.sdb.model.PutAttributesResponse;
import com.amazonaws.sdb.model.QueryRequest;
import com.amazonaws.sdb.model.QueryResponse;
import com.amazonaws.sdb.model.QueryResult;
import com.amazonaws.sdb.model.ReplaceableAttribute;
import com.amazonaws.sdb.model.ResponseMetadata;

public class TestSimpleDB extends TestCase {

  public static final String EXO_DOMAIN = "exo-test-domain";

  protected AmazonSimpleDB   sdbService;

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

      // create test domain
      List<String> domains = getDomainsList();
      if (!domains.contains(EXO_DOMAIN)) {
        // create
        CreateDomainResponse resp = createDomain(sdbService, EXO_DOMAIN);
      }
    }
  }

  @Override
  protected void tearDown() throws Exception {
    // do nothing now

    super.tearDown();
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

  protected PutAttributesResponse createItem(AmazonSimpleDB service,
                                             String domainName,
                                             String item,
                                             List<ReplaceableAttribute> list) throws Exception {
    PutAttributesRequest request = new PutAttributesRequest().withDomainName(domainName)
                                                             .withItemName(item);
    request.setAttribute(list);
    return service.putAttributes(request);
  }

  protected List<String> createItems(AmazonSimpleDB service,
                                     String domainName,
                                     String prefixName,
                                     int itemsCount) {

    List<String> ids = new ArrayList<String>();

    for (int i = 1; i <= itemsCount; i++) {

      List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();
      list.add(new ReplaceableAttribute("id", "eXo", false));
      list.add(new ReplaceableAttribute("name", "[]:1[]item" + i + ":1", false));
      list.add(new ReplaceableAttribute("pid", "_pid" + i, false));
      list.add(new ReplaceableAttribute("ver", "1", false));
      list.add(new ReplaceableAttribute("value", "simle item value text " + i, false));

      try {
        String iname = prefixName + i;
        ids.add(iname);
        createItem(service, domainName, iname, list);
      } catch (Exception e) {
        e.printStackTrace();
        fail(e.getMessage());
      }
    }

    return ids;
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



  public void testListDomains() {
    /* Listing the domains belonging to our AWS account */
    String nextToken = "";

    try {
      System.out.println("My Amazon SimpleDB domains:");

      ListDomainsResult result = getDomains(sdbService, nextToken, 10);
      while (nextToken != null) {
        nextToken = null;

        if (result != null) {
          java.util.List<String> domainNamesList = result.getDomainName();
          for (String domainName : domainNamesList) {
            System.out.println(domainName);
          }

          nextToken = result.getNextToken();
        }
      }
    } catch (AmazonSimpleDBException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  public void testGetItem() {
    try {
      GetAttributesResponse response = readItem(sdbService, EXO_DOMAIN, "company1");
      if (response.isSetGetAttributesResult()) {
        GetAttributesResult res = response.getGetAttributesResult();
        List<Attribute> attributeList = res.getAttribute();
        for (Attribute attribute : attributeList) {
          System.out.print("            Attribute");
          System.out.println();
          if (attribute.isSetName()) {
            System.out.print("                Name");
            System.out.println();
            System.out.print("                    " + attribute.getName());
            System.out.println();
          }
          if (attribute.isSetValue()) {
            System.out.print("                Value");
            System.out.println();
            System.out.print("                    " + attribute.getValue());
            System.out.println();
          }
        }
      }
      if (response.isSetResponseMetadata()) {
        System.out.print("        ResponseMetadata");
        System.out.println();
        ResponseMetadata responseMetadata = response.getResponseMetadata();
        if (responseMetadata.isSetRequestId()) {
          System.out.print("            RequestId");
          System.out.println();
          System.out.print("                " + responseMetadata.getRequestId());
          System.out.println();
        }
        if (responseMetadata.isSetBoxUsage()) {
          System.out.print("            BoxUsage");
          System.out.println();
          System.out.print("                " + responseMetadata.getBoxUsage());
          System.out.println();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  public void testGetItems() throws AmazonSimpleDBException {
    // fill sdb with data

    List<String> ids;
    ;
    GetAttributesResponse getResponse = readItem(sdbService, EXO_DOMAIN, "exo-item1");
    if (getResponse.isSetGetAttributesResult()) {
      GetAttributesResult res = getResponse.getGetAttributesResult();
      List<Attribute> attributeList = res.getAttribute();

      if (attributeList.size() <= 0)
        ids = createItems(sdbService, EXO_DOMAIN, "exo-item", 100);
    }

    // test, query and printout
    String token = "";

    QueryRequest request = new QueryRequest().withDomainName(EXO_DOMAIN)
                                             .withNextToken(token)
                                             .withQueryExpression("['pid' = '_pid5']");

    try {

      QueryResponse response = sdbService.query(request);

      System.out.println("Query Action Response");
      System.out.println("=============================================================================");
      System.out.println();

      System.out.print("    QueryResponse");
      System.out.println();
      if (response.isSetQueryResult()) {
        System.out.print("        QueryResult");
        System.out.println();
        QueryResult queryResult = response.getQueryResult();
        java.util.List<String> itemNameList = queryResult.getItemName();
        for (String itemName : itemNameList) {
          System.out.print("            ItemName");
          System.out.println();
          System.out.print("                " + itemName);
        }
        if (queryResult.isSetNextToken()) {
          System.out.print("            NextToken");
          System.out.println();
          System.out.print("                " + queryResult.getNextToken());
          System.out.println();
        }
      }
      if (response.isSetResponseMetadata()) {
        System.out.print("        ResponseMetadata");
        System.out.println();
        ResponseMetadata responseMetadata = response.getResponseMetadata();
        if (responseMetadata.isSetRequestId()) {
          System.out.print("            RequestId");
          System.out.println();
          System.out.print("                " + responseMetadata.getRequestId());
          System.out.println();
        }
        if (responseMetadata.isSetBoxUsage()) {
          System.out.print("            BoxUsage");
          System.out.println();
          System.out.print("                " + responseMetadata.getBoxUsage());
          System.out.println();
        }
      }
      System.out.println();

    } catch (AmazonSimpleDBException ex) {
      System.out.println("Caught Exception: " + ex.getMessage());
      System.out.println("Response Status Code: " + ex.getStatusCode());
      System.out.println("Error Code: " + ex.getErrorCode());
      System.out.println("Error Type: " + ex.getErrorType());
      System.out.println("Request ID: " + ex.getRequestId());
      System.out.print("XML: " + ex.getXML());
    }
  }

  public void testCreateReplaceableItem() {
    List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();
    // list.add(new ReplaceableAttribute( attributeName, value, replacePreviousValue?));
    list.add(new ReplaceableAttribute("organization", "eXo Platform", true));
    list.add(new ReplaceableAttribute("address", "45, 33 Univ str", true));
    
    list.add(new ReplaceableAttribute("state", "Ukraine", true));
    list.add(new ReplaceableAttribute("country", "Ukraine", true));

    try {
      createItem(sdbService, EXO_DOMAIN, "company1", list);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  public void testCreateItemDiffAttributes() {
    List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();
    list.add(new ReplaceableAttribute("item1", "eXo", false));
    list.add(new ReplaceableAttribute("data", "item #1 data", false));

    try {
      createItem(sdbService, EXO_DOMAIN, "test-item1", list);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    list = new ArrayList<ReplaceableAttribute>();
    list.add(new ReplaceableAttribute("itemName", "eXo Item", false));
    list.add(new ReplaceableAttribute("data", "item #2 data", false));

    try {
      createItem(sdbService, EXO_DOMAIN, "test-item2", list);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    // read
    try {
      GetAttributesResponse response = readItem(sdbService, EXO_DOMAIN, "test-item1");
      if (response.isSetGetAttributesResult()) {
        GetAttributesResult res = response.getGetAttributesResult();
        List<Attribute> attributeList = res.getAttribute();
        for (Attribute attribute : attributeList) {
          System.out.print("            Attribute");
          System.out.println();
          if (attribute.isSetName()) {
            System.out.print("                Name");
            System.out.println();
            System.out.print("                    " + attribute.getName());
            System.out.println();
          }
          if (attribute.isSetValue()) {
            System.out.print("                Value");
            System.out.println();
            System.out.print("                    " + attribute.getValue());
            System.out.println();
          }
        }
      }

      response = readItem(sdbService, EXO_DOMAIN, "test-item2");
      if (response.isSetGetAttributesResult()) {
        GetAttributesResult res = response.getGetAttributesResult();
        List<Attribute> attributeList = res.getAttribute();
        for (Attribute attribute : attributeList) {
          System.out.print("            Attribute");
          System.out.println();
          if (attribute.isSetName()) {
            System.out.print("                Name");
            System.out.println();
            System.out.print("                    " + attribute.getName());
            System.out.println();
          }
          if (attribute.isSetValue()) {
            System.out.print("                Value");
            System.out.println();
            System.out.print("                    " + attribute.getValue());
            System.out.println();
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

}
