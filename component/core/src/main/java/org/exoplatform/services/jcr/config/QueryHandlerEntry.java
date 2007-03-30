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
 * @version $Id: QueryHandlerEntry.java 12843 2007-02-16 09:11:18Z peterit $
 */

public class QueryHandlerEntry extends MappedParametrizedObjectEntry {

	public QueryHandlerEntry() {
		super();
	}
	
	public QueryHandlerEntry(String type, ArrayList params) {
		super(type, params);
	}
}