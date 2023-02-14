

GW.chart = {
		
		history_chart: {},
		
		chartColors : {
				red: 'rgb(255, 99, 132)',
				orange: 'rgb(255, 159, 64)',
				yellow: 'rgb(255, 205, 86)',
				green: 'rgb(75, 192, 192)',
				blue: 'rgb(54, 162, 235)',
				purple: 'rgb(153, 102, 255)',
				grey: 'rgb(201, 203, 207)'
			},
			
		MONTHS : [
				'January',
				'February',
				'March',
				'April',
				'May',
				'June',
				'July',
				'August',
				'September',
				'October',
				'November',
				'December'
			],

		COLORS : [
				'#4dc9f6',
				'#f67019',
				'#f53794',
				'#537bc4',
				'#acc236',
				'#166a8f',
				'#00a950',
				'#58595b',
				'#8549ba'
			],

		utils : {
			// Adapted from http://indiegamr.com/generate-repeatable-random-numbers-in-js/
			srand: function(seed) {
				this._seed = seed;
			},

			rand: function(min, max) {
				var seed = this._seed;
				min = min === undefined ? 0 : min;
				max = max === undefined ? 1 : max;
				this._seed = (seed * 9301 + 49297) % 233280;
				var res =  min + (this._seed / 233280) * (max - min);
				return res;
			},

			numbers: function(config) {
				var cfg = config || {};
				var min = cfg.min || 0;
				var max = cfg.max || 1;
				var from = cfg.from || [];
				var count = cfg.count || 8;
				var decimals = cfg.decimals || 8;
				var continuity = cfg.continuity || 1;
				var dfactor = Math.pow(10, decimals) || 0;
				var data = [];
				var i, value;

				for (i = 0; i < count; ++i) {
					value = (from[i] || 0) + this.rand(min, max);
					if (this.rand() <= continuity) {
						data.push(Math.round(dfactor * value) / dfactor);
					} else {
						data.push(null);
					}
				}

				return data;
			},

			labels: function(config) {
				var cfg = config || {};
				var min = cfg.min || 0;
				var max = cfg.max || 100;
				var count = cfg.count || 8;
				var step = (max - min) / count;
				var decimals = cfg.decimals || 8;
				var dfactor = Math.pow(10, decimals) || 0;
				var prefix = cfg.prefix || '';
				var values = [];
				var i;

				for (i = min; i < max; i += step) {
					values.push(prefix + Math.round(dfactor * i) / dfactor);
				}

				return values;
			},

			months: function(config) {
				var cfg = config || {};
				var count = cfg.count || 12;
				var section = cfg.section;
				var values = [];
				var i, value;

				for (i = 0; i < count; ++i) {
					value = MONTHS[Math.ceil(i) % 12];
					values.push(value.substring(0, section));
				}

				return values;
			},

			color: function(index) {
				return COLORS[index % COLORS.length];
			},

			transparentize: function(color, opacity) {
				var alpha = opacity === undefined ? 0.5 : 1 - opacity;
				return Color(color).alpha(alpha).rgbString();
			}
		},

		randomScalingFactor : function() {
				var num =  Math.round(this.utils.rand(-100, 100));
//				console.log("numbers: " + num)
				return num
		},
		
		getYYYYMMDD: function(date){
			
			var yyyy = date.getFullYear();
			
			var mm = date.getMonth() + 1;
			
			var mm = (mm>9 ? '' : '0') + mm;
			
			var dd = date.getDate();
			
			var dd = (dd>9 ? '' : '0') + dd;
			
			return yyyy + mm + dd;
			
		},
		
		isInSameDay: function(date1, date2){
			
			var day1 = this.getYYYYMMDD(date1);
			
			var day2 = this.getYYYYMMDD(date2);
			
			var isin = false;
			
			if(day1==day2){
				
				isin  =true;
				
			}
			
			return isin;
			
		},
		
		renderUtil: function(type, msg){
			
			this.utils.srand(Date.now());
			
			var labels = [], succeed = [], failed = [], running = [], unknown = [];
						
			//			{
			// 	"history_id":"BSoARJLDm5cD",
			// 	"history_input":"\n",
			// 	"history_output":"3.9.7 \n",
			// 	"history_begin_time":1641022884104,
			// 	"history_end_time":1641022889227,
			// 	"history_notes":null,
			// 	"history_process":"beqbtr",
			// 	"host_id":null,
			// 	"indicator":"Done"
			//  }
						
			//sort the array ascending from early to later
			
			for(var i=0;i<msg.length;i+=1){
				
				var current_time = new Date(msg[i].history_begin_time);
				
				for(var j=i+1;j<msg.length;j+=1){
					
					var next_time = new Date(msg[j].history_begin_time);
					
					if(current_time.getTime() > next_time.getTime()){
						
						var swap = msg[i];
						msg[i] = msg[j];
						msg[j] = swap;
						current_time = new Date(msg[i].begin_time);
						
					}
					
				}
				
			}

			console.log("Sorted Array: ", msg);
			
			//count how many runs on each day
			
			var previous = null;
			
			var suc_times = 0, fail_times = 0, running_times = 0, unknown_times = 0;
			
			for(var i=0;i<msg.length;i+=1){
				
				var current = new Date(msg[i].history_begin_time);
				
				if(previous==null){
					
					previous = current;
					
					labels.push(this.getYYYYMMDD(current));
					
					if(msg[i].indicator=="Done"){
						
						suc_times += 1
						
					}else if(msg[i].indicator == "Failed"){
						
						fail_times += 1
						
					}else if(msg[i].indicator == "Running"){
						
						running_times += 1
						
					}else{
						
						unknown_times += 1
						
					}
					
					if(msg.length==1){
						
						succeed.push(suc_times);
						
						failed.push(fail_times);
						
						running.push(running_times);
						
						unknown.push(unknown_times);
						
					}
					
				}else if(this.isInSameDay(current, previous)){
					
					if(msg[i].indicator=="Done"){
						
						suc_times += 1
						
					}else if(msg[i].indicator == "Failed"){
						
						fail_times += 1
						
					}else if(msg[i].indicator == "Running"){
						
						running_times += 1
						
					}else{
						
						unknown_times += 1
						
					}
					
					if(i==(msg.length-1)){
						
						succeed.push(suc_times);
						
						failed.push(fail_times);
						
						running.push(running_times);
						
						unknown.push(unknown_times);
						
					}
					
				}else if(!this.isInSameDay(current, previous)){
					
					previous = current;
					
					succeed.push(suc_times);
					
					failed.push(fail_times);
					
					running.push(running_times);
					
					unknown.push(unknown_times);
					
					labels.push(this.getYYYYMMDD(current));
					
					suc_times = 0, fail_times = 0, running_times = 0, unknown_times = 0;
					
					if(msg[i].indicator=="Done"){
						
						suc_times += 1
						
					}else if(msg[i].indicator == "Failed"){
						
						fail_times += 1
						
					}else if(msg[i].indicator == "Running"){
						
						running_times += 1
						
					}else{
						
						unknown_times += 1
						
					}
						
					if(i==(msg.length-1)){
						
						succeed.push(suc_times);
						
						failed.push(fail_times);
						
						running.push(running_times);
						
						unknown.push(unknown_times);
						
					}
					
				}
				
			}
			
			console.log(labels)
			
			console.log(succeed);
			
			
			var ctx = document.getElementById(type + '-history-chart').getContext('2d');
			var config = {
				type: 'line',
				data: {
					labels: labels,
					datasets: [{
						label: 'Running',
						fill: false,
						backgroundColor: this.chartColors.blue,
						borderColor: this.chartColors.blue,
						borderDash: [5, 5],
						data: running,
					}, {
						label: 'Done',
						fill: false,
						backgroundColor: this.chartColors.green,
						borderColor: this.chartColors.green,
						borderDash: [5, 5],
						data: succeed,
					}, {
						label: 'Failed',
						backgroundColor: this.chartColors.red,
						borderColor: this.chartColors.red,
						borderDash: [5, 5],
						data: failed
					}, {
						label: 'Unknown',
						backgroundColor: this.chartColors.grey,
						borderColor: this.chartColors.grey,
						borderDash: [5, 5],
						data: unknown
					}]
				},
				options: {
					responsive: true,
					title: {
						display: true,
						text: type + ' Execution History Chart'
					},
					tooltips: {
						mode: 'index',
						intersect: false,
					},
					hover: {
						mode: 'nearest',
						intersect: true
					},
					scales: {
						xAxes: [{
							display: true,
							scaleLabel: {
								display: true,
								labelString: 'Date'
							}
						}],
						yAxes: [{
							display: true,
							scaleLabel: {
								display: true,
								labelString: 'Number of Runs'
							}
						}]
					}
				}
			};

			this.history_chart[type] = new Chart(ctx, config);
			
		},

		
		renderProcessHistoryChart: function(msg){
			msg = msg.sort(function(x, y) {
				return x['history_begin_time'] - y['history_begin_time'];
			})
			this.renderUtil("process", msg);
			
		},
		
		renderWorkflowHistoryChart: function(msg){
			
			this.renderUtil("workflow", msg);
			
		},
		
		renderHostHistoryChart: function(msg){
			
			this.renderUtil("host", msg);
			
		}
		
}
