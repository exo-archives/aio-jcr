/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.value;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PropertyType;

import org.exoplatform.services.jcr.JcrImplBaseTest;

/**
 * Created by The eXo Platform SARL
 *
 * 22.01.2007
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: TestDateFormatHelper.java 13421 2007-03-15 10:46:47Z geaz $
 */
public class TestDateFormatHelper extends JcrImplBaseTest {

  private Node testRoot = null;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    
    testRoot = session.getRootNode().addNode("dateformathelper_test");
    session.save();
  }

  @Override
  protected void tearDown() throws Exception {
    testRoot.remove();
    session.save();
    
    super.tearDown();
  }

  public void testTestDateValue() throws Exception {
    
    Calendar calendar = Calendar.getInstance();
    
    Node dateParent = testRoot.addNode("date node");
    dateParent.setProperty("calendar", calendar);
    
    assertEquals("Calendars must be equals", calendar, dateParent.getProperty("calendar").getDate());
    
    testRoot.save();
    
    assertEquals("Calendars must be equals", calendar, dateParent.getProperty("calendar").getDate());
  }
  
  public void testTestStringDateValue() throws Exception {
    final String date = "2023-07-05T19:28:00.000+02:00";
    
    Node dateParent = testRoot.addNode("date node");
    dateParent.setProperty("calendar", date, PropertyType.DATE);
    
    assertEquals("Dates must be equals", date, dateParent.getProperty("calendar").getString());
    
    testRoot.save();
    
    assertEquals("Dates must be equals", date, dateParent.getProperty("calendar").getString());
  }
  
}
