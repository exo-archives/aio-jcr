/**
 * Copyright 2001-2007 The eXo Platform SASL   All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */
package org.exoplatform.services.jcr.ext.maven;

import java.io.InputStream;
import java.util.Calendar;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="vkrasnikov@gmail.com">Volodymyr Krasnikov</a>
 * @version $Id: MavenResourceBean.java 15:40:22
 */

public class MavenResourceBean {
	private InputStream inputStream;
	private long contentLength;
	private Calendar lastModified;
	
	public MavenResourceBean(){
		
	}
	public MavenResourceBean(InputStream inputStream, long contentLength, Calendar lastModified){
		this.inputStream = inputStream;
		this.contentLength = contentLength;
		this.lastModified = lastModified;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public long getContentLength() {
		return contentLength;
	}

	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}

	public Calendar getLastModified() {
		return lastModified;
	}

	public void setLastModified(Calendar lastModified) {
		this.lastModified = lastModified;
	}
	
}

