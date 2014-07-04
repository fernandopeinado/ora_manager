<%@ attribute name="title" required="true"%>
<%@ attribute name="styles" fragment="true"%>
<%@ attribute name="content" fragment="true"%>
<%@ attribute name="scripts" fragment="true"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<t:initvars />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>${title}</title>
<link href="${contextPath}/lib/bootstrap/css/bootstrap.css" rel="stylesheet">
<link href="${contextPath}/lib/bootstrap/css/bootstrap-responsive.css" rel="stylesheet">
<jsp:invoke fragment="styles" />
</head>
<body data-contextpath="${contextPath}" style="padding-left: 0px; padding-right: 0px;">
	<jsp:invoke fragment="content" />
	<script src="${contextPath}/lib/jquery/jquery.js"></script>
	<script src="${contextPath}/lib/bootstrap/js/bootstrap.js"></script>
	<script src="${contextPath}/js/framework.js"></script>
	<jsp:invoke fragment="scripts" />
</body>
</html>