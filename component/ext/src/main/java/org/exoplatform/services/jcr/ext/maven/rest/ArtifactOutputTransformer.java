/**
 * Copyright 2001-2007 The eXo Platform SASL   All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */
package org.exoplatform.services.jcr.ext.maven.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.exoplatform.services.rest.transformer.OutputEntityTransformer;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="vkrasnikov@gmail.com">Volodymyr Krasnikov</a>
 * @version $Id: ArtifactOutputTransformer.java 10:20:38
 */

public class ArtifactOutputTransformer extends OutputEntityTransformer {
	@Override
	public void writeTo(Object entity, OutputStream entityDataStream) throws IOException {
		// TODO Auto-generated method stub
		//entity is a InputStream
		IOUtils.copy((InputStream)entity, entityDataStream);
		
		IOUtils.closeQuietly( (InputStream)entity );
	}
}

