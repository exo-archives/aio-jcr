/***************************************************************************
 * Copyright 2001-2009 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.jcr.benchmark.jcrapi.node.write;

import java.io.ByteArrayInputStream;
import java.util.Random;

import javax.jcr.Node;
import javax.jcr.PropertyType;

import org.exoplatform.jcr.benchmark.JCRTestContext;
import org.exoplatform.jcr.benchmark.jcrapi.AbstractAddItemTest;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SAS
 * 
 * @author Nikolay Zamosenchuk
 */
public class NodeAddNodeWithPropertiesTest extends AbstractAddItemTest {

  private int    binaryPropertySize;

  private byte[] binaryValue;

  private String stringValue = "Content Repository API for Java (JCR) is a specification for"
                                 + " a Java platform API for accessing content repositories in a uniform manner.[1] [2] "
                                 + "The content repositories are used in content management systems to keep the content data"
                                 + " and also the meta-data used in CMS such as versioning meta-data. The specification was "
                                 + "developed under the Java Community Process as JSR-170 (Version 1)[3][4] and as JSR-283 "
                                 + "(Version 2)[5]. The main Java package is javax.jcr. \r\n Overview \r\n A JCR is a type "
                                 + "of Object Database tailored to the storage, searching, and retrieval of hierarchical data."
                                 + " The JCR API grew out of the needs of content management systems, which require storage of "
                                 + "documents and other binary objects with associated metadata. However the API is applicable "
                                 + "to many types of applications. In addition to object storage the JCR provides APIs for "
                                 + "versioning of data, transactions, observation of changes in data, and import or export of "
                                 + "data to XML in a standard way. \r\n source: http://en.wikipedia.org/wiki/Content_repository_API_for_Java";

  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    super.doPrepare(tc, context);
    // getting testCase param to define binaryPropertySize;
    binaryPropertySize = tc.getIntParam("ext.fileSizeInKb") * 1024;
    binaryValue = new byte[binaryPropertySize];
    Random generator = new Random();
    // generating binary value in memory
    generator.nextBytes(binaryValue);
  }

  @Override
  protected void createContent(Node parent, TestCase tc, JCRTestContext context) throws Exception {
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    // adding node
    Node node = nextParent().addNode(context.generateUniqueName("parentNode"));
    // setting string property
    node.setProperty("stringProperty", stringValue, PropertyType.STRING);
    // setting binary property
    node.setProperty("binaryProperty",
                     context.getSession()
                            .getValueFactory()
                            .createValue(new ByteArrayInputStream(binaryValue)),
                     PropertyType.BINARY);

  }

}
