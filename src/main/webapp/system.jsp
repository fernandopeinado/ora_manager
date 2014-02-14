<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="t"%>
<t:window title="System">
	<jsp:attribute name="content">
		<h3>Basic Information</h3>
		<c:if test="${basicInfo != null}">
			<dl class="dl-horizontal">
				<dt>Name</dt><dd>${basicInfo.name}</dd>
				<dt>Kernel</dt><dd>${basicInfo.kernel}</dd>
				<dt>Release</dt><dd>${basicInfo.release}</dd>
			</dl>
		</c:if>
		<div class="row">
			<div class="span6">
				<h3>Processor</h3>
				<c:if test="${processorInfo != null}">
				<dl class="dl-horizontal">
				<c:forEach items="${processorInfo.cpus}" var="model">
				<dt>Model</dt><dd>${model.value} x ${model.key}</dd>
				<dt>Cache</dt><dd>${processorInfo.caches[model.key]}</dd>
				</c:forEach>
				</dl>
				</c:if>
			</div>
			<div class="span6">
				<h3>Memory</h3>
				<c:if test="${memoryInfo != null}">
				<dl class="dl-horizontal">
				<dt>Total</dt><dd>${memoryInfo.total}</dd>
				<dt>Swap</dt><dd>${memoryInfo.swap}</dd>
				</dl>
				</c:if>
			</div>
		</div>
		<div class="row">
			<div class="span6">
				<h3>Network</h3>
				<c:if test="${networkInfo != null}">
				<dl class="dl-horizontal">
				<c:forEach items="${networkInfo.interfaces}" var="iface">
				<dt>${iface.key}</dt><dd>(${iface.value.mac}) ${iface.value.speed} ${iface.value.duplex}</dd>
				</c:forEach>
				</dl>
				</c:if>
			</div>
			<div class="span6">
				<h3>Storage</h3>
				<c:if test="${storageInfo != null}">
				<dl class="dl-horizontal">
				<c:forEach items="${storageInfo.devices}" var="dev">
				<dt>${dev.key}</dt><dd>${dev.value}</dd>
				</c:forEach>
				</dl>
				</c:if>
			</div>
		</div>
		<c:if test="${sysctl != null}">
		<div class="row">
			<div class="span12">
				<div class="accordion" id="sysctl">
					<div class="accordion-group">
				    	<div class="accordion-heading">
				      		<a class="accordion-toggle" data-toggle="collapse" data-parent="#sysctl" href="#sysctlContent">sysctl</a>
				    	</div>
				    	<div id="sysctlContent" class="accordion-body collapse">
				      		<div class="accordion-inner">
							<table class="table table-condensed table-striped">
							<thead><th>Property</th><th>Value</th></thead>
							<tbody>
							<c:forEach items="${sysctl.props}" var="prop">
							<tr><td>${prop.key}</td><td>${prop.value}</td></tr>
							</c:forEach>
							</tbody>
							</table>
				      		</div>
				    	</div>
				  	</div>
				</div>
			</div>
		</div>
		</c:if>
	</jsp:attribute>
	<jsp:attribute name="scripts">

	</jsp:attribute>
</t:window>