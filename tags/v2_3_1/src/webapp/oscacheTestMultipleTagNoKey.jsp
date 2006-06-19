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
	time='150'>

	<b>Cache Time</b>: <%= new Date() %><br>

	<b> This is some cache content refresh = 150</b><br><br>

</cache:cache>
<cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	duration='PT5M'>

	<b>Cache Time</b>: <%= new Date() %><br>

	<b> This is some cache content refresh=300</b><br><br>

</cache:cache>
<cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	duration='PT5M1S'>

	<b>Cache Time</b>: <%= new Date() %><br>

	<b> This is some cache content refresh=301</b><br><br>

</cache:cache>
<cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	time='302'>

	<b>Cache Time</b>: <%= new Date() %><br>

	<b> This is some cache content refresh=302</b><br><br>

</cache:cache>
<cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	time='303'>

	<b>Cache Time</b>: <%= new Date() %><br>

	<b> This is some cache content refresh=303</b><br><br>

</cache:cache>
<cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	duration='PT304S'>

	<b>Cache Time</b>: <%= new Date() %><br>

	<b> This is some cache content refresh=304</b><br><br>

</cache:cache>
<cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	time='305'>

	<b>Cache Time</b>: <%= new Date() %><br>

	<b> This is some cache content refresh=305</b><br><br>

</cache:cache>
<cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	duration='PT5M6S'>

	<b>Cache Time</b>: <%= new Date() %><br>

	<b> This is some cache content refresh=306</b><br><br>

</cache:cache>
<cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	time='307'>

	<b>Cache Time</b>: <%= new Date() %><br>

	<b> This is some cache content refresh=307</b><br><br>

</cache:cache>
<cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	time='308'>

	<b>Cache Time</b>: <%= new Date() %><br>

	<b> This is some cache content refresh=308</b><br><br>

</cache:cache>
<cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	time='309'>

	<b>Cache Time</b>: <%= new Date() %><br>

	<b> This is some cache content refresh=309</b><br><br>

</cache:cache>
<cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	time='310'>

	<b>Cache Time</b>: <%= new Date() %><br>

	<b> This is some cache content refresh=310</b><br><br>

</cache:cache>
<cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	time='311'>

	<b>Cache Time</b>: <%= new Date() %><br>

	<b> This is some cache content refresh=311</b><br><br>

</cache:cache>
<cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	time='312'>

	<b>Cache Time</b>: <%= new Date() %><br>

	<b> This is some cache content refresh=312</b><br><br>
</cache:cache>
<cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	time='313'>

	<b>Cache Time</b>: <%= new Date() %><br>

	<b> This is some cache content refresh=313</b><br><br>

</cache:cache>
<cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	time='314'>

	<b>Cache Time</b>: <%= new Date() %><br>

	<b> This is some cache content refresh=314</b><br><br>
</cache:cache>
<cache:cache
 	refresh='<%= refresh %>'
	scope="<%= scope %>"
	time='315'>

	<b>Cache Time</b>: <%= new Date() %><br>

	<b> This is some cache content refresh=315</b><br><br>

</cache:cache>
<cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	time='316'>

	<b>Cache Time</b>: <%= new Date() %><br>

	<b> This is some cache content refresh=316</b><br><br>

</cache:cache>
<cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	time='317'>

	<b>Cache Time</b>: <%= new Date() %><br>

	<b> This is some cache content refresh=317</b><br><br>

</cache:cache>
<cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	time='318'>

	<b>Cache Time</b>: <%= new Date() %><br>

	<b> This is some cache content refresh=318</b><br><br>

</cache:cache>
<cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	time='319'>

	<b>Cache Time</b>: <%= new Date() %><br>

	<b> This is some cache content refresh=319</b><br><br>

</cache:cache>
<cache:cache
	refresh='<%= refresh %>'
	scope="<%= scope %>"
	time='320'>

	<b>Cache Time</b>: <%= new Date() %><br>

	<b> This is some cache content refresh=320</b><br><br>

</cache:cache>
<hr>
</body>
</html>



