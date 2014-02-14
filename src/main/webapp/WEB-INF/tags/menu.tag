<%@ attribute name="menuActive" required="true" %>
<li class="dropdown">
	<a href="#" class="dropdown-toggle" data-toggle="dropdown">Menu <b class="caret"></b></a>
	<ul class="dropdown-menu">
		<li role="presentation"><a role="menuitem" tabindex="-1" href="${contextPath}">Home</a></li>
		<li role="presentation"><a role="menuitem" tabindex="-1" href="${contextPath}/ws/dashboard">Dashboard</a></li>
		<li role="presentation"><a role="menuitem" tabindex="-1" href="${contextPath}/ws/database/size">Database Size</a></li>
		<li role="presentation"><a role="menuitem" tabindex="-1" href="${contextPath}/ws/system">System</a></li>
	</ul>
</li>