<%@ page import='java.util.*' %>
<%@ page import='javax.jcr.*' %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page pageEncoding="ISO-8859-1" %>

<% 
  String repName = (String) request.getSession().getAttribute("rep");
  Session ses = (Session) request.getSession().getAttribute("ses");
  Node node = (Node) request.getSession().getAttribute("node");
%>

<html>
	<head>
	  <title>eXo Platform JCR browser sample</title>	  
		<link rel="stylesheet" href="../exojcrstyle.css">
		<meta content="text/html; charset=UTF-8" http-equiv="content-type">
	</head>
  <body>
    <h1>eXo Platform JCR browser sample</h1>
      <form action="../admin/index.jsp" method="post" name="admin">
        <!-- input type="hidden" name="rep" value="<%= repName%>" -->
        <h2 class="info">Repository:&nbsp;<span class="infoText"><%= repName%></span></h2>
        <h2 class="info">Workspace:&nbsp;<span class="infoText"><%= ses.getWorkspace().getName()%>&nbsp;<input type="submit" name="submit" value="select"></span>
       	</h2>
      </form>
     <%if (!node.isSame(ses.getRootNode())) {%>
     <form action="." method="post" name="location" name="user_back">
       <input type="hidden" name="node" value="<%= node.getParent().getPath()%>">
       <h2 class="info">Path:&nbsp;
        	<span class="infoText"><%= node.getPath()%>&nbsp;<input type="submit" name="submit" value=".."></span>
       </h2>
     </form>
     <%} else {%>
       <h2>Path:&nbsp;<span class="infoText"><%= node.getPath()%></span>
       </h2>
     <%}%>
     <h2>Childrens:</h2>
     <hr width="25%" align=left>
		 <h2 class="child">Properties:</h2>
     <%if (node.hasProperties()) {%>
     <table cellpadding="0" cellspacing="0">
       <%PropertyIterator pi = node.getProperties();%>
       <%while (pi.hasNext()) {
           Property tp = pi.nextProperty();
           String pv = "";
           try {
             pv = tp.getString();
           } catch (Exception e) {
             try {
               Value[] pvs = tp.getValues();
               for(int i = 0; i < pvs.length; i++) {
                 if (i > 0)
                   pv = pv + ", ";
                 pv = pv + pvs[i].getString();
               }
             } catch (Exception e1) { pv = "&lt; non-viewable &gt;"; }
           }
       %>
       <tr><td class="key"><%= tp.getName()%></td><td class="value"><%= pv%></td></tr>
       <%}%>
     </table>
     <%}%>
     <hr width="25%" align=left>
     <h2 class="child">Nodes:<h2>
     <%if (node.hasNodes()) {%>
     <table cellpadding="0" cellspacing="0">
       <%NodeIterator ni = node.getNodes();%>
       <%int count = 0;%>
       <%while (ni.hasNext()) {%>
         <%Node tn = ni.nextNode();%>
       <tr><td>
       <form action="." method="post" name="Child_<%= count++%>">
         <input type="hidden" name="node" value="<%= tn.getPath()%>">
         <input type="submit" name="submit" value="<%= tn.getName()%>">
       </form>
       </td></tr>
       <%}%>
     </table>
     <%}%>
  </body>  
</html>
