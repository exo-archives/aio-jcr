<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page isELIgnored="false"%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <title>eXo Platform JCR browser sample SELECT REPOSITORY</title>
	<link rel="stylesheet" href="../exojcrstyle.css">
</head>

<jsp:useBean id="browser" scope="session" class="org.exoplatform.applications.jcr.browser.JCRBrowser"></jsp:useBean>

<body>
  <h1>eXo Platform JCR browser sample</h1>
  
<c:choose>
    <c:when test="${browser.errorsFound}">
      <%-- show errors only --%>
      <div id="browserErrors" class="errors">
        <p class="errorHeader">Error(s) occurs during the browser work. Check log/console for details.</p>
        <c:forEach var="err" items="${browser.errorsAndClean}">
          <c:out value="${err}" escapeXml="true"/><br/>
        </c:forEach>
        <span class="errorBack"><a href="${request.contextPath}">Back</a></span>
      </div>
    </c:when>
    
    <c:otherwise>
			  <h2 class="info">Selected:</h2>
			  <h2 class="info">Repository:&nbsp;
			  	<span class="infoText"><c:out value="${browser.repository.configuration.name}"/>
				  		<form method="post" name="repository" action="repository.jsp" >
				  			<input name="submit" type="submit" value="change">
				  		</form>
				  </span>		
			  </h2>
			  <h2 class="info">Workspace:&nbsp;
			  <span class="infoText"><c:out value="${browser.session.workspace.name}"/>
				  <form action="workspace.jsp" method="post" name="workspace">
				    <input name="submit" type="submit" value="change">
				  </form>
			  </span>
			  </h2><h2>&nbsp;
			  <span class="infoText">
				  <form action="../user/index.jsp" method="post" name="browse">
				    <input name="submit" type="submit" value="browse!">
				  </form>
			  </span>
			  </h2>
	
    </c:otherwise>
  </c:choose>
  
</body>  
</html>

