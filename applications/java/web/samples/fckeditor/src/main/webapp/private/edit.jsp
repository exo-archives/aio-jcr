<%@ page import='java.util.*' %>
<%@ page import='javax.jcr.*' %>
<%@ page import='org.exoplatform.frameworks.jcr.*' %>
<%@ page import='org.exoplatform.frameworks.jcr.command.*' %>
<%@ page import='org.exoplatform.frameworks.jcr.command.web.*' %>
<%@ page import='org.exoplatform.frameworks.jcr.web.*' %>
<%@ page import='org.apache.commons.chain.*' %>
<%@ page import='org.exoplatform.container.ExoContainer' %>
<%@ page import='org.exoplatform.services.command.impl.*' %>

<%@ page import='org.exoplatform.frameworks.jcr.web.fckeditor.*' %>

<%
String editorName = request.getParameter("editor");
if(editorName == null) {
  editorName = "FCKeditor1";
}
String editor = request.getParameter(editorName);
String path = request.getParameter("path");
if(path == null) {
  path = "/content/index";
  
  JCRAppSessionFactory factory = (JCRAppSessionFactory)session.getAttribute(SingleRepositorySessionFactory.SESSION_FACTORY);
  GenericWebAppContext ctx = new GenericWebAppContext(session.getServletContext(), request, response,
          factory);
  ExoContainer container = (ExoContainer)session.getServletContext().getAttribute(WebConstants.EXO_CONTAINER);
  CommandService catalog = (CommandService)container.getComponentInstanceOfType(CommandService.class);

  ctx.put(DefaultKeys.WORKSPACE, "production");
  
  try {
	  Command getNode = catalog.getCatalog().getCommand("getNode");
	  ctx.put("currentNode", "/");
	  ctx.put("path", "content/index");
	  getNode.execute(ctx);
  } catch(PathNotFoundException e) {
    // Node not found - create it
	  Command addNode = catalog.getCatalog().getCommand("addNode");
	  ctx.put("nodeType", "nt:folder");
	  ctx.put("path", "content");
	  addNode.execute(ctx);
	
	  Command addResourceFile = catalog.getCatalog().getCommand("addResourceFile");
	  ctx.put("currentNode", "/content");
	  ctx.put("path", "index");
	  ctx.put("data", "NEW Node data");
	  ctx.put("mimeType", "text/html");
	  addResourceFile.execute(ctx);
	
	  ctx.put("path", "/");
	  Command save = catalog.getCatalog().getCommand("save");
	  save.execute(ctx);
  }   
}

String ws = request.getParameter("workspace");

JCRContentFCKeditor oFCKeditor;
if(session.getAttribute("ed") == null) {
  oFCKeditor = new JCRContentFCKeditor(request, editorName, ws, path, "nt:file");
  FCKeditorConfigurations conf = new FCKeditorConfigurations();
  conf.put("ImageBrowserURL", "/fckeditor/FCKeditor/editor/filemanager/browser/default/browser.html?Connector=/fckeditor/connector");
  oFCKeditor.setConfig(conf);
  session.setAttribute("ed", oFCKeditor);  
} else {
  oFCKeditor = (JCRContentFCKeditor)session.getAttribute("ed");
  if(editor != null)
    oFCKeditor.saveValue(editor);
}

%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>eXo Platform JCR FCKeditor sample LOGIN ERROR</title>
	<link rel="stylesheet" href="../exojcrstyle.css">
  <script type="text/javascript" src="../FCKeditor/fckeditor.js"></script>
</head>
  <body>

   <form action="/fckeditor/private/edit.jsp?editor=<%=editorName%>&path=<%=path%>" method="post">

   <%=oFCKeditor.create()%>

   <br>
	   <input type="submit" value="Submit" />
	   </form>
   <br>

   <%=oFCKeditor.getValue()%>

  </body>
</html>