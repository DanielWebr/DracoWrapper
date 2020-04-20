$(document).ready(function() {
	//displayGraph.call(177);
});
datasetArray = [];
var globInc = 0;
var graphData;
var ctx = $("#mycanvas");

var createDataArray = function (arrayOfAlgos) {
	//console.log(arrayOfAlgos);
	//console.log(this);
	for (var querID in this) {
		globInc = querID;
		console.log(arrayOfAlgos[globInc]);
		$.ajax({
			url: ("http://students.kiv.zcu.cz/~matusik/WEB/getChartData.php?arg1=" + this[globInc]),
			method: "GET",
			async: false,
			success: function(data) {
				var dispScatter = [];
				for(var i in data) {
					dispScatter[i] = { x: data[i].x, y: data[i].y};
				}
				
				var dynamicColors = function() {
					var r = Math.floor(Math.random() * 255);
					var g = Math.floor(Math.random() * 255);
					var b = Math.floor(Math.random() * 255);
					return "rgb(" + r + "," + g + "," + b + ")";
				};
				randCol = dynamicColors();
				var dataset = {
					label: arrayOfAlgos[globInc],
					backgroundColor: randCol,
					borderColor: randCol,
					hoverBackgroundColor: 'rgba(59,89,152,1)',
					hoverBorderColor: 'rgba(59,89,152,1)',
					data: dispScatter,
					fill: false,
					lineTension: 0
				};
				datasetArray[globInc] = dataset;
				//console.log(dataset);
			},
			error: function(data) {
				console.log(data);
			}
		});
	}
	//console.log(datasetArray);
	displayGraph();
};

var displayGraph = function() {
	var data = {
		datasets: datasetArray
	};
	//console.log(data);
	var checkBox = document.getElementById("myCheck");
	var yAxis;
	if (checkBox.checked == true){
		yAxis = [{
			type: 'logarithmic',
			scaleLabel: {
				display: true,
				labelString: 'distortion',
				fontSize: 32
			}
		}]
	}
	else {
		yAxis = [{
			type: 'linear',
			scaleLabel: {
				display: true,
				labelString: 'distortion',
				fontSize: 32
			}
		}]
	}
	graphData = {
		type: 'line',
		data: data,
		options: {
			scales: {
				xAxes: [{
					type: 'linear',
					scaleLabel: {
						display: true,
						labelString: 'bitrate',
						fontSize: 32
					}					
				}],
				yAxes: yAxis
			}
		}
	};
	var LineGraph = new Chart(ctx, graphData);
}

$("#save-btn").click(function() {
	var myCanvas = $("#mycanvas");
	var myCanvasContext = myCanvas[0].getContext("2d");

	//var context = document.getElementById("chart-container").getContext("2d");

	var lineChart = new Chart(myCanvasContext, graphData); // Works fine

	// tweak the lib according to sspecht @ https://stackoverflow.com/questions/45563420/exporting-chart-js-charts-to-svg-using-canvas2svg-js
	tweakLib();
	// deactivate responsiveness and animation
	graphData.options.responsive = false;
	graphData.options.animation = false;

	// canvas2svg 'mock' context
	var svgContext = C2S(640,480);


	// new chart on 'mock' context fails:
	var mySvg = new Chart(svgContext, graphData);
	// Failed to create chart: can't acquire context from the given item

	
	var svgSerial = svgContext.getSerializedSvg(true);
	var svgJson = JSON.stringify(svgSerial);
	var blob = new Blob([svgSerial], {type: 'image/svg+xml'});
	saveAs(blob, "chart.svg");
	//console.log(svgSerial);
});

function tweakLib(){
  C2S.prototype.getContext = function (contextId) {
    if (contextId=="2d" || contextId=="2D") {
        return this;
    }
    return null;
  }

  C2S.prototype.style = function () {
      return this.__canvas.style
  }

  C2S.prototype.getAttribute = function (name) {
      return this[name];
  }

  C2S.prototype.addEventListener =  function(type, listener, eventListenerOptions) {  
    console.log("canvas2svg.addEventListener() not implemented.")
  }
}
