/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.transformer;

/**
 * EntityTransformerFactory produces instances of GenericEntityTransformer.
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class EntityTransformerFactory {
  
	private Class<? extends GenericEntityTransformer> transformerType;
	
	/**
	 * Create a new instance of GenericEntityTransformer
	 * @param transformerType the type of transformer with should be creted. 
	 */
	public EntityTransformerFactory(Class<? extends GenericEntityTransformer> transformerType) {
		this.transformerType = transformerType;
	}
	
  /**
   * Create a new GenericEntityTransformer
   * @return new instance GenericEntityTransformer
   * @see org.exoplatform.services.rest.transformer.GenericEntityTransformer
   * @throws Exception
   */
  public GenericEntityTransformer newTransformer() throws Exception {
  	return transformerType.newInstance();
  }
  
}
