/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.config;

import java.util.ArrayList;


/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: ContainerEntry.java 13463 2007-03-16 09:17:29Z geaz $
 */

public class ContainerEntry extends MappedParametrizedObjectEntry {
  
  private ArrayList valueStorages;
  
//  private BinarySwapEntry binarySwap;

	public ContainerEntry() {
		super();
	}

	public ContainerEntry(String type, ArrayList params) {
		super(type, params);
	}

  public ArrayList getValueStorages() {
    return valueStorages;
  }

  public void setValueStorages(ArrayList valueStorages) {
    this.valueStorages = valueStorages;
  }
  
//  public BinarySwapEntry getBinarySwap() {
//    return binarySwap;
//  }
//
//  public void setBinarySwap(BinarySwapEntry binarySwap) {
//    this.binarySwap = binarySwap;
//  }    
}