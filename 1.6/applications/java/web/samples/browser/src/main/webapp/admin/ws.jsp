<%@ page import='java.util.*' %>
<%@ page import='javax.jcr.*' %>
<%@ page import='org.exoplatform.services.jcr.core.*' %>
<%@ page import='org.exoplatform.services.jcr.config.*' %>
<%
  Repository repository = null;
  boolean useRmi = ((String) request.getSession().getAttribute("way")).equalsIgnoreCase("rmi");
  repository = (Repository) request.getSession().getAttribute("repo");
  String repName = (String) request.getSession().getAttribute("rep");
%>

<html>
<head>
  <title>eXo Platform JCR browser sample</title>
	<link rel="stylesheet" href="../exojcrstyle.css">
</head>
<body>
  <h1>eXo Platform JCR browser sample</h1>
  <form action="." method="post" name="back">
  Repository selected: <input disabled name="rep0" type="text" maxlength="30" value="<%= repName%>">
    <input type="submit" name="submit" value="back <--">
  </form>
  <hr width="25%" align="left">
  <form action="." method="post" name="workspace">
    <h2>
    Select workspace:
    <%if (!useRmi) {%>
    <select name="ws">
    <%String[] wss = ((ManageableRepository)repository).getWorkspaceNames();%>
    <%if (wss.length > 0) {%>
      <%for (int i = 0; i < wss.length; i++) {%>
      <option><%= wss[i]%>
      <%}%>
    <%}%>
    </select>
    <%} else {%>
    <input name="ws" type="text" maxlength="30" value="production">
    <%}%>
    <input name="submit" type="submit" value="select">
    </h2>
  </form>
  <hr width="25%" align="left">
  <table cellpadding="0" cellspacing="0">
    <tr>
      <td colspan="2" align="left"><h2>Descriptors</h2></td>
    </tr>
    <tr>
      <td class="key">Name</td>
      <td class="value"><%= repository.getDescriptor(Repository.REP_NAME_DESC) %></td>
    </tr>
    <tr>
      <td class="key">Vendor</td>
      <td class="value"><%= repository.getDescriptor(Repository.REP_VENDOR_DESC) %></td>
    </tr>
    <tr>
      <td class="key">URL</td>
      <td class="value"><%= repository.getDescriptor(Repository.REP_VENDOR_URL_DESC) %></td>
    </tr>
    <tr>
      <td class="key">Repository version</td>
      <td class="value"><%= repository.getDescriptor(Repository.REP_VERSION_DESC) %></td>
    </tr>
    <tr>
      <td class="key">Spec name</td>
      <td class="value"><%= repository.getDescriptor(Repository.SPEC_NAME_DESC) %></td>
    </tr>
    <tr>
      <td class="key">Spec version</td>
      <td class="value"><%= repository.getDescriptor(Repository.SPEC_VERSION_DESC) %></td>
    </tr>
    <tr>
      <td class="key">Level 1 supported</td>
      <td class="value"><%= repository.getDescriptor(Repository.LEVEL_1_SUPPORTED) %></td>
    </tr>
    <tr>
      <td class="key">Level 2 supported</td>
      <td class="value"><%= repository.getDescriptor(Repository.LEVEL_2_SUPPORTED) %></td>
    </tr>
    <tr>
      <td class="key">Observation supported</td>
      <td class="value"><%= repository.getDescriptor(Repository.OPTION_OBSERVATION_SUPPORTED) %></td>
    </tr>
    <tr>
      <td class="key">Versioning supported</td>
      <td class="value"><%= repository.getDescriptor(Repository.OPTION_VERSIONING_SUPPORTED) %></td>
    </tr>
    <tr>
      <td class="key">Locking supported</td>
      <td class="value"><%= repository.getDescriptor(Repository.OPTION_LOCKING_SUPPORTED) %></td>
    </tr>
    <tr>
      <td class="key">Transactions supported</td>
      <td class="value"><%= repository.getDescriptor(Repository.OPTION_TRANSACTIONS_SUPPORTED) %></td>
    </tr>      
    <tr>
      <td class="key">Query SQL supported</td>
      <td class="value"><%= repository.getDescriptor(Repository.OPTION_QUERY_SQL_SUPPORTED) %></td>
    </tr>      
  </table>
  <%if (!useRmi) {%>
  <hr width="25%" align="left">
  <table cellpadding="0" cellspacing="0">
    <tr>
      <td colspan="2" align="center" class="key"><h2>Configuration</h2></td>
    </tr>
    <%
    List wsl = ((ManageableRepository)repository).getConfiguration().getWorkspaceEntries();
    if (wsl.size() > 0) {
      String[] wsens = new String[3];
      wsens[0] = "container";
      wsens[1] = "query handler";
      wsens[2] = "cache";
      for (int i = 0; i < wsl.size(); i++) {
        WorkspaceEntry wse = (WorkspaceEntry) wsl.get(i);
        MappedParametrizedObjectEntry[] wses = new MappedParametrizedObjectEntry[3];
        wses[0] = wse.getContainer();
        wses[1] = wse.getQueryHandler();
        wses[2] = wse.getCache();
    %>
    <tr>
      <td class="key"><%= wse.getName()%></td>
      <td>
        <table cellpadding="0" cellspacing="0" class="subTable">
          <%
            for (int j = 0; j < 3; j++) {
              if (wses[j] == null)
                continue;
              List prms = wses[j].getParameters();

              String wsen = wsens[j];
              if (wses[j].getType() != null)
                wsen = wsen + " {" + wses[j].getType() + "}";
              if (prms.size() > 0) {
                for (int k = 0; k < prms.size(); k++) {
                  SimpleParameterEntry pe = (SimpleParameterEntry) prms.get(k);
                %>
                <tr>
                  <%if (k == 0) {%>
                  <td rowspan="<%= (prms.size() > 0 ? prms.size() : 1)%>" width="30%" class="key"><%= wsen%></td>
                  <%}%>
                  <td class="key"><%= pe.getName()%></td> <!-- width="30%"  -->
                  <td class="value"><%= pe.getValue()%></td>
                </tr>
                <%}%>
              <%} else {%>
                <tr>
                	<td colspan="3" class="key"><h3>-- no params defined --</h3></td></tr>
            <%}%>
          <%}%>
        </table>
      </td>
    </tr>
    <%}
    }%>
  </table>
  <%}%>
</body>  
</html>
