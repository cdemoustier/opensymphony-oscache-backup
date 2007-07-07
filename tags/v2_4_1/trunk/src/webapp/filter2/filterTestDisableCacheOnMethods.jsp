<%@ page import="java.util.*" %>
<head>
<title>Filter Test Disable Cache On Methods Page</title>
<style type="text/css">
body {font-family: Arial, Verdana, Geneva, Helvetica, sans-serif}
</style>
</head>
<body>
<a href="<%= request.getContextPath() %>/">Back to index</a><p>
<hr>
<b>This is the POST vs. GET cache content example (refresh time = 60 seconds).</b><br>
<br>
<b>Time this page was last refreshed: </b>: <%= new Date() %><br><br>
<b>Current Time Millis</b>: <%= System.currentTimeMillis() %><br>
<br>
<a href="filterTestDisableCacheOnMethods.jsp">GET with the cache</a><p>
<br>
<br>
<form action="filterTestDisableCacheOnMethods.jsp" method="post">
	<input type="submit" value="POST without the cache">
</form>
</body>
</html>
