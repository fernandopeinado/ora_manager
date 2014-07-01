<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="t"%>
<t:graph-portlet title="Dashboard">
	<jsp:attribute name="content">
		<div style="padding-left: 20px; padding-right: 150px; padding-top: 20px;">
			<div id="cpu_chart_div" style="width: 100%; height: 320px;"></div>
		</div>
	</jsp:attribute>
	<jsp:attribute name="scripts">
		<script type="text/javascript" src="https://www.google.com/jsapi"></script>
		<script type="text/javascript">
			var matrixData = [[ 'timestamp', 'CPU', 'Scheduler', 'User I/O', 'System I/O', 'Concurrency', 'Application', 'Commit', 'Configuration', 'Administrative', 'Network', 'Other' ]
				<c:forEach var="snap" items="${snapshots}">
					<c:if test="${not empty snap.deltaObs['CPU']}">
						, [ '${snap.dateTime}', ${snap.deltaObs['CPU']}, ${snap.deltaObs['Scheduler']}, ${snap.deltaObs['User I/O']}, ${snap.deltaObs['System I/O']}, ${snap.deltaObs['Concurrency']}, ${snap.deltaObs['Application']}, ${snap.deltaObs['Commit']}, ${snap.deltaObs['Configuration']}, ${snap.deltaObs['Administrative']}, ${snap.deltaObs['Network']}, ${snap.deltaObs['Other']} ]
					</c:if>
				</c:forEach>
			];
			
			var cpuCores = ${cpuCores};
			var cpuThreads = ${cpuThreads};
			var aasAxisOptions = framework.aas.aasAxisOptions(matrixData, cpuCores, cpuThreads);

			var cpuCoresLine = {
					dashedHorizontalLine: {
						color: 'red',
						lineWidth: 1,
						y: cpuCores
					}
			}
			var cpuThreadsLine = {
					horizontalLine: {
						color: 'red',
						lineWidth: 1,
						y: cpuThreads
					}
			}
			var cpuLines = (cpuCores == cpuThreads) ? [ cpuThreadsLine ] : [ cpuCoresLine, cpuThreadsLine ];
			
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
			    			max: aasAxisOptions.max,
			    			tickInterval: aasAxisOptions.tickInterval
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
			       		text: 'Average Active Sessions'
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
			        },
			        canvasOverlay: {
						show: true,
						objects: cpuLines
			        }
				});
			});
		</script>
	</jsp:attribute>
</t:graph-portlet>
