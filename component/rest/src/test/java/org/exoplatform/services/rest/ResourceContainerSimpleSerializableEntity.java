/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.SerializableInputTransformer;
import org.exoplatform.services.rest.transformer.SerializableOutputTransformer;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class ResourceContainerSimpleSerializableEntity implements ResourceContainer {
	
  @HTTPMethod("GET")
  @URITemplate("/test/serializable/")
  @InputTransformer(SerializableInputTransformer.class)
  @OutputTransformer(SerializableOutputTransformer.class)
	public Response method1(SimpleDeserializableEntity de) {
  	System.out.println("\n>>> serializable entity: " + de.data);
  	de.data = ">>> this is response data";
  	SimpleSerializableEntity se = new SimpleSerializableEntity();
  	se.data = "<<< this request data\n";
		return Response.Builder.ok(se).build();
	}
}
