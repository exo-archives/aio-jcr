/**
 * Copyright 2001-2007 The eXo Platform SASL   All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */
package org.exoplatform.services.jcr.ext.maven;
/**
 * Created by The eXo Platform SARL        .
 * @author <a href="vkrasnikov@gmail.com">Volodymyr Krasnikov</a>
 * @version $Id: ArtifactManagingException.java 12:15:42
 */

public final class ArtifactManagingException extends Exception {
	
	public ArtifactManagingException(){
		super();
	}
	
	public ArtifactManagingException(String message){
		super(message);
	}
	
	public ArtifactManagingException(String message, Throwable e){
		super(message, e);
	}

}

