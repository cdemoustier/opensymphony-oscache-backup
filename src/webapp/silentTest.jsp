<%@ page import="java.util.*" %>
<%@ taglib uri="http://www.opensymphony.com/oscache" prefix="cache" %>

<head>
<title>Test Page - Silent Mode</title>
<style type="text/css">
body {font-family: Arial, Verdana, Geneva, Helvetica, sans-serif}
</style>
</head>
<body>

<a href="<%= request.getContextPath() %>/">Back to index</a><p>
<hr>

Testing the silent attribute...<br/>
<cache:cache key="silentTest" mode="silent" duration="10s">
This content was inserted into the cache silently. Duration = 10 seconds. Current time = <%= new Date() %>
</cache:cache>
<br/>Some content has been silently cached<br/>
<hr>
<cache:cache key="silentTest">
You should never get to see this text!
</cache:cache>

</body>
</html>
