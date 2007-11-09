/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS. All rights reserved.          *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.datamodel;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.exoplatform.services.jcr.util.SIDGenerator;

/**
 * Created by The eXo Platform SAS
 * Author : Peter Nedonosko
 *          peter.nedonosko@exoplatform.com.ua
 * 11.10.2007  
 *
 * -XX:MaxPermSize=128m
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: QNameInterTest.java 111 2007-11-11 11:11:11Z peterit $
 */
public class QNameInternTest extends TestCase {

  static public final int INTERN_SIZE = 100000;
  static public final int SAMPLE_MOD = INTERN_SIZE / 100;
  static public final int NOTSAMPLE_MOD = SAMPLE_MOD / 10;

  private String memoryInfo() {
    String info = "";
    info = "free: " + mb(Runtime.getRuntime().freeMemory()) + "M of " + mb(Runtime.getRuntime().totalMemory()) + "M (max: " + mb(Runtime.getRuntime().maxMemory()) + "M)";
    return info;
  }
  
  private String mb(long mem) {
    return String.valueOf(Math.round(mem * 100d/ (1024d * 1024d)) / 100d);
  }
  
  public void testInternReferenced() throws Exception {
    System.out.println("START " + getName() + ", " + memoryInfo());
    
    Set<String> samples = new HashSet<String>();
    
    long dsum = 0;
    
    String[] interList = new String[INTERN_SIZE];
    for (int i=0; i<INTERN_SIZE; i++) {
      String s = SIDGenerator.generate().intern();
      interList[i] = s; // save ref to prevent GCing
      if (i%SAMPLE_MOD == 0)
        samples.add(s);
    }
    
    // add not containing strings    
    for (int i=0; i<NOTSAMPLE_MOD; i++) {
      samples.add(SIDGenerator.generate());
    }
    
    for (String sample: samples) {
      long start = System.currentTimeMillis();
      String sint = sample.intern(); // ask already interned object (most of the samples set)
      long d = System.currentTimeMillis() - start;
      dsum += d;
    }
    
    System.out.println("\tSample avg. get time " + (dsum * 1f/samples.size()));
    System.out.println("FINISH " + getName() + ", " + memoryInfo());
  }
  
  public void testInternEquals() throws Exception {
    System.out.println("START " + getName() + ", " + memoryInfo());
    
    Set<String> samples = new HashSet<String>();
    
    long dsum = 0;
    
    String[] interList = new String[INTERN_SIZE];
    for (int i=0; i<INTERN_SIZE; i++) {
      String s = SIDGenerator.generate().intern();
      interList[i] = s; // save ref to prevent GCing
      if (i%SAMPLE_MOD == 0)
        samples.add(s);
    }
    
    // add not containing strings
    for (int i=0; i<NOTSAMPLE_MOD; i++) {
      samples.add(SIDGenerator.generate());
    }
    
    for (String sample: samples) {
      long start = System.currentTimeMillis();
      String sint = new String(sample.toCharArray()).intern(); // ask already interned content (most of the samples set)
      long d = System.currentTimeMillis() - start;
      dsum += d;
      //System.out.println("Sample found " + d);
    }
    
    System.out.println("\tSample avg. get time " + (dsum * 1f/samples.size()));
    System.out.println("FINISH " + getName() + ", " + memoryInfo());
  }
  
  public void testStringArrayTraverse() throws Exception {
    
    System.out.println("START " + getName() + ", " + memoryInfo());
    
    Set<String> samples = new HashSet<String>();
    
    long dsum = 0;
    
    String[] interList = new String[INTERN_SIZE];
    for (int i=0; i<interList.length; i++) {
      String s = SIDGenerator.generate();
      interList[i] = s; // save ref to prevent GCing
      interList[i] = s;
      if (i%SAMPLE_MOD == 0)
        samples.add(s);
    }
    
    // add not containing strings
    for (int i=0; i<NOTSAMPLE_MOD; i++) {
      samples.add(SIDGenerator.generate());
    }    
    
    next: for (String sample: samples) {
      long start = System.currentTimeMillis();
      for (String is: interList) {
        if (is == sample) {
          long d = System.currentTimeMillis() - start;
          dsum += d;
          //System.out.println("Sample found " + d);
          continue next;
        }
      }
      dsum += System.currentTimeMillis() - start;
    }
    
    System.out.println("\tSample avg. get time " + (dsum * 1f/samples.size()));
    System.out.println("FINISH " + getName() + ", " + memoryInfo());
  }
    
}
 