/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */
package org.exoplatform.services.jcr.impl.core.value;

import java.io.IOException;

import javax.jcr.PropertyType;

import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;

/**
 * a string value implementation
 * @author Gennady Azarenkov
 */
public class StringValue extends BaseValue {

  public static final int TYPE = PropertyType.STRING;

  /**
   * @param text
   * @throws IOException
   */
  public StringValue(String text) throws IOException {
    super(TYPE, new TransientValueData(text));
  }

  /**
   * @param data
   * @throws IOException
   */
  public StringValue(TransientValueData data) throws IOException {
    super(TYPE, data);
  }

}
