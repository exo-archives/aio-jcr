/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.jcr.benchmark.usecases;

import com.sun.japex.TestCase;

import java.io.ByteArrayInputStream;
import java.util.Random;

import javax.jcr.Node;
import javax.jcr.Value;

import org.exoplatform.jcr.benchmark.JCRTestBase;
import org.exoplatform.jcr.benchmark.JCRTestContext;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: exo-jboss-codetemplates.xml 34027 2009-07-15 23:26:43Z
 *          aheritier $
 */
public class NodeGetSamePropertyTest extends JCRTestBase {

  private Node                testNode;

  private int                 propertyType1Count;

  private int                 propertyType2Count;

  private final static String PROPERTY_NAME = "prop";

  /**
   * {@inheritDoc}
   */
  @Override
  public void doFinish(TestCase tc, JCRTestContext context) throws Exception {
    super.doFinish(tc, context);
    testNode.remove();
    testNode.getSession().save();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    // TODO Auto-generated method stub
    super.doPrepare(tc, context);
    testNode = context.getSession().getRootNode().addNode(context.generateUniqueName("testRoot"));

    propertyType1Count = tc.getIntParam("usecase.propertyType1Count");
    propertyType2Count = tc.getIntParam("usecase.propertyType1Count");
    Value[] values = new Value[propertyType1Count + propertyType2Count];

    for (int i = 0; i < propertyType1Count; i++) {
      int propertyType1Size = tc.getIntParam("usecase.propertyType1SizeInKb") * 1024;
      byte[] binaryValue = new byte[propertyType1Size];
      Random generator = new Random();
      // generating binary value in memory
      generator.nextBytes(binaryValue);
      values[i] = testNode.getSession()
                          .getValueFactory()
                          .createValue(new ByteArrayInputStream(binaryValue));

    }

    for (int i = 0; i < propertyType2Count; i++) {
      int propertyType2Size = tc.getIntParam("usecase.propertyType2SizeInKb") * 1024;
      byte[] binaryValue = new byte[propertyType2Size];
      Random generator = new Random();
      // generating binary value in memory
      generator.nextBytes(binaryValue);
      values[propertyType1Count + i] = testNode.getSession()
                                               .getValueFactory()
                                               .createValue(new ByteArrayInputStream(binaryValue));

    }
    if (values.length == 1) {
      testNode.setProperty(PROPERTY_NAME, values[0]);
    } else {
      testNode.setProperty(PROPERTY_NAME, values);
    }
    testNode.getSession().save();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    // if ((propertyType1Count + propertyType2Count) == 1) {
    // testNode.getProperty(PROPERTY_NAME).getStream();
    // } else {
    // Property prop = testNode.getProperty(PROPERTY_NAME);
    // Value[] val = prop.getValues();
    // for (int i = 0; i < val.length; i++) {
    // val[i].getStream();
    // }
    // }

    testNode.getProperty(PROPERTY_NAME);
  }

}
