/**
 * Copyright 2001-2007 The eXo Platform SAS All rights reserved.
 * Please look at license.txt in info directory for more license detail.
 **/

package org.exoplatform.services.jcr.impl.jndi;

import javax.jcr.Repository;
import javax.naming.InitialContext;

import org.exoplatform.services.jcr.JcrImplBaseTest;

/**
 * Created by The eXo Platform SAS.
 * 
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id$
 */
public class RepositoryBindingTest extends JcrImplBaseTest {

  public void setUp() throws Exception {
    super.setUp();
  }

  /**
   * Prerequisites: there should be entry in configuration.xml like: <component-plugin>
   * <name>bind.datasource</name> <set-method>addPlugin</set-method>
   * <type>org.exoplatform.services.naming.BindReferencePlugin</type> <init-params> <value-param>
   * <name>bind-name</name> <value>repo</value> </value-param> <value-param> <name>class-name</name>
   * <value>javax.jcr.Repository</value> </value-param> <value-param> <name>factory</name>
   * <value>org.exoplatform.services.jcr.impl.jndi.BindableRepositoryFactory</value> </value-param>
   * <properties-param> <name>ref-addresses</name> <description>ref-addresses</description>
   * <property name="repositoryName" value="db1"/> <!-- property name="containerConfig" value=""/
   * --> </properties-param> </init-params> </component-plugin>
   * 
   * 
   * @throws Exception
   */
  public void testIfConfiguredRepositoryBound() throws Exception {

    InitialContext ctx = new InitialContext();
    Repository rep = (Repository) ctx.lookup("repo");
    assertNotNull(rep);
  }
}
