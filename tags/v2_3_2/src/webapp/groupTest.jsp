<%@ page import="java.util.*" %>
<%@ taglib uri="http://www.opensymphony.com/oscache" prefix="cache" %>

<head>
<title>Test Page</title>
<style type="text/css">
body {font-family: Arial, Verdana, Geneva, Helvetica, sans-serif}
</style>
</head>
<body>

<a href="<%= request.getContextPath() %>/">Back to index</a><p>
<hr>Flushing 'group2'...<hr>
<cache:flush group='group2' scope='application'/>
<hr>
<cache:cache key='test1' groups='group1,group2' duration='5s'>
    <b>Cache Time</b>: <%= (new Date()).getTime() %><br>
    This is some cache content test1 that is in 'group1' and 'group2'. Normally it would refresh if it
    was more than 5 seconds old, however the &lt;cache:flush group='group2' scope='application'&gt;
    tag causes this entry to be flushed on every page refresh.<br>
</cache:cache>
<hr>
<cache:cache key='test2' groups='group1' duration='5s'>
    <b>Cache Time</b>: <%= (new Date()).getTime() %><br>
    This is some cache content test2 that is in 'group1' (refreshes if more than 5 seconds old)<br>
</cache:cache>
<hr>
<cache:cache key='test3' duration='20s'>
    <b>Cache Time</b>: <%= (new Date()).getTime() %><br>
    This is some cache content test3 that is in 'group1' and 'group2'. The groups are added using the &lt;cache:addgroup /&gt; tag.<br>
    <cache:addgroup group='group1'/>
    <cache:addgroup group='group2'/>
</cache:cache>
<hr>
<cache:cache key='test4' duration='20s'>
    <b>Cache Time</b>: <%= (new Date()).getTime() %><br>
    This is some cache content test4 that is in 'group1' and 'group2'. The groups are added using the &lt;cache:addgroups /&gt; tag.<br>
    <cache:addgroups groups='group1,group2'/>
</cache:cache>
<hr>
</body>
</html>
