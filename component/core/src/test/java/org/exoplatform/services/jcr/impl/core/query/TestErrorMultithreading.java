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

package org.exoplatform.services.jcr.impl.core.query;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.services.jcr.core.CredentialsImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;

/**
 * Created by The eXo Platform SAS Author : Sergey Karpenko
 * <sergey.karpenko@exoplatform.com.ua>
 * 
 * @version $Id: $
 */

public class TestErrorMultithreading extends BaseQueryTest {
  public static final int    COUNT         = 10;

  public static final int    NODE_COUNT    = 100;

  public static final int    THREADS_COUNT = 10;

  public static final String THREAD_NAME   = "name";

  public void tearDown() {
    // do nothing
  }

  public void testRunActions() throws Exception {
    fillRepo();
    // checkRepo();
  }

  private void fillRepo() throws Exception {

    class Writer extends Thread {
      public String  name;

      public Session sess;

      Writer(String name, Session s) {
        this.name = name;
        this.sess = s;
      }

      public void run() {
        System.out.println(name +" - START");
        try {
          Node root = sess.getRootNode();

          for (int i = 0; i < COUNT; i++) {
            for (int j = 0; j < NODE_COUNT; j++) {
              int num = i * NODE_COUNT*10 + j;
              String n = name +"_" + num;
              root.addNode(n);
              System.out.println("ADD "+n);
            }
            root.save();
            System.out.println(name + " - SAVE");
          }
        } catch (Exception e) {
          System.out.println();
          System.out.println(name + "-thread error");
          e.printStackTrace();
        }
        System.out.println(name +" - FINISH");
      }
    }

    Set<Writer> writers = new HashSet<Writer>();

    // create
    for (int t = 0; t < THREADS_COUNT; t++) {
      Credentials credentials = new CredentialsImpl("admin", "admin".toCharArray());

      Session ss = (SessionImpl) repository.login(credentials, "ws");
      Writer wr = new Writer(THREAD_NAME + t, ss);
      writers.add(wr);
    }

    // start
    Iterator<Writer> it = writers.iterator();
    while (it.hasNext()) {
      it.next().start();
    }

    // join
    it = writers.iterator();
    while (it.hasNext()) {
      it.next().join();
    }

    System.out.println("FINISH!");
  }
}
