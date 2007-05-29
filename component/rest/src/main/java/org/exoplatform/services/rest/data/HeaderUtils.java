/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.data;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */

public class HeaderUtils {
	
	public static String[] parse(String s) {
		if(s != null && s.length() != 0) {
			String[] ss = s.split(",");
			sortByQvalue(ss, 0, ss.length - 1);
			return removeQvalues(ss);
		}
		return null;
	}
	
	public static float parseQuality(String s) {
		float q = Float.valueOf(s);
		if(q >= 0f && q <= 1.0f)
			return q;
		throw new IllegalArgumentException("Invalid quality value, must be between 0 and 1");
			
	}
	
	public static String[] sortByQvalue(String s[], int lo0, int hi0) {
		int lo = lo0;
		int hi = hi0;
		if (hi0 > lo0) {
			while( lo <= hi ) {
				if(getQvalue(s[lo]) <= getQvalue(s[hi]))
					swap(s, lo, hi);
				lo++;	hi--;
				if( lo0 < hi )
					sortByQvalue(s, lo0, hi );
				if( lo < hi0 )
					sortByQvalue(s, lo, hi0 );
			}
		}
		return s;
	}
	
	public static String[] removeQvalues(String[] s) {
		for(int i = 0; i < s.length; i++)
			s[i] = s[i].split(";q=")[0];
		return s;
	}
	
	public static Float getQvalue(String s) {
		float q = 1.0f;
		String[] temp = s.split(";");
		for(String t : temp) {
			if(t.startsWith("q=")) {
				String[] qq = t.split("=");
				q = parseQuality(qq[1]);
			}
		}
		return q;
	}

	private static void swap(String a[], int i, int j) {
		String t = a[i];
		a[i] = a[j];
		a[j] = t;
	}

}
