/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.util;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.exoplatform.services.jcr.impl.util.ArraysComparator;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class TestArraysComparator extends TestCase {

  public void testname() throws Exception {
    String[] first = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" };
    String[] second = new String[] { "11", "12", "13", "14", "15", "6", "7", "8", "9", "10" };
    List<String> l1 = new ArrayList<String>();
    List<String> l2 = new ArrayList<String>();
    List<String> l3 = new ArrayList<String>();
    ArraysComparator<String> comparator = new ArraysComparator<String>();
    comparator.findDifferences(first, second, l1, l2, l3);
  }
}
