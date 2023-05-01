
edu = {
		gmu: {
			csiss: {
				geoweaver:{

					desc: "a light-weight web system to allow users to easily orchestrate and execute full-stack scientific " +
							"workflows and automatically preserve model run history at a scientist-affordable cost. ",
							
					sponsor: "ESIPLab incubator project, NASA ACCESS project, NSF Geoinformatics project, NSF Cybertraining project",
					
					version: "1.0.0-rc10",
					
					author: "open source contributors",
					
					institute: "George Mason University, ESIP, University of Washington, University of Idaho, University of Texas Austin, etc"
					
				}
			}
		}
};

// GW will be the short name of the package
var GW = edu.gmu.csiss.geoweaver

//put all the shared added function here as global depenedency
String.prototype.replaceAll = function(search, replacement) {
	var target = this;
	return target.replace(new RegExp(search, 'g'), replacement);
};