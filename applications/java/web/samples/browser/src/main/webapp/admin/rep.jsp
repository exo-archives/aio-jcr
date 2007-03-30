<html>
<head>
  <title>eXo Platform JCR browser sample REPOSITORY ACCESS</title>
	<link rel="stylesheet" href="../exojcrstyle.css">
</head>
<body>
  <h1>eXo Platform JCR browser sample</h1>
  <form action="." method="post">
    Enter repository access way and repository name to view: 
    <%--<input type="radio" name="way" value="jndi" checked onclick="document.forms[0].rep.value='repository'">JNDI<input type="radio" name="way" value="rmi" onclick="document.forms[0].rep.value='//localhost:9999/repository'">RMI --%>
    <input type="radio" name="way" value="jndi" checked onclick="document.forms[0].rep.value='repository'">JNDI<input type="radio" name="way" value="rmi" onclick="document.forms[0].rep.value='rmirepository'">RMI/JNDI
    <input name="rep" type="text" size="30" maxlength="30" value="repository">
    <input name="submit" type="submit" value="select">
  </form>
</body>  
</html>

