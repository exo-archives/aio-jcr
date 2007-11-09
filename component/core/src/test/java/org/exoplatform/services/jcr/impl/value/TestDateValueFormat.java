/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.value;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.jcr.Node;
import javax.jcr.PropertyType;

import org.exoplatform.services.jcr.JcrImplBaseTest;

/**
 * Created by The eXo Platform SARL
 *
 * 22.01.2007
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class TestDateValueFormat extends JcrImplBaseTest {

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
  
  /**
   * It's a pb found. If we will set property with date contains timezone different to the current.
   * And will get property as string after that. We will have a date with the current timezone, actualy the date 
   * will be same but in different tz.
   * 
   * "2023-07-05T19:28:00.000-0300" --> "2023-07-06T01:28:00.000+0300" - it's same date, but... print is different.
   * 
   *  The pb can be solved ib SimpleDateFormat be setting the formatter timezone before the format procedure.
   *  
   *  TimeZone tz = TimeZone.getTimeZone("GMT-03:00");
      Calendar cdate = Calendar.getInstance();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
      sdf.setTimeZone(tz);
      Date d = sdf.parse(javaDate);
      log.info("parse " + sdf.format(d)); // print date in GMT-03:00 timezone 
   * 
   * @throws Exception
   */
  public void testTestStringDateValue() throws Exception {
    final String date = "2023-07-05T19:28:00.000-03:00"; // ISO8601, JCR supported
    final String javaDate = "2023-07-05T19:28:00.000-0300"; // ISO8601 + RFC822, jvm supported 
    
    Node dateParent = testRoot.addNode("date node");
    dateParent.setProperty("calendar", date, PropertyType.DATE);
    
    //TimeZone tz = TimeZone.getTimeZone("GMT-03:00");
    Calendar cdate = Calendar.getInstance();
    
    //Calendar cdate = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    //sdf.setTimeZone(tz);
    
    Date d = sdf.parse(javaDate);
    //log.info("parse " + sdf.format(d));
    //cdate.setTimeZone(TimeZone.getTimeZone("GMT-05:00"));
    cdate.setTime(d);
    //log.info("calendar " + sdf.format(cdate.getTime())); 
    
    //assertEquals("Dates must be equals", date, dateParent.getProperty("calendar").getString());
    assertEquals("Dates must be equals", cdate, dateParent.getProperty("calendar").getDate());
    
    testRoot.save();
    
    assertEquals("Dates must be equals", cdate, dateParent.getProperty("calendar").getDate());
  }
  
}
