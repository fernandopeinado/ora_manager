<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="t"%>
<t:graph-portlet title="Dashboard">
	<jsp:attribute name="content">
		<div style="padding-left: 20px; padding-right: 150px">
			<div id="cpu_chart_div" style="width: 100%; height: 320px;"></div>
		</div>
	</jsp:attribute>
	<jsp:attribute name="scripts">
		<script type="text/javascript" src="https://www.google.com/jsapi"></script>
		<script type="text/javascript">
			var matrixData = [[ 'timestamp', 'CPU', 'Scheduler', 'User I/O', 'System I/O', 'Concurrency', 'Application', 'Commit', 'Configuration', 'Administrative', 'Network', 'Other' ]
				<c:forEach var="snap" items="${snapshots}">
					<c:if test="${snap.deltaObs['CLASS;Administrative'] >= 0}">
						, [ '${snap.dateTime}', ${snap.deltaObs['CLASS;CPU']/1500}, ${snap.deltaObs['CLASS;Scheduler']/1500}, ${snap.deltaObs['CLASS;User I/O']/1500}, ${snap.deltaObs['CLASS;System I/O']/1500}, ${snap.deltaObs['CLASS;Concurrency']/1500}, ${snap.deltaObs['CLASS;Application']/1500}, ${snap.deltaObs['CLASS;Commit']/1500}, ${snap.deltaObs['CLASS;Configuration']/1500}, ${snap.deltaObs['CLASS;Administrative']/1500}, ${snap.deltaObs['CLASS;Network']/1500}, ${snap.deltaObs['CLASS;Other']/1500} ]
					</c:if>
				</c:forEach>
			];
			
			console.log(matrixData.length);
			
			$(document).ready(function(){
				var cpu = framework.timedSeries.decompose(matrixData);
			    var plot1b = $.jqplot('cpu_chart_div', cpu.data, {
			    	seriesColors: [ "#00CC00", "#CCFFCC", "#004AE7", "#0094E7", "#8B1A00", "#FF3333", "#E46800", "#5C440B", "#717354", "#9F9371", "#F06EAA" ],
					stackSeries: true,
			       	showMarker: false,
			       	seriesDefaults: {
			       		shadow: false,
						fill: true
			       	},
			       	series: cpu.labels,
			       	axes: {
			    		yaxis: {
			    			min: 0,
			    			max: 2,
			    			tickInterval: 0.2
			    	  	},
			           	xaxis: {
			            	renderer: $.jqplot.DateAxisRenderer,
			            	labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
			                tickRenderer: $.jqplot.CanvasAxisTickRenderer,
			                tickOptions: {
			                    formatString:'%H:%M',
			                    fontSize: '8pt'
			                },
			            	min: cpu.minTime, 
			            	max: cpu.maxTime,
			            	numberTicks: 12,
			           	}
			       	},
			       	title: {
			       		text: 'Principal Atividade'
			       	},
			       	legend: {
			            show: true,
			            location: 'ne',
			            placement: 'outside'
			        },
			        grid: {
			        	drawBorder: false,
			        	shadow: false,
			        	background: '#FFFFFF',
			        	gridLineColor: '#E5E5E5'
			        }
				});
			});
		</script>
	</jsp:attribute>
</t:graph-portlet>


