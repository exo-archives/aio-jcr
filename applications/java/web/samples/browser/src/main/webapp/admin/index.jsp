<%
  String repName = (String) request.getSession().getAttribute("rep");
  String way = (String) request.getSession().getAttribute("way");
  String wsName = (String) request.getSession().getAttribute("ws");
%>
<html>
<head>
  <title>eXo Platform JCR browser sample SELECT REPOSITORY</title>
	<link rel="stylesheet" href="../exojcrstyle.css">
</head>
<body>
  <h1>eXo Platform JCR browser sample</h1>
  <h2 class="info">Selected:</h2>
  <h2 class="info">Repository:&nbsp;
  	<span class="infoText"><%= repName%>/<%= way.toUpperCase()%>
	  		<form action="rep.jsp" method="post" name="repository">
	  			<input name="submit" type="submit" value="change">
	  		</form>
	  </span>		
  </h2>
  <h2 class="info">Workspace:&nbsp;<span class="infoText"><%= wsName%> <form action="ws.jsp" method="post" name="admin_workspace"><input name="submit" type="submit" value="change"></form></span>
  </h2>
  <h2>&nbsp;<span class="infoText"><form action="../user/index.jsp" method="post" name="browse"><input name="submit" type="submit" value="browse!"></form></span>
  </h2>
</body>  
</html>

