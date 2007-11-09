<%@ page import='java.util.*' %>
<%@ page import='javax.jcr.*' %>
<%@ page import='org.exoplatform.frameworks.jcr.*' %>
<%@ page import='org.exoplatform.frameworks.jcr.command.*' %>
<%@ page import='org.exoplatform.frameworks.jcr.command.web.*' %>
<%@ page import='org.exoplatform.frameworks.jcr.web.*' %>
<%@ page import='org.apache.commons.chain.*' %>
<%@ page import='org.exoplatform.container.ExoContainer' %>
<%@ page import='org.exoplatform.services.command.impl.*' %>

<%


  JCRAppSessionFactory factory = (JCRAppSessionFactory)request.getSession()
        .getAttribute(SingleRepositorySessionFactory.SESSION_FACTORY);
  GenericWebAppContext ctx = new GenericWebAppContext(getServletContext(), request, response,
          factory);
  ExoContainer container = (ExoContainer)getServletContext().
    getAttribute(WebConstants.EXO_CONTAINER);
  CommandService catalog = (CommandService)container.
    getComponentInstanceOfType(CommandService.class); 


  ctx.put(DefaultKeys.WORKSPACE, "production");
  
  Command add = catalog.getCommand("addNode");
  ctx.put("currentNode", "/");
  ctx.put("nodeType", "nt:folder");
  ctx.put("path", "content");
  add.execute(ctx);

  Command add1 = catalog.getCommand("addResourceFile");
  ctx.put("currentNode", "/content");
  ctx.put("path", "index");
  ctx.put("data", "NEW Node data");
  ctx.put("mimeType", "text/html");
  add1.execute(ctx);

  ctx.put("path", "/");
  Command save = catalog.getCommand("save");
  save.execute(ctx);

%>