package org.exoplatform.services.jcr.ext.maven;

public class SearchCriteria {
	
	private String containsExpr;
	private boolean includePom;
	private boolean includeJar;
	
	public String getContainsExpr() {
		return containsExpr;
	}
	public void setContainsExpr(String containsExpr) {
		this.containsExpr = containsExpr;
	}
	public boolean isIncludePom() {
		return includePom;
	}
	public void setIncludePom(boolean includePom) {
		this.includePom = includePom;
	}
	public boolean isIncludeJar() {
		return includeJar;
	}
	public void setIncludeJar(boolean includeJar) {
		this.includeJar = includeJar;
	}
	
}
