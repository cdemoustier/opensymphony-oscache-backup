<%@ page import="java.util.*" %>
<%@ taglib uri="http://www.opensymphony.com/oscache" prefix="cache" %>

<%
String scope = "application";
if (request.getParameter("scope") != null)
{
	scope = request.getParameter("scope");
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

<form action="cachetest.jsp">
	<input type="checkbox" name="refresh" value="now"> Refresh <br>
	<b>Scope</b> : <select name="scope">
		<option value="application" <%= scope.equals("application") ? "selected" : "" %>>Application
		<option value="session" <%= scope.equals("session") ? "selected" : "" %>>Session
		<option value="request" <%= scope.equals("request") ? "selected" : "" %>>Request
		<option value="page" <%= scope.equals("page") ? "selected" : "" %>>Page
	</select> <br>
	<input type="submit" value="Refresh">
</form>

<% Date start = new Date(); %> <b>Start Time</b>: <%= start %><p>
<hr>
 <%-- Note that we have to supply a cache key otherwise the 'refresh' parameter
         causes the refreshed page to end up with a different cache key! --%>
<cache:cache key="test"
	refresh='<%= request.getParameter("refresh") == null ? false : true %>'
	scope="<%= scope %>">
	<b>Cache Time</b>: <%= new Date() %><br>
	<% try { %>
	Inside try block. <br>
	<%
	Thread.sleep(1000L); // Kill some time
	if ((new Date()).getTime() % 5 == 0)
	{
		System.out.println("THROWING EXCEPTION....");
		throw new Exception("ack!");
	}
	%>
	<p>

	<% }
	catch (Exception e)
	{
	%>
		Using cached content: <cache:usecached />
	<%
	}
	%>
</cache:cache>
<hr>
<b>End Time</b>: <%= new Date() %><p>

<b>Running Time</b>: <%= (new Date()).getTime() - start.getTime() %> ms.<p>
</body>