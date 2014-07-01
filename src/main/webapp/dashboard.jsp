<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="t"%>
<t:window title="Dashboard">
	<jsp:attribute name="content">
    	<div class="row">
			<div class="span12">
				<iframe id="wait_analysis" src="${contextPath}/ws/agent/waitAnalysis" width="100%" height="380" seamless="seamless"></iframe>
			</div>
		</div>
	</jsp:attribute>
	<jsp:attribute name="scripts">
		<script type="text/javascript">
			framework.pageScript();
			function refreshCPU() {
				$("#wait_analysis")[0].contentWindow.location.reload();
				setTimeout(refreshCPU, 60000);
			}
			setTimeout(refreshCPU, 60000);
		</script>
	</jsp:attribute>
</t:window>