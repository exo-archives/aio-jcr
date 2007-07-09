/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.observation;

import javax.jcr.observation.Event;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: ExtendedEvent.java 13001 2007-02-27 14:02:15Z ksm $
 */

public interface ExtendedEvent extends Event {
  
  public static final int SAVE = 32;
  public static final int MOVE = 64;
  public static final int COPY = 128;
  public static final int ADD_MIXIN = 256;
  public static final int REMOVE_MIXIN = 512;
  public static final int CLONE = 1024;
  public static final int UPDATE = 2048;
  public static final int IMPORT = 4096;
  public static final int CHECKIN = 8192;
  public static final int CHECKOUT = 16384;
  public static final int RESTORE = 32768;
  public static final int MERGE = 65536;
  public static final int CANCEL_MERGE = 131072;
  public static final int DONE_MERGE = 262144;
  public static final int ADD_VERSION_LABEL = 524288;
  public static final int REMOVE_VERSION_LABEL = 1048576;
  public static final int REMOVE_VERSION = 2097152;
  public static final int LOCK = 4194304;
  public static final int UNLOCK = 8388608;
  public static final int READ = 16777216;
  

}
