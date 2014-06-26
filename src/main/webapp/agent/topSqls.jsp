<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="t"%>
<t:graph-portlet title="Top SQLs">
	<jsp:attribute name="content">
	<table class="table table-striped table-condensed">
		<caption>Top Database Sizes - ${timestamp}</caption>
		<thead>
			<tr>	
				<th>ID</th>
				<th>%CPU</th>
			</tr>
		</thead>
		<tbody>
		<c:forEach var="topSql" items="${topSqls}">
			<tr>
				<td><a href="#" title="${topSql.sql}">${topSql.sqlId}</a></td>
				<td width="50"><fmt:formatNumber minFractionDigits="2" maxFractionDigits="2" value="${topSql.percent}"></fmt:formatNumber></td>
			</tr>
		</c:forEach>
		</tbody>
	</table>		
	</jsp:attribute>
	<jsp:attribute name="scripts">
	</jsp:attribute>
</t:graph-portlet>