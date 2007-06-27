/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.core.nodetype;

import javax.jcr.nodetype.PropertyDefinition;


/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: PropertyDefinitions.java 12843 2007-02-16 09:11:18Z peterit $
 */

public class PropertyDefinitions {
	
	private PropertyDefinition multiDef = null;
	private PropertyDefinition singleDef = null;

	public PropertyDefinitions() {
		super();
	}
	
	public void setDefinition(PropertyDefinition def) {
		boolean residual = ((ExtendedItemDefinition)def).isResidualSet(); 
		if(def.isMultiple()) {
			if( (residual && multiDef == null) || !residual) 
				multiDef = def;
		} else {
			if( (residual && singleDef == null) || !residual) 
				singleDef = def;
		}
	}
	
	public PropertyDefinition getDefinition(boolean multiple) { 

		refresh();
		
		if(multiple && multiDef != null)
			return multiDef;
		if(!multiple && singleDef != null)
			return singleDef;
		
		return null;
	}
	
	public PropertyDefinition getAnyDefinition() { 

		refresh();
		
		if(multiDef != null)
			return multiDef;
		if(singleDef != null)
			return singleDef;
		
		return null;
	}
  
	private void refresh() {
		// if both defined should be both either residual or not 
		if (multiDef != null && singleDef != null) {
			if (((ExtendedItemDefinition) multiDef).isResidualSet()
					&& !((ExtendedItemDefinition) singleDef).isResidualSet())
				multiDef = null;
			if (((ExtendedItemDefinition) singleDef).isResidualSet()
					&& !((ExtendedItemDefinition) multiDef).isResidualSet())
				singleDef = null;
		}

	}
  
	public String dump() {
		return "Definitions single: "+((singleDef==null)?"N/D":singleDef.getName())+
		", multiple: "+((multiDef==null)?"N/D":multiDef.getName());
	}

}
