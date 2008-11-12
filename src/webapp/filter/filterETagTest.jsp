<%@ page import="java.util.*" %>
<% boolean etag = "true".equalsIgnoreCase(request.getParameter("etag"));
   if (etag) response.addHeader(com.opensymphony.oscache.web.filter.CacheFilter.HEADER_ETAG, "W/\"12345-67890\""); %>
<head>
<title>Filter ETag Test Page</title>
<style type="text/css">
body {font-family: Arial, Verdana, Geneva, Helvetica, sans-serif}
</style>
</head>
<body>
<a href="<%= request.getContextPath() %>/">Back to index</a><p>
<hr>
<b>Current Time</b>: <%= new Date() %><br>
<b>Current Time Millis</b>: <%= System.currentTimeMillis() %><br>
<b>ETag</b>: <%= etag %><br>
</body>
</html>
