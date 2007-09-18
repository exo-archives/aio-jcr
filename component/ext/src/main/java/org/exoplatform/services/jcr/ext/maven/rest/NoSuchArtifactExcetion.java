/**
 * Copyright 2001-2007 The eXo Platform SASL   All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */
package org.exoplatform.services.jcr.ext.maven.rest;
/**
 * Created by The eXo Platform SARL        .
 * @author <a href="vkrasnikov@gmail.com">Volodymyr Krasnikov</a>
 * @version $Id: NoSuchArtifactExcetion.java 15:07:17
 */

public class NoSuchArtifactExcetion extends Exception {
	public NoSuchArtifactExcetion(String message){
		super(message);
	}
	public NoSuchArtifactExcetion(String message, Throwable cause){
		super(message, cause);
	}
}

