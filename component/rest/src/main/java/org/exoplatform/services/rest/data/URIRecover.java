/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.data;

import java.lang.reflect.Method;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.ArrayList;

import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.URIParam;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */

public class URIRecover {
	

  
	/**
	 * Recovery full URI string from uri parameters an uri annotations
	 * @param uriParams
	 * @param server
	 * @param clazzURIAnno
	 * @return uri string WITHOUT fist slash "/"  
	 */
	public static String recoveryURI(String[] uriParams, Method server, URITemplate clazzURIAnno) {
	  
	  List<URIParam> uriAnno = filterURIParamAnnotations(server.getParameterAnnotations());
		
	  if(uriAnno.size() != uriParams.length)
			throw new IllegalArgumentException("Number of annotatited parameters of method '" + 
					server.getName() + "' is not equals to lenght of 'uriParams' array!");
		
		String fullURIPattern = glueString(server.getAnnotation(URITemplate.class), clazzURIAnno);
		String[] tempArray = fullURIPattern.replaceFirst("/", "").split("/");
		StringBuffer result = new StringBuffer();
		int i = 0;
		for(String s : tempArray) {
			if(s.startsWith("{") && s.endsWith("}")) {
				s = s.replaceAll("\\{", "");
				s = s.replaceAll("\\}", "");
				boolean found = false;
				for(URIParam ua : uriAnno) {
				  if(s.equals(ua.value())) {
				    found = true;
				    break;
				  }
				}
				if(!found) {
//				  throw new IllegalArgumentException("Parameter '" + s + "' from method '" + 
//				      server.getName() + "' annotations was not found in parameter anotations!");
				  i++;
				  continue;
				}
				result.append(uriParams[i++] + "/");
				continue;
			}
			result.append(s + "/");
		}
		return result.toString();
	}

	private static List<URIParam> filterURIParamAnnotations(Annotation[][] anno) {
	  List<URIParam> list = new ArrayList<URIParam>();
    for(Annotation[] a : anno) {
      if(a.length == 0)
        continue;
      if("org.exoplatform.services.rest.URIParam".equals(
          a[0].annotationType().getCanonicalName()))
        
        list.add((URIParam)a[0]);
    }
    return list;
	}
	
	private static String glueString(URITemplate methodURIAnno, URITemplate clazzURIAnno){
		if(methodURIAnno == null && clazzURIAnno == null)
			return "";
		String methodURIPattern = (methodURIAnno == null) ? "" : methodURIAnno.value();
		String clazzURIPattern = (clazzURIAnno == null) ? "" : clazzURIAnno.value();
		if (clazzURIPattern.endsWith("/") && methodURIPattern.startsWith("/"))
			return clazzURIPattern + methodURIPattern.replaceFirst("/", "");
		else if (!clazzURIPattern.endsWith("/") && !methodURIPattern.startsWith("/"))
			return clazzURIPattern + "/" + methodURIPattern;
		else
			return clazzURIPattern + methodURIPattern;
	}
		
}
