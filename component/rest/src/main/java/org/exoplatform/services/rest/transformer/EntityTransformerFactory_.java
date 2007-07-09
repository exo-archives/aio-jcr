/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.transformer;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class EntityTransformerFactory_ {
  
	private Class<? extends GenericEntityTransformer> transformerType;
	
	public EntityTransformerFactory_(Class<? extends GenericEntityTransformer> transformerType) {
		this.transformerType = transformerType;
	}
	
  public GenericEntityTransformer newTransformer() throws Exception {
  	return transformerType.newInstance();
  }
  
}
