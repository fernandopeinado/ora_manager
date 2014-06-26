<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="t"%>
<t:window title="Dashboard">
	<jsp:attribute name="content">
    	<div class="row">
			<div class="span12">
				<iframe id="cpu_graph" src="${contextPath}/ws/agent/waitAnalisys" width="100%" height="350" seamless="seamless"></iframe>
			</div>
		</div>
    	<div class="row">
			<div class="span4">
				<iframe id="top_sql" src="${contextPath}/ws/agent/waitAnalisys/topSql" width="100%" height="380" seamless="seamless"></iframe>
			</div>
		</div>
	</jsp:attribute>
	<jsp:attribute name="scripts">
		<script type="text/javascript">
			framework.pageScript();
			function refreshCPU() {
				$("#cpu_graph")[0].contentWindow.location.reload();
				$("#top_sql")[0].contentWindow.location.reload();
				setTimeout(refreshCPU, 60000);
			}
			setTimeout(refreshCPU, 60000);
		</script>
	</jsp:attribute>
</t:window>