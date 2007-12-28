<%@ page import='java.util.*' %>
<%@ page import='javax.jcr.*' %>
<%@ page import='javax.naming.*' %>

<%
  InitialContext ctx = new InitialContext();
  out.println(ctx);
  out.println("repository = "+ ctx.lookup("repository"));
%>