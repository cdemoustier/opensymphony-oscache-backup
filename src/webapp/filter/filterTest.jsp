<%@ page import="java.util.*" %>
<head>
<title>Filter Test Page</title>
<style type="text/css">
body {font-family: Arial, Verdana, Geneva, Helvetica, sans-serif}
</style>
</head>
<body>
<a href="<%= request.getContextPath() %>/">Back to index</a><p>
<hr>
<b>Current Time</b>: <%= new Date() %><br>
<b>Current Time Millis</b>: <%= System.currentTimeMillis() %><br>
</body>
</html>
