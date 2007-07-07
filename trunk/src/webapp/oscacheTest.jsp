<%@ page import="java.util.*" %>
<%@ taglib uri="http://www.opensymphony.com/oscache" prefix="cache" %>

<%
String scope = "application";
if (request.getParameter("scope") != null)
{
	scope = request.getParameter("scope");
}

boolean refresh = false;
if (request.getParameter("refresh") != null)
{
	refresh = true;
}

boolean forcecacheuse = false;
if (request.getParameter("forcecacheuse") != null)
{
	forcecacheuse = true ;
}
%>
<head>
<title>Test Page</title>
<style type="text/css">
body {font-family: Arial, Verdana, Geneva, Helvetica, sans-serif}
</style>
</head>
<body>

<a href="<%= request.getContextPath() %>/">Back to index</a><p>
<hr>
 <cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	key="oscacheTest"
	duration="2s">

	<b>Cache Time</b>: <%= new Date() %><br>

	<b>This is some cache content (refresh = 2 seconds)</b><br><br>
	<%
	if (forcecacheuse)
	{
	%>
		<cache:usecached />
	<%
	}
	%>
</cache:cache>
</body>
</html>
