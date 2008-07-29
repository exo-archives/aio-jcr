/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.services.jcr.impl.utils;

import junit.framework.TestCase;

import org.exoplatform.services.jcr.util.StringNumberParser;

/**
 * Created by The eXo Platform SAS
 * 
 * Date: 18.06.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: StringNumberParserTest.java 15951 2008-06-18 13:11:53Z pnedonosko $
 */
public class StringNumberParserTest extends TestCase {

  public void testParseInt() {
    assertEquals(1000, StringNumberParser.parseInt("1000"));

    assertEquals(1024, StringNumberParser.parseInt("1K"));

    assertEquals(5 * 1024, StringNumberParser.parseInt("5K"));

    assertEquals(127 * 1024 * 1024, StringNumberParser.parseInt("127M"));

    assertEquals(1 * 1024 * 1024 * 1024, StringNumberParser.parseInt("1g"));
  }

  public void testParseLong() {
    assertEquals(1000l, StringNumberParser.parseLong("1000"));

    assertEquals(1024l, StringNumberParser.parseLong("1K"));

    assertEquals(5l * 1024, StringNumberParser.parseLong("5K"));

    assertEquals(127l * 1024 * 1024, StringNumberParser.parseLong("127M"));

    assertEquals(4l * 1024 * 1024 * 1024, StringNumberParser.parseLong("4g"));

    assertEquals(5l * 1024 * 1024 * 1024 * 1024, StringNumberParser.parseLong("5TB"));
  }

  public void testParseNumber() {
    assertEquals(10.27d, StringNumberParser.parseNumber("10.27").doubleValue());
    
    assertEquals(233.4 * 1024 * 1024, StringNumberParser.parseNumber("233.4m").doubleValue());
  }
  
  public void testParseTime() {
    assertEquals(63l * 1000, StringNumberParser.parseTime("63"));

    assertEquals(2l * 60 * 1000, StringNumberParser.parseTime("2m"));

    assertEquals(15l * 60 * 60 * 1000, StringNumberParser.parseTime("15h"));

    assertEquals(3l * 24 * 60 * 60 * 1000, StringNumberParser.parseTime("3d"));

    assertEquals(5l * 7 * 24 * 60 * 60 * 1000, StringNumberParser.parseTime("5w"));

    assertEquals(12l, StringNumberParser.parseTime("12ms"));
  }

}
