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

boolean forceCacheUse = false;
if (request.getParameter("forceCacheUse") != null)
{
	forceCacheUse = true ;
}
%>
<head>
<title>Cron Test Page</title>
<style type="text/css">
body {font-family: Arial, Verdana, Geneva, Helvetica, sans-serif}
</style>
</head>
<body>

<a href="<%= request.getContextPath() %>/">Back to index</a><p>
<em>The cached content for the current day of the week should expire every minute.
Try setting your system clock to a couple of minutes before midnight and watch what
happens when you refresh the page as the day rolls over.</em>
<hr>
<b>Time this page was last refreshed: </b>: <%= new Date() %><br>
<hr>
 <cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	key="cronTest1"
	cron="* * * * Sunday">

	<b>Cache Time</b>: <%= new Date() %><br>

	<b>This is some cache content (expires according to the cron expression &quot;* * * * Sunday&quot;)</b><br><br>
	<%
	if (forceCacheUse)
	{
	%>
		<cache:usecached />
	<%
	}
	%>
</cache:cache>
<hr>
 <cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	key="cronTest2"
	cron="* * * * Monday">

	<b>Cache Time</b>: <%= new Date() %><br>

	<b>This is some cache content (expires according to the cron expression &quot;* * * * Monday&quot;)</b><br><br>
	<%
	if (forceCacheUse)
	{
	%>
		<cache:usecached />
	<%
	}
	%>
</cache:cache>
<hr>
 <cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	key="cronTest3"
	cron="* * * * Tuesday">

	<b>Cache Time</b>: <%= new Date() %><br>

	<b>This is some cache content (expires according to the cron expression &quot;* * * * Tuesday&quot;)</b><br><br>
	<%
	if (forceCacheUse)
	{
	%>
		<cache:usecached />
	<%
	}
	%>
</cache:cache>
<hr>
 <cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	key="cronTest4"
	cron="* * * * Wednesday">

	<b>Cache Time</b>: <%= new Date() %><br>

	<b>This is some cache content (expires according to the cron expression &quot;* * * * Wednesday&quot;)</b><br><br>
	<%
	if (forceCacheUse)
	{
	%>
		<cache:usecached />
	<%
	}
	%>
</cache:cache>
<hr>
 <cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	key="cronTest5"
	cron="* * * * Thursday">

	<b>Cache Time</b>: <%= new Date() %><br>

	<b>This is some cache content (expires according to the cron expression &quot;* * * * Thursday&quot;)</b><br><br>
	<%
	if (forceCacheUse)
	{
	%>
		<cache:usecached />
	<%
	}
	%>
</cache:cache>
<hr>
 <cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	key="cronTest6"
	cron="* * * * Friday">

	<b>Cache Time</b>: <%= new Date() %><br>

	<b>This is some cache content (expires according to the cron expression &quot;* * * * Friday&quot;)</b><br><br>
	<%
	if (forceCacheUse)
	{
	%>
		<cache:usecached />
	<%
	}
	%>
</cache:cache>
<hr>
 <cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	key="cronTest7"
	cron="* * * * Saturday">

	<b>Cache Time</b>: <%= new Date() %><br>

	<b>This is some cache content (expires according to the cron expression &quot;* * * * Saturday&quot;)</b><br><br>
	<%
	if (forceCacheUse)
	{
	%>
		<cache:usecached />
	<%
	}
	%>
</cache:cache>
<hr>
</body>
</html>
