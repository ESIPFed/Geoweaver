/**
 * All things related to Host section on the left menu: host listing, creation, browsing, and editing. 
 */
GW.host = {
        
	cred_cache: [{"h":"xxxx", "s": "yyyyy", "env": {"bin":"python3", "pyenv": "cdl"}}],
	
	host_environment_list_cache: null,

	password_frame: null,
	
	ssh_password_frame: null,
	
	new_host_frame: null,
	
	local_hid: null,
	
	editOn: false,
	
	clearCache: function(){
		
		this.cred_cache = [];

		this.host_environment_list_cache = [];
		
	},

	checkIfHostPanelActive: function(){

		return document.getElementById("main-host-info").style.display=="flex";

	},
	
	setEnvCache: function(hid, env){
		
		var is = false;
		
		for(var i=0;i<GW.host.cred_cache.length;i++){
			
			if(GW.host.cred_cache[i].h == hid){
				
				GW.host.cred_cache[i].env = env;
				
				is = true;
				
				break;
				
			}
			
		}
		
		if(!is){
			
			GW.host.cred_cache.push({"h": hid, "env": env});
			
		}
		
	},
	
	setCache: function(hid, s){
		
		var is = false;
		
		for(var i=0;i<GW.host.cred_cache.length;i++){
			
			if(GW.host.cred_cache[i].h == hid){
				
				GW.host.cred_cache[i].s = s;
				
				is = true;
				
				break;
				
			}
			
		}
		
		if(!is){
			
			GW.host.cred_cache.push({"h": hid, "s": s});
			
		}
		
	},
	
	findEnvCache: function(hid){
		
		var env = null;
		
		for(var i=0;i<GW.host.cred_cache.length;i++){
			
			if(GW.host.cred_cache[i].h == hid){
				
				env = GW.host.cred_cache[i].env;
				
				break;
				
			}
			
		}
		
		return env;
		
	},
	
	findCache: function(hid){
		
		var s = null;
		
		for(var i=0;i<GW.host.cred_cache.length;i++){
			
			if(GW.host.cred_cache[i].h == hid){
				
				s = GW.host.cred_cache[i].s;
				
				break;
				
			}
			
		}
		
		return s;
		
	},
	
	isLocal: function(msg){
		
		var is = false;
		
		if(msg.ip=="127.0.0.1"){
			
			is = true;
			
		}
		
		return is;
		
	},
	
	encrypt: function(hid, pstext, req, dialog, button, business_callback){
		
		//Two-step encryption is applied here. 
		//First, get public key from server.
		//Second, encrypt the password and sent the encypted string to server. 
		$.ajax({
			
			url: "key",
			
			type: "POST",
			
			data: ""
			
		}).done(function(msg){
			
			//encrypt the password using the received rsa key
			
			msg = $.parseJSON(msg);
			
			var encrypt = new JSEncrypt();
			
			encrypt.setPublicKey(msg.rsa_public);
			
			var encrypted = encrypt.encrypt(pstext);
			
//                msg.pswd = encrypted;
			
			business_callback(encrypted, req, dialog, button);
			
		}).fail(function(jxr, status){
			
		});
		
	},
	
	enter_password: function(hid, req, business_callback){
		
		if(this.password_frame != null){
			
			try{
			
				this.password_frame.closeFrame();
				
			}catch(e){}
			
			this.password_frame = null;
			
		}
		
		var content = '<div class="modal-body">'+
		   '   <div class="form-group row required" style="font-size: 12px;">'+
		   '     <label for="host password" class="col-sm-4 col-form-label control-label">Input Host User Password: </label>'+
		   '     <div class="col-sm-6">'+
		   '        <input type=\"password\" class=\"form-control\" id=\"inputpswd\" placeholder=\"Password\" >'+
		   '     </div>'+
		   '     <div class="col-sm-12 form-check">'+
		   '        <input type="checkbox" class="form-check-input" id="remember" />'+
		   '        <label class="form-check-label" for="remember">Remember password</label>'+
		   '     </div>'+
		   '   </div></div>';
		
		content += '<div class="modal-footer">' +
			"   <button type=\"button\" id=\"pswd-confirm-btn\" class=\"btn btn-outline-primary\">Confirm</button> "+
			"   <button type=\"button\" id=\"pswd-cancel-btn\" class=\"btn btn-outline-secondary\">Cancel</button>"+
			'</div>';
		
		this.password_frame = GW.process.createJSFrameDialog(520, 340, content, "Host Password")
		
		$("#inputpswd").on('keypress',function(e) {
			
			if(e.which == 13) {
				
				$("#pswd-confirm-btn").click();
				
			}
			
		}); 
		
		$("#pswd-confirm-btn").click(function(){
			
			$('#pswd-confirm-btn').prop('disabled', true);
			
//              dialogItself.enableButtons(false);
			
			if(document.getElementById('remember').checked) {
				
				GW.host.setCache(hid, $('#inputpswd').val()); //remember s
				
			}
			
			GW.host.encrypt(hid, $('#inputpswd').val(), req, GW.host.password_frame, $('#pswd-confirm-btn'), business_callback);
				
		});
		
		$("#pswd-cancel-btn").click(function(){
			
			GW.host.password_frame.closeFrame();
			
		});
		
	},
	
	start_auth_single: function(hid, req, business_callback){
		
		var s = GW.host.findCache(hid);
		
		// if(hid == GW.host.local_hid){
			
		//  GW.host.encrypt(hid, "local", req, null, null, business_callback);
			
		// }else 
		if(s==null){
			
			GW.host.enter_password(hid, req, business_callback);
			
		}else{
			
			GW.host.encrypt(hid, s, req, null, null, business_callback);
			
		}
		
	},
	
	encrypt_m : function(hosts, pswds, req, dialogItself, button, business_callback){
		
		//Two-step encryption is applied here. 
		//First, get public key from server.
		//Second, encrypt the password and sent the encypted string to server. 
		$.ajax({
			
			url: "key",
			
			type: "POST",
			
			data: ""
			
		}).done(function(msg){
			
			//encrypt the password using the received rsa key
			
			msg = $.parseJSON(msg);
			
			var encrypt = new JSEncrypt();
			
			encrypt.setPublicKey(msg.rsa_public);
			
			var encrypt_passwds = [];
			
			for(var i=0; i<hosts.length; i++){

				var encrypted = encrypt.encrypt(pswds[i]);//$('#inputpswd_' + i).val());
				
				encrypt_passwds.push(encrypted);
				
			}
			
			var ids = GW.host.turnHosts2Ids(hosts);

			var envs = GW.host.turnHosts2EnvIds(hosts);
			
			req.hosts = ids;
			
			req.passwords = encrypt_passwds;

			req.envs = envs;
			
			business_callback(req, dialogItself, button);
			
		}).fail(function(jxr, status){
			 
			console.error("fail to get encrypted key");
			
		});
		
	},
	
	enter_pswd_m : function(newhosts, hosts, req, business_callback){
		
		var content = '<div class="modal-body">';
		
		for(var i=0;i<newhosts.length;i++){
			
			content += '<div class="form-group row required">'+
			   '     <label for="host password" class="col-sm-4 col-form-label control-label">Host '+newhosts[i].name+' Password: </label>'+
			   '     <div class="col-sm-8">'+
			   '        <input type=\"password\" class=\"form-control\" id=\"inputpswd_'+i+'\" required=\"true\" placeholder=\"Password\">'+
			   '     </div>'+
			   '   </div>';
		}
		
		content += '     <div class="form-group form-check">'+
		   '        <input type="checkbox" class="form-check-input" id="remember">'+
		   '        <label class="form-check-label" for="remember">Remember password</label>'+
		   '     </div></div>';
		
		content += '<div class="modal-footer">' +
			"   <button type=\"button\" id=\"pswd-confirm\" class=\"btn btn-outline-primary\">Confirm</button> "+
			"   <button type=\"button\" id=\"pswd-cancel\" class=\"btn btn-outline-secondary\">Cancel</button>"+
			'</div>';
		
		var frame = GW.process.createJSFrameDialog(360, 360, content, "Host Password");
		
		frame.on('#pswd-cancel', 'click', (_frame, evt) => {
			
			_frame.closeFrame()
			
		})
		
		frame.on('#pswd-confirm', 'click', (_frame, evt) => {
			
			var filled = true;
			
			$.each( $( "input[type='password']" ), function() {
				if(!$(this).val()){
					
					filled = false;
					
					alert("Please input password. ");
					
					return;
					
				}
			});
			
			if(!filled) return;
			
			var $button = $(this);
//              
//              $button.spin();
			
			var shortpasswds = [];
			
			for(var i=0;i<newhosts.length;i++){
				
				shortpasswds.push($("#inputpswd_" + i).val());
				
				if(document.getElementById('remember').checked) {
					
					GW.host.setCache(newhosts[i].id, $("#inputpswd_" + i).val());
					
				}
				
			}
			
			var passwds = GW.host.extendList(shortpasswds, newhosts, hosts);
			
			GW.host.encrypt_m(hosts, passwds, req, _frame, $button, business_callback);
			
			_frame.closeFrame()
			
		});
		
		
	},
	
	start_auth_multiple: function(hosts, req, business_callback){
		
		var newhosts = this.shrinkList(hosts);
		
		if(newhosts.length>0){
			
			GW.host.enter_pswd_m(newhosts, hosts, req, business_callback);
			
		}else{
			
			var passwds = GW.host.extendList([], newhosts, hosts);
			
			GW.host.encrypt_m(hosts, passwds, req, null, null, business_callback);
		}
		
	},
	
	turnHosts2Ids: function(hosts){
		
		var ids = [];
		
		for(var i=0; i<hosts.length; i++){
			
			ids.push(hosts[i].id);
			
		}
		
		return ids;
		
	},

	turnHosts2EnvIds: function(hosts){
		
		var ids = [];
		
		for(var i=0; i<hosts.length; i++){
			
			ids.push(hosts[i].env);
			
		}
		
		return ids;
		
	},
	
	/**
	 * Extend the list to original size
	 */
	extendList: function(shortpasswds, newhosts, hosts){
		
		var fullpasswdslist = [];
		
		for(var i=0;i<hosts.length;i++){
			
			var passwd = null;
			
			for(var j=0;j<newhosts.length;j++){
				
				if(newhosts[j].id==hosts[i].id){
					
					passwd = shortpasswds[j];
					
					break;
					
				}
				
			}
			
			if(passwd!=null)
			
				fullpasswdslist.push(passwd);
			
			else
				
				fullpasswdslist.push(GW.host.findCache(hosts[i].id));
			
		}
		
		return fullpasswdslist;
		
	},
	
	shrinkList: function(hosts){
		
		var newhosts = [];
		
		for(var i=0;i<hosts.length;i++){
			
			var exist = false;
			
			for(var j=0;j<newhosts.length;j++){
				
				if(hosts[i].id==newhosts[j].id){
					
					exist = true;
					
					break;
					
				}
				
			}
			
			if(!exist && GW.host.findCache(hosts[i].id)==null){ //the p is not cached
				
				newhosts.push(hosts[i]);
				
			}
			
		}
		
		return newhosts;
		
	},
	
	/**
	 * Close the SSH Terminal and Connection
	 */
	closeSSH: function(token){
		
		$.ajax({
			
			url: "geoweaver-ssh-logout-inbox",
			
			method: "POST",
			
			data: "token=" + token
			
		}).done(function(msg){
			
			if(msg == "done"){
			
				console.log("SSH session is closed.");
				
				$("#ssh-terminal-iframe").html("");
				
			}else{
				
				console.error("Fail to close SSH.");
				
			}
			
		}).fail(function(){
			
			console.error("Fail to close SSH.");
			
		});
		
	},
	
	/**
	 * Show the SSH Terminal Section
	 */
	showSSHCmd: function(token){
		
//          var frame = GW.process.createJSFrameDialog(600, 540, "<iframe src=\"geoweaver-ssh?token="+
//                  token+"\" style=\"height:100%;width:100%;\"></iframe>", "SSH Command Line")
		
		var frame = "<h4 class=\"border-bottom\">SSH Terminal Section  <button type=\"button\" class=\"btn btn-secondary btn-sm\" id=\"closeSSHTerminal\" >close</button></h4>"+
		
		"<iframe src=\"geoweaver-ssh?token="+
		token+"\" style=\"height:700px; max-height:1000px;width:100%;\"></iframe>"
		
		$("#ssh-terminal-iframe").html(frame);
		
		$("#closeSSHTerminal").click(function(){
			
			GW.host.closeSSH(token);
			
			$("#ssh-terminal-iframe").html(""); //double remove to make sure it clears every time
			
		})
		
	},
	
	/**
	 * Open the SSH Connection Dialog if the host is a remote server
	 */
	openssh: function(hostid){
		
		//get the host information
		
		$.ajax({
			
			url: "detail",
			
			method: "POST",
			
			data: "type=host&id=" + hostid
			
		}).done(function(msg){
			
			//open the login page
			
			hostmsg = $.parseJSON(msg);
			
			if(GW.host.ssh_password_frame != null){
				
				try{
					
					GW.host.ssh_password_frame.closeFrame();
					
				}catch(e){
					
					console.log("Probably it is closed already.");
					
				}
				
				GW.host.ssh_password_frame = null;
				
			}
			
			if(GW.host.findCache(hostid)==null){
				
				var cont = '<div class="modal-body" style=\"font-size: 12px;\">'+
					"<div class=\"row\">";
				
				cont += "<div class=\"col col-md-5\">IP</div><div class=\"col col-md-5\">" + hostmsg.ip + "</div>";
				
				cont += "<div class=\"col col-md-5\">Port</div><div class=\"col col-md-5\">" + hostmsg.port + "</div>";
				
				cont += "<div class=\"col col-md-5\">User</div><div class=\"col col-md-5\">" + hostmsg.username + "</div>";
				
				cont += "<div class=\"col col-md-5\">Password</div><div class=\"col col-md-5\"><input type=\"password\" id=\"passwd\" class=\"form-control\" id=\"inputpswd\" placeholder=\"Password\"></div>";
				
				cont += "     <div class=\"col-sm-12 form-check\">"+
			   "        <input type=\"checkbox\" class=\"form-check-input\" id=\"ssh-remember\" />"+
			   "        <label class=\"form-check-label\" for=\"ssh-remember\">Remember password and don't ask again.</label>"+
			   "     </div>";
								
				cont += "</div></div>";
				
				cont += '<div class="modal-footer">' +
				"   <button type=\"button\" id=\"ssh-connect-btn\" class=\"btn btn-outline-primary\">Connect</button> "+
				"   <button type=\"button\" id=\"ssh-cancel-btn\" class=\"btn btn-outline-secondary\">Cancel</button>"+
				'</div>';
				
				GW.host.ssh_password_frame = GW.process.createJSFrameDialog(500, 340, cont, "Open SSH session")
				
				$("#ssh-connect-btn").click(function(){
					
					$("#ssh-connect-btn").prop("disabled", true);
					
					$.ajax({
						
						url: "key",
						
						type: "POST",
						
						data: ""
						
					}).done(function(msg){
						
						//encrypt the password using the received rsa key
						msg = $.parseJSON(msg);
						
						var encrypt = new JSEncrypt();
						
						encrypt.setPublicKey(msg.rsa_public);
						
						var encrypted = encrypt.encrypt($("#passwd").val());
						
						var req = {
								host: hostmsg.ip,
								port: hostmsg.port,
								username: hostmsg.username,
								password: encrypted
						}
					
						$.ajax({
							
							url: "geoweaver-ssh-login-inbox",
							
							method: "POST",
							
							data: req
							
						}).done(function(msg){
							
							msg = $.parseJSON(msg);
							
							if(msg.token!=null){
								
								//open a dialog to show the SSH command line interface

								GW.host.showSSHCmd(msg.token);
								
								if(document.getElementById('ssh-remember').checked) {
									
									GW.host.setCache(hostid, $('#passwd').val()); //only remember password if users check the box the the login is successful.
									
								}
								
							}else{
								
								alert("Username or Password is wrong or the server is not accessible");
								
							}
							try{
								GW.host.ssh_password_frame.closeFrame();
							}catch(e){console.log(e)}
							
							
						}).fail(function(status){
							
							alert("Username or Password is wrong or the server is not accessible" + status);
							
							$("#ssh-connect-btn").prop("disabled", false);
							
						});
						
						
					});
					
				});
				
				$("#ssh-cancel-btn").click(function(){
					
					GW.host.ssh_password_frame.closeFrame();
					
				});
				
			}else{
				
				//if the login attempt failed once, the password will be removed and users need input again.
				var pswd = GW.host.findCache(hostid);
				
				$.ajax({
					
					url: "key",
					
					type: "POST",
					
					data: ""
					
				}).done(function(msg){
					
					//encrypt the password using the received rsa key
					msg = $.parseJSON(msg);
					
					var encrypt = new JSEncrypt();
					
					encrypt.setPublicKey(msg.rsa_public);
					
					var encrypted = encrypt.encrypt(pswd);
					
					var req = {
							host: hostmsg.ip,
							port: hostmsg.port,
							username: hostmsg.username,
							password: encrypted
					}
				
					$.ajax({
						
						url: "geoweaver-ssh-login-inbox",
						
						method: "POST",
						
						data: req
						
					}).done(function(msg){
						
						msg = $.parseJSON(msg);
						
						if(msg.token!=null){
							
							//open a dialog to show the SSH command line interface

							GW.host.showSSHCmd(msg.token);
							
						}else{
							
							alert("Username or Password is wrong or the server is not accessible");
							
							GW.host.setCache(hostid, null);
							
						}
						
					}).fail(function(status){
						
						alert("Username or Password is wrong or the server is not accessible" + status);
						
						GW.host.setCache(hostid, null);
						//$("#ssh-connect-btn").prop("disabled", false);
						
					});
					
				});
				
			}
			
		});
		
	},

	cleanMenu: function(){

		$("#host_folder_ssh_target").html("");

		$("#host_folder_jupyter_target").html("");
		
		$("#host_folder_jupyterhub_target").html("");
		
		$("#host_folder_gee_target").html("");
	
	},

	refreshHostListForExecution:function(){

		$.ajax({
			
			url: "listhostwithenvironments",
			
			method: "POST",
			
			data: "type=host"
			
		}).done(function(msg){
			
			msg = $.parseJSON(msg);

			GW.host.host_environment_list_cache = msg;
			
			console.log("Start to refresh the host list..");
			
			GW.host.list(msg);
			
			if($(".hostselector")) {

				for(var i=0;i<msg.length;i++){
					
					//right now only SSH host can run processes
					if(msg[i].type == "ssh"){

						$(".hostselector").append("<option id=\""+msg[i].id+"\">"+msg[i].name+"</option>");

					}
					
				}

				//show the environment of the first host
				if($(".environmentselector")){

					$(".environmentselector").append('<option id="default_option">default</option>');

					var envs = msg[0].envs

					for(var i=0;i<envs.length;i++){

						$(".environmentselector").append("<option id=\""+envs[i].id+"\">"+envs[i].name+"</option>");

					}

				}

				$(".hostselector").change(function(){

					//get the corresponding environmentselector
					// var corenvelelist = $(this).closest('div').next().find('.environmentselector');
					var hostselectid = $(this).attr("id");

					var envselectid = "environmentforprocess_" + hostselectid.split("_")[1];

					//change the environment selector options
					var envselect  = $("#" + envselectid);

					var selectedhostid = $(this).children("option:selected").attr("id");

					envselect.empty().append('<option id="default_option">default</option>');

					//add new options to the environment selector
					var theenv = GW.host.findEnvironmentByHostId(selectedhostid);

					if(theenv != null){

						for(var i=0;i<theenv.length;i++){

							envselect.append("<option id=\""+theenv[i].id+"\">"+theenv[i].name+"</option>");

						}
					
					}

				});
				
			}
			
		}).fail(function(jxr, status){
			
			console.error("fail to list host");
			
		});

	},

	findEnvironmentByHostId: function(hostid){

		var theenv = null;

		if(GW.host.host_environment_list_cache!=null){

			for(var i=0;i<GW.host.host_environment_list_cache.length; i++){
				var value = GW.host.host_environment_list_cache[i];
				if(hostid == value.id){
					theenv = value.envs;
					break;
				}

			}

		}
		
		return theenv;

	},

	refreshSearchList: function(){

		GW.search.filterMenuListUtil("host_folder_ssh_target", "hosts", "host");

	},
	
	//refresh host list for the menu
	refreshHostList: function(){
		
		$.ajax({
			
			url: "list",
			
			method: "POST",
			
			data: "type=host"
			
		}).done(function(msg){
			
			msg = $.parseJSON(msg);
			
			console.log("Start to refresh the host list..");
			
			GW.host.list(msg);
			
		}).fail(function(jxr, status){
			
			console.error("fail to list host");
			
		});
		
	},
	
	addMenuItem: function(one){
		
		console.log("Add host to the tree")

		var one_item = ` <li class="host" id="host-` + one.id + 
				
				`" onclick="GW.menu.details('` + one.id + `', 'host')">&nbsp;&nbsp;&nbsp;` + 
				
				one.name + `</li>`;

		$("#host_folder_"+one.type+"_target").append(one_item);
		
	},

	expand: function(one){
		
		console.log("EXPAND host type")
		
		$("#host_folder_"+one.type+"_target").collapse("show");
	},
	
	list: function(msg){

		GW.host.cleanMenu();
		
		for(var i=0;i<msg.length;i++){
			
			this.addMenuItem(msg[i]);
			
		}
		
		$('#hosts').collapse("show");
		
	},
	
	validateIP: function(ipaddress) {  
		
		var valid = false;
	  
		if (/^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/.test(ipaddress)) {  
	  
			valid =  true  
	  
		}else{
			
			alert("You have entered an invalid IP address!")  
			
		}  
		
		return valid;
	  
	},  
	
	preEditcheck: function(){
		
		console.log("Check if the input valid");
		
		var valid = false;
		
		var hosttype = $( "#_host_type" ).text()
		
		if(hosttype=="ssh" || hosttype == ""){

			if($("#_host_name").val()&&$("#_host_ip").val()&&$("#_host_port").val()&&$("#_host_username").val()
					&&this.validateIP($("#_host_ip").val())&&$.isNumeric($("#_host_port").val())){
				
				valid = true;
				
			}
			
		}else if(hosttype=="jupyter"){
			
			if($("#_host_name").val()&&$("#_host_url").val()){
				
				valid = true;
				
			}
			
		}else if(hosttype=="gee"){
			
			if($("#_host_name").val()&&$("#_host_client_id").val()){
				
				valid = true;
				
			}
			
		}
		
		return valid;
		
		
	},
	
	precheck: function(){
		
		var valid = false;
		
		var hosttype = $( "#hosttype option:selected" ).val()
		
		if(hosttype=="ssh"){

			if($("#hostname").val()&&$("#hostip").val()&&$("#hostport").val()&&$("#username").val()
					&&this.validateIP($("#hostip").val())&&$.isNumeric($("#hostport").val())){
				
				valid = true;
				
			}
			
		}else if(hosttype=="jupyter" || hosttype=="jupyterhub" || hosttype=="jupyterlab"){
			
			if($("#hostname").val()&&$("#jupyter_home_url").val()){
				
				valid = true;
				
			}
			
		}else if(hosttype=="gee"){
			
			if($("#hostname").val()&&$("#client_id").val()){
				
				valid = true;
				
			}
			
		}
		
		return valid;
		
	},

	
	
	add: function(callback){
		
		if(this.precheck()){
			
			var hostport = ""
			
			if(typeof $("#hostport").val() != "undefined"){
				
				hostport = $("#hostport").val()
				
			}
			
			var hostip = ""
				
			if(typeof $("#hostip").val() != "undefined"){
				
				hostip = $("#hostip").val()
				
			}
			
			var hosttype = $( "#hosttype option:selected" ).val()
			
			var jupyter_url = ""
				
			if(typeof $("#jupyter_home_url").val() != "undefined"){
				
				jupyter_url = $("#jupyter_home_url").val()
				
			}
			
			var confidential = "FALSE"; //default is public

			var confidential_field_value = $('#host_dynamic_form input[name="confidential"]:checked').val()

			if(typeof  confidential_field_value != "undefined"){
				
				confidential = confidential_field_value
				
			}
			
			var req = {
				
				type: "host",
				
				hostname: $("#hostname").val(),
				
				hostip: hostip,
				
				hostport: hostport,
				
				url: jupyter_url,
				
				hosttype: hosttype,
				
				username: $("#username").val(),

				confidential: confidential,

				ownerid: GW.user.current_userid
				
			}
			
			$.ajax({
				
				url: "add",
				
				method: "POST",

				data: req
				
			}).done(function(msg){
				
				msg = $.parseJSON(msg);
				
				GW.host.addMenuItem(msg);

				GW.host.expand(msg);
				
				callback();
				
			}).fail(function(jqXHR, textStatus){
				
				alert("Fail to add the host.");
				
			});
			
		}else{
			
			alert("Invalid input");
			
		}
		
	},
	
	edit: function(){
		
		if(this.preEditcheck()){
			
			var hostid = $("#_host_id").text();
			
			var hostname = $("#_host_name").val()
			
			var hostusername = $("#_host_username").val()
			
			var hostip = ""
				
			if(typeof $("#_host_ip").val() != "undefined"){
				
				hostip = $("#_host_ip").val()
				
			}
			
			var hostport = ""
			
			if(typeof $("#_host_port").val() != "undefined"){
				
				hostport = $("#_host_port").val()
				
			}
			
			var hosttype = $( "#_host_type" ).text()
			
			var jupyter_url = ""
				
			if(typeof $("#_host_url").val() != "undefined"){
				
				jupyter_url = $("#_host_url").val()
				
			}

			var confidential = "FALSE"; //default is public

			if(typeof $('input[name="confidential"]:checked').val() != "undefined"){
				
				confidential = $('input[name="confidential"]:checked').val()
				
			}
			
			var req = {
				
				type: "host",
				
				hostname: hostname,
				
				hostip: hostip,
				
				hostport: hostport,
				
				url: jupyter_url,
				
				hosttype: hosttype,
				
				username: hostusername,
				
				hostid: hostid,

				confidential: confidential
				
			}
			
			$.ajax({
				
				url: "edit",
				
				method: "POST",
				
				data: req
				
			}).done(function(msg){
				
				msg = $.parseJSON(msg);
				
				GW.general.showToasts("Host updated.");
				
				GW.host.refreshHostList();
				
			}).fail(function(jqXHR, textStatus){
				
				alert("Fail to add the host.");
				
			});
			
		}else{
			
			alert("Invalid input");
			
		}
		
	},
	
	editSwitch: function(){
		
		if(GW.host.checkIfHostPanelActive()){

			console.log("Turn on/off the fields");
		
			$(".host-value-field").prop( "disabled", GW.process.editOn );
			
			GW.process.editOn = !GW.process.editOn;
			
			if(!GW.process.editOn){
				
				console.log("Save the changes if any")
				
				this.edit()
				
			}
		}
		
		
	},
	
	openJupyter: function(hostid){
		
		window.open("/Geoweaver/jupyter-proxy/"+hostid+"/", "_blank");
		
	},

	openGoogleEarth: function(hostid){
		
		window.open("/Geoweaver/GoogleEarth-proxy/"+hostid+"/", "_blank");
		
	},
	
	getToolbar: function(hostid, hosttype){
		
		var content = "<i class=\"fa fa-edit subalignicon\" onclick=\"GW.host.editSwitch()\" data-toggle=\"tooltip\" title=\"Edit\"></i>";
		
		if( hosttype=="ssh" || hosttype == null || hosttype == "null" ){
			
			// "<i class=\"fas fa-external-link-alt subalignicon\" onclick=\"GW.host.openssh('"+
			
			//  hostid + "')\" data-toggle=\"tooltip\" title=\"Connect SSH\"></i>"+ //this is a problematic function
				
//              "<i class=\"fa fa-line-chart subalignicon\" onclick=\"GW.host.recent('"+
//              
//              hostid + "')\" data-toggle=\"tooltip\" title=\"History\"></i>"+

			content +=  "<i class=\"fab fa-python subalignicon\" onclick=\"GW.host.readEnvironment('"+
								
				hostid + "')\" data-toggle=\"tooltip\" title=\"Read Python Environment\"></i>"+
				
				"<i class=\"fa fa-upload subalignicon\" onclick=\"GW.fileupload.uploadfile('"+
				
				hostid + "')\" data-toggle=\"tooltip\" title=\"Upload File\"></i>"+
				
				" <i class=\"fa fa-sitemap subalignicon\" onclick=\"GW.filebrowser.start('"+
							
				hostid + "')\" data-toggle=\"tooltip\" title=\"Browser File Hierarchy\"></i>";
			
		}else if(hosttype=="jupyter" || hosttype=="jupyterhub" || hosttype=="jupyterlab" ){
			
			content += "<i class=\"fas fa-chart-line subalignicon\" onclick=\"GW.host.recent('" +
			
				hostid + "')\" data-toggle=\"tooltip\" title=\"History\"></i>" + 
				
				"<i class=\"fas fa-external-link-alt subalignicon\" onclick=\"GW.host.openJupyter('" + 
				
				hostid + "')\" data-toggle=\"tooltip\" title=\"Open Jupyter\"></i>";
			
		}else if(hosttype=="gee"){
			
			content += "<i class=\"fa fa-line-chart subalignicon\" onclick=\"GW.host.recent('"+
			
				hostid + "')\" data-toggle=\"tooltip\" title=\"History\"></i>" + 
				
				"<i class=\"fas fa-external-link-alt subalignicon\" onclick=\"GW.host.openGoogleEarth('" + 
				
				hostid + "')\" data-toggle=\"tooltip\" title=\"Open Google Earth\"></i>";
			
		}
		
		return content;
		
	},



	showEnvironmentTable: function(msg){

		var content = "<h4 class=\"border-bottom\">Environment List  <button type=\"button\" class=\"btn btn-secondary btn-sm\" id=\"closeEnvironmentPanel\" >close</button></h4>"+
		"<div class=\"modal-body\" style=\"font-size: 12px;\">"+
		"<table class=\"table table-striped\" id=\"environment_table\"> "+
		"  <thead class=\"thead-light\"> "+
		"    <tr> "+
		"      <th scope=\"col\">Name</th> "+
		"      <th scope=\"col\">Bin Path</th> "+
		"      <th scope=\"col\">PyEnv</th> "+
		"      <th scope=\"col\">Base Directory</th> "+
		"      <th scope=\"col\">Settings</th> "+
		"    </tr> "+
		"  </thead> "+
		"  <tbody> ";

		
		for(var i=0;i<msg.length;i++){
			
			content += "    <tr> "+
				"      <td>"+msg[i].name+"</td> "+
				"      <td>"+msg[i].bin+"</td> "+
				"      <td>"+msg[i].pyenv+"</td> "+
				"      <td>"+msg[i].basedir+"</td> "+
				"      <td>"+msg[i].settings+"</td> "+
				"    </tr>";
			
		}
		
		content += "</tbody>"+
		"</table>"+
		"</div>";

		$("#environment-iframe").html(content);

		$("#closeEnvironmentPanel").click(function(){

			$("#environment-iframe").html("");

		});

	},

	readEnvironmentCallback: function(encrypt, req, dialogItself, button){

		req.pswd = encrypt;

		req.token = GW.general.CLIENT_TOKEN;

		$.ajax({
			
			url: "readEnvironment",
			
			type: "POST",
			
			data: req
			
		}).done(function(msg){
			
			if(msg){
				
				msg = GW.general.parseResponse(msg);
				
				if(msg.status == "failed"){
					
					alert("Fail to read python environment.");
					
					console.error("fail to execute the process " + msg.reason);
					
				}else{

					GW.host.showEnvironmentTable(msg);

				}
				
				
				
			}else{
				
				console.warn("Return Response is Empty");

				GW.host.showEnvironmentTable([]);
				
			}

			if(dialogItself) {
					
				try{dialogItself.closeFrame(); }catch(e){}
				
			}
			
			
		}).fail(function(jxr, status){
			
			alert("Error: unable to log on. Check if your password or the configuration of host is correct.");
			
			if($("#inputpswd").length) $("#inputpswd").val("");
			
			console.error("fail to execute the process " + req.processId);
			
		});
	},

	readEnvironment: function(hid){

		var req = {
			
			hostid: hid,
			
		}

		GW.host.start_auth_single(hid, req, GW.host.readEnvironmentCallback );

	},
	
	display: function(msg){
		
		GW.process.editOn = false;
		
		var content = "<div class=\"modal-body\">";
		
		content += "<div class=\"row\" style=\"font-size: 12px;\">";
		content += "<form class=\"well form-horizontal\" id=\"info_form\">"
		content += "<legend><center><h2><b>Host Details</b></h2></center></legend><br>"
		var hostid = null, hostip = null, hosttype = null, confidential = null, owner = null, envs = null;
		
		jQuery.each(msg, function(i, val) {
			
			if(val!=null&&val!="null"&&val!=""){
				
				if(typeof val =='object')
				{
				  val = JSON.stringify(val);
				}
				
				if(i=="id"){
					
					hostid = val;
					
				}
				
				if(i=="ip"){
					
					hostip = val;
					
				}
				
				if(i=="type"){
					
					hosttype = val;
					
				}

				if(i=="confidential"){

					confidential = val;
					return;

				}else if(i=="owner"){

					owner = val;
					return;

				}else if(i=="envs"){

					envs = val;
					return;

				}



				if(i=="id" || i=="ip" || i=="type" || i=="url"){
				
					if(i=="ip"){
						
						content += "<div style=\"margin-top: 10px;\" class=\"col col-md-3 control-label\">"+"IP Address"+"</div>";  
					
					}else if (i=="id") {

						content += "<div class=\"form-group\"><label class=\"col col-md-3 control-label\">"+i.toUpperCase()+"</label>";
					
					}else if (i=="url") {

						content += "<div style=\"margin-top: 10px;\" class=\"col col-md-3 control-label\">"+"URL"+"</div>"; 

					}else {

						content += "<div class=\"col col-md-3 control-label\">"+i.charAt(0).toUpperCase()+ i.slice(1)+"</div>";

					}
				
				}else {

					content += "<div style=\"margin-top: 10px;\" class=\"col col-md-3 control-label\">"+i.charAt(0).toUpperCase()+ i.slice(1)+"</div>";
				}
				
				
				if(i=="id" || i=="type"){
					
					content += "<div class=\"col col-md-7\" id=\"_host_"+i+"\" >"+val+"</div>";
					
				}else{
					
					if (i=='name'){

						content += "<div class=\"col col-md-7 inputGroupContainer\"><div class=\"input-group\"><span class=\"input-group-addon\"><i class=\"glyphicon glyphicon-pencil\"></i></span>"+
							"<input class=\"host-value-field form-control\" type=\"text\" id=\"_host_"+i+"\" disabled=\"true\" value=\""+
							val+"\" /></div></div>";


					} else if (i=="ip") {
						
						content += "<div class=\"col col-md-7 inputGroupContainer\"><div class=\"input-group\"><span class=\"input-group-addon\"><i class=\"glyphicon glyphicon-globe\"></i></span>"+
							"<input class=\"host-value-field form-control\" type=\"text\" id=\"_host_"+i+"\" disabled=\"true\" value=\""+
							val+"\" /></div></div>";
							
					} else if (i=="port") {

						content += "<div class=\"col col-md-7 inputGroupContainer\"><div class=\"input-group\"><span class=\"input-group-addon\"><i class=\"glyphicon glyphicon-transfer\"></i></span>"+
						"<input class=\"host-value-field form-control\" type=\"text\" id=\"_host_"+i+"\" disabled=\"true\" value=\""+
						val+"\" /></div></div>";

					}else if (i=="username") {

						content += "<div class=\"col col-md-7 inputGroupContainer\"><div class=\"input-group\"><span class=\"input-group-addon\"><i class=\"glyphicon glyphicon-user\"></i></span>"+
						"<input class=\"host-value-field form-control\" type=\"text\" id=\"_host_"+i+"\" disabled=\"true\" value=\""+
						val+"\" /></div></div>";

					}else {

						content += "<div class=\"col col-md-7 inputGroupContainer\"><div class=\"input-group\"><span class=\"input-group-addon\"><i class=\"glyphicon glyphicon-link\"></i></span>"+
						"<input class=\"host-value-field form-control\" type=\"text\" id=\"_host_"+i+"\" disabled=\"true\" value=\""+
						val+"\" /></div></div>";

					}
					
					
				}

					
			}

		});

		content += "<div style=\"margin-top: 5px; \" class=\"col col-md-3 control-label\">Confidential</div>"+
						"<div class=\"col col-md-7\">";
				
		if(confidential=="FALSE"){

			content  += '       <input type="radio" name="confidential" value="FALSE" checked> '+
			'       <label for="confidential">Public</label>';
			
			if(GW.user.current_userid==owner && GW.user.current_userid != "111111")
				content += '       <input type="radio" name="confidential" value="TRUE"> '+
				'       <label for="confidential">Private</label>';

		}else{

			content  += '       <input type="radio" name="confidential" value="FALSE"> '+
			'       <label for="confidential">Public</label>';
			
			if(GW.user.current_userid==owner&& GW.user.current_userid != "111111")
				content += '       <input type="radio" name="confidential" value="TRUE" checked> '+
				'       <label for="confidential">Private</label>';

		}
		
		content += "</form>"

		var delbtn = "";
		
//          if(hostip!="127.0.0.1")
		if(msg.name!="localhost")
			delbtn = "<i class=\"fa fa-minus subalignicon\" style=\"color:red;\" data-toggle=\"tooltip\" title=\"Delete this host\" onclick=\"GW.menu.del('" +hostid+"','host')\"></i>";
		
		content += "</div>"+
			
			"<div class=\"col-md-12\">"+
			
			"<p align=\"right\">"+
			
				this.getToolbar(hostid, hosttype) +
				
				delbtn +
			
			"</p>"+
			
			"</div>"+

			"<div class=\"col-md-12\" style=\"max-height:600px;margin:0;\" id=\"environment-iframe\">"+
			
			"</div>"+
			
			"<div class=\"col-md-12\" style=\"max-height:600px;margin:0;\" id=\"ssh-terminal-iframe\">"+
			
			"</div>"+
			
			"<div class=\"col-md-12\" style=\"margin:0;\" id=\"host-file-uploader\">"+
			
			"</div>"+
			
			"<div class=\"col-md-12\" style=\"max-height:800px;margin:0;\" id=\"host-file-browser\">"+
			
			"</div>"+
			
			"<div class=\"col-md-12\" style=\"max-height:800px;margin:0;\" id=\"host-history-browser\">"+
			
			"</div>"+
			
			"</div>";
		
		
		$("#main-host-content").html(content);

		GW.ssh.current_process_log_length = 0;
		
		GW.general.switchTab("host");
		
		
	},
	
	viewJupyter: function(history_id){
		
		$.ajax({
			
			url: "log",
			
			method: "POST",
			
			data: "type=host&id=" + history_id
			
		}).done(function(msg){
			
			if(msg==""){
				
				alert("Cannot find the host history in the database.");
				
				return;
				
			}
			
			msg = $.parseJSON(msg);
			
			var code = msg.output;
			
			if(code!=null && typeof code != 'undefined'){
				
				if(typeof code != 'object'){
				
					code = $.parseJSON(code);
				
				}
				
				var notebook = nb.parse(code.content);
				
				var rendered = notebook.render();
				
				var content = '<div class="modal-body">'+$(rendered).html()+'</div>';
				
				content += '<div class="modal-footer">' +
				"   <button type=\"button\" id=\"host-history-download-btn\" class=\"btn btn-outline-primary\">Download</button> "+
				"   <button type=\"button\" id=\"host-history-cancel-btn\" class=\"btn btn-outline-secondary\">Cancel</button>"+
				'</div>';
				
//                  console.log(content);
				
				GW.host.his_frame = GW.process.createJSFrameDialog(800, 600, content, "History Jupyter Notebook " + history_id);
				
				$("#host-history-download-btn").click(function(){
					
					GW.host.downloadJupyter(history_id);
					
				})
				
				$("#host-history-cancel-btn").click(function(){
					
					GW.host.his_frame.closeFrame();
					
				});
				
			}
			
		}).fail(function(e){
			
			console.error(e);
			
		});
		
	},
	
	
	deleteSelectedJupyter: function(){
		
		if(confirm("Are you sure to remove all the selected history? This is permanent.")){
			
			$(".hist-checkbox:checked").each(function() {
				
				var histid = $(this).attr('id');
				
				console.log("Removing "+histid);
				
				GW.host.deleteJupyterDirectly(histid.substring(9));
				
			});
			
		}
		
	},
	
	deleteJupyterDirectly: function(history_id){
		
		$.ajax({
			
			url: "del",
			
			method: "POST",
			
			data: "type=history&id=" + history_id
			
		}).done(function(msg){
			
			if(msg==""){
				
				alert("Cannot find the host history in the database.");
				
				return;
				
			}else if(msg=="done"){
				
				console.log("The history " + history_id + " is removed")
				
				$("#host_history_row_" + history_id).remove()
				
			}else{
				
				alert("Fail to delete the jupyter notebook")
				
				console.error("Fail to delete jupyter: " + msg);
				
			}
			
		})
		
	},
	
	deleteJupyter: function(history_id){
		
		if(confirm("Are you sure to remove this history? This is permanent.")){
			
			this.deleteJupyterDirectly(history_id);
			
		}
		
	},
	
	downloadJupyter: function(history_id){

		$.ajax({
			
			url: "log",
			
			method: "POST",
			
			data: "type=host&id=" + history_id
			
		}).done(function(msg){
			
			if(msg==""){
				
				alert("Cannot find the host history in the database.");
				
				return;
				
			}

			msg = $.parseJSON(msg);
			
			var code = msg.output;
			
			if(code!=null && typeof code != 'undefined'){
				
				if(typeof code != 'object'){
				
					code = $.parseJSON(code);
				
				}
				
//              function download(content, fileName, contentType) {
				
				var a = document.createElement("a");
				
				var file = new Blob([JSON.stringify(code.content)], {type: "application/json"});
				
				a.href = URL.createObjectURL(file);
				
				a.target = "_blank"
				
				a.download = "jupyter-"+history_id + ".ipynb";
				
				a.click();
				
//              }
				
			}
			
//              download(jsonData, 'json.txt', 'text/plain');
			
			
			
		})
		
	},
	
	historyTableCellUpdateCallBack: function(updatedCell, updatedRow, oldValue){
		
		console.log("The new value for the cell is: " + updatedCell.data());
		console.log("The old value for that cell was: " + oldValue);
		console.log("The values for each cell in that row are: " + updatedRow.data());

		// The values for each cell in that row are: <input type="checkbox" class="hist-checkbox" id="selected_3naxi3l8o52j">,http://localhost:8888/api/contents/work/GMU%20workspace/COVID/covid_win_laptop.ipynb,xyz,2021-03-03 22:00:32.913,<a href="javascript: GW.host.viewJupyter('3naxi3l8o52j')">View</a> <a href="javascript: GW.host.downloadJupyter('3naxi3l8o52j')">Download</a> <a href="javascript: GW.host.deleteJupyter('3naxi3l8o52j')">Delete</a>

		var thecheckbox = updatedRow.data()[0]

		var hisid = $(thecheckbox).attr("id").substring(9)

		console.log("history id: " + hisid)

		var newvalue = updatedRow.data()[2]

		GW.history.updateNotesOfAHistory(hisid, newvalue);
		
	},

	
	recent: function(hid){
		
		console.log("Show the history of all previously executed scripts/jupyter notebok");
		
		$.ajax({
			
			url: "recent",
			
			method: "POST",
			
			data: "type=host&hostid=" + hid + "&number=100"
			
		}).done(function(msg){
			
			if(!msg.length){
				
				alert("no history found");
				
				return;
				
			}
			
			msg = $.parseJSON(msg);
			
			var content = "<h4 class=\"border-bottom\">Recent History  <button type=\"button\" class=\"btn btn-secondary btn-sm\" id=\"closeHostHistoryBtn\" >close</button></h4>"+
			"<div class=\"modal-body\" style=\"font-size: 12px;\">"+
			
			"<div class=\"row\"><button type=\"button\" class=\"btn btn-danger btn-sm\" id=\"deleteHostHistoryBtn\" >Delete Selected</button> "+
			"<button type=\"button\" class=\"btn btn-danger btn-sm\" id=\"deleteHostHistoryNoNoteBtn\" >Delete No-Notes</button> "+
			"<button type=\"button\" class=\"btn btn-danger btn-sm\" id=\"deleteHostHistoryAllBtn\" >Delete All</button> "+
			"<button type=\"button\" class=\"btn btn-primary btn-sm\" id=\"compareHistoryBtn\" >Compare</button> "+
			"<button type=\"button\" class=\"btn btn-primary btn-sm\" id=\"refreshHostHistoryBtn\" >Refresh</button> </div>"+
			
			"<table class=\"table host_history_table table-color\"> "+
			"  <thead> "+
			"    <tr> "+
			"      <th scope=\"col\"><input type=\"checkbox\" id=\"all-selected\" ></th> "+
			"      <th scope=\"col\">Process</th> "+
			"      <th scope=\"col\" style=\"width:200px;\">Notes (Click to Edit)</th> "+
			"      <th scope=\"col\">Begin Time</th> "+
			"      <th scope=\"col\">End Time</th> "+
//              "      <th scope=\"col\">Status</th> "+
			"      <th scope=\"col\">Action</th> "+
			"    </tr> "+
			"  </thead> "+
			"  <tbody> ";
			
			for(var i=0;i<msg.length;i++){
				
//                  var status_col = GW.process.getStatusCol(msg[i].id, msg[i].status);
				
				var detailbtn = "      <td ><a href=\"javascript: GW.host.viewJupyter('"+
					msg[i].id+"')\">View</a> <a href=\"javascript: GW.host.downloadJupyter('"+
					msg[i].id+"')\">Download</a> <a href=\"javascript: GW.host.deleteJupyter('"+
					msg[i].id+"')\">Delete</a></td> ";
				
				content += "    <tr id=\"host_history_row_"+msg[i].id+"\"> "+
					"      <td><input type=\"checkbox\" class=\"hist-checkbox\" id=\"selected_"+msg[i].id+"\" /></td>"+
					"      <td>"+msg[i].name+"</td> "+
					"      <td>"+msg[i].notes+"</td> "+
					"      <td>"+msg[i].begin_time+"</td> "+
					"      <td>"+msg[i].end_time+"</td> "+
//                      status_col +
					detailbtn + 
					"    </tr>";
				
			}
			
			content += "</tbody></div>";
			
			$("#host-history-browser").html(content);
			
			// initialize the tab with editable cells
			
			var table = $('.host_history_table').DataTable();

			table.MakeCellsEditable({
				"onUpdate": GW.host.historyTableCellUpdateCallBack,
				"columns": [3],
				"allowNulls": {
					"columns": [3],
					"errorClass": 'error'
				},
				"confirmationButton": { // could also be true
					"confirmCss": 'my-confirm-class',
					"cancelCss": 'my-cancel-class'
				},
				"inputTypes": [
					{
						"column": 3,
						"type": "text",
						"options": null
					}]
			});
			
//              $("#all-selected").on("click", function(){});
			
			$('#all-selected').change(function(){
				if ($(this).is(':checked')) {
					//check all the rows
					$(".hist-checkbox").prop('checked', true);
					
				}else {
					$(".hist-checkbox").prop('checked', false);
					
				}
			});
			
			$("#closeHostHistoryBtn").on("click", function(){
				
				$("#host-history-browser").html("");
				
			});
			
			$("#deleteHostHistoryBtn").on("click", function(){
				
				GW.host.deleteSelectedJupyter();
				
			});
			
			$("#deleteHostHistoryNoNoteBtn").on("click", function(){
				
				GW.history.deleteNoNotesJupyter(hid, GW.host.recent);
				
			})

			$("#deleteHostHistoryAllBtn").on("click", function(){

				GW.history.deleteAllJupyter(hid, GW.host.recent);

			})
			
			$("#compareHistoryBtn").on("click", function(){
				
				GW.comparison.show();
				
			});
			
			$("#refreshHostHistoryBtn").on("click", function(){
				
				GW.host.recent(hid);
				
			});
			
//              var frame = GW.process.createJSFrameDialog(720, 480, content, 'History of ' + msg.name)
			
		}).fail(function(jxr, status){
			
			console.error(status);
			
		});
		
		
	},
	
	getNewDialogContentByHostType: function(host_type){
		
		var content = ""
		
		if(host_type=="jupyter") {
			
			content = '     <div class="form-group row required">'+
				   '     <label for="hostname" class="col-sm-3 col-form-label control-label">Host Name </label>'+
				   '     <div class="col-sm-9">'+
				   '       <input type="text" class="form-control" id="hostname" value="New Host">'+
				   '     </div>'+
				   '    </div>'+
				   '    <div class="form-group row required">'+
				   '     <label for="hostname" class="col-sm-3 col-form-label control-label">Jupyter URL </label>'+
				   '     <div class="col-sm-9">'+
				   '       <input type="text" class="form-control" id="jupyter_home_url" placeholder="http://localhost:8888/">'+
				   '     </div>'+
				   '    </div>';
			
		}else if(host_type=="jupyterhub"){
			
			content = '     <div class="form-group row required">'+
			   '     <label for="hostname" class="col-sm-3 col-form-label control-label">Host Name </label>'+
			   '     <div class="col-sm-9">'+
			   '       <input type="text" class="form-control" id="hostname" value="New Host">'+
			   '     </div>'+
			   '    </div>'+
			   '    <div class="form-group row required">'+
			   '     <label for="hostname" class="col-sm-3 col-form-label control-label">JupyterHub URL </label>'+
			   '     <div class="col-sm-9">'+
			   '       <input type="text" class="form-control" id="jupyter_home_url" placeholder="http://localhost:8000/">'+
			   '     </div>'+
			   '    </div>';
			
		}else if(host_type=="jupyterlab"){
			
			content = '     <div class="form-group row required">'+
			   '     <label for="hostname" class="col-sm-3 col-form-label control-label">Host Name </label>'+
			   '     <div class="col-sm-9">'+
			   '       <input type="text" class="form-control" id="hostname" value="New Host">'+
			   '     </div>'+
			   '    </div>'+
			   '    <div class="form-group row required">'+
			   '     <label for="hostname" class="col-sm-3 col-form-label control-label">JupyterLab URL </label>'+
			   '     <div class="col-sm-9">'+
			   '       <input type="text" class="form-control" id="jupyter_home_url" placeholder="http://localhost:8888/">'+
			   '     </div>'+
			   '    </div>';
			
		}else if(host_type == "ssh") {
			
			content = '     <div class="form-group row required">'+
				   '     <label for="hostname" class="col-sm-3 col-form-label control-label">Host Name </label>'+
				   '     <div class="col-sm-9">'+
				   '       <input type="text" class="form-control" id="hostname" value="New Host">'+
				   '     </div>'+
				   '    </div>'+
				   '    <div class="form-group row required">'+
				   '     <label for="hostip" class="col-sm-3 col-form-label control-label">Hose IP</label>'+
				   '     <div class="col-sm-9">'+
				   '       <input type="text" class="form-control" id="hostip" placeholder="Host IP">'+
				   '     </div>'+
				   '    </div>'+
				   '    <div class="form-group row required">'+
				   '     <label for="hostport" class="col-sm-3 col-form-label control-label">Port</label>'+
				   '     <div class="col-sm-9">'+
				   '       <input type="text" class="form-control" id="hostport" placeholder="">'+
				   '     </div>'+
				   '    </div>'+
				   '    <div class="form-group row required">'+
				   '     <label for="username" class="col-sm-3 col-form-label control-label">User Name</label>'+
				   '     <div class="col-sm-9">'+
				   '       <input type="text" class="form-control" id="username" placeholder="">'+
				   '     </div>'+
				   '    </div>';
			
		}else if(host_type == "gee") {
			
			content = '     <div class="form-group row required">'+
				   '     <label for="hostname" class="col-sm-3 col-form-label control-label">Host Name </label>'+
				   '     <div class="col-sm-9">'+
				   '       <input type="text" class="form-control" id="hostname" value="New Host">'+
				   '     </div>'+
				   '    </div>'+
				   '    <div class="form-group row required">'+
				   '     <label for="hostname" class="col-sm-3 col-form-label control-label">Client ID </label>'+
				   '     <div class="col-sm-9">'+
				   '       <input type="text" class="form-control" id="client_id" placeholder="ee.Authenticate client ID..">'+
				   '     </div>'+
				   '    </div>';
			
		}

		content += '    <div class="form-group row required">'+
		'     <label for="hostname" class="col-sm-3 col-form-label control-label">Confidential </label>'+
		'     <div class="col-sm-9" style="padding-left: 30px;">'+
		'       <input type="radio" name="confidential" value="FALSE" checked> '+
		'       <label for="confidential">Public</label>';
		
		if(GW.user.current_userid!=null && GW.user.current_userid!="111111")
			content += '       <input type="radio" name="confidential" value="TRUE"> '+
			'       <label for="confidential">Private</label>';
		
		content += '     </div>'+
		'       </div>';
		
		return content;
		
	},
	
	newDialog: function(category){
		
		if(GW.host.new_host_frame!=null){
			
			try{
				
				GW.host.new_host_frame.closeFrame();
				
			}catch(e){
				
				console.error("Fail to close old frame. Maybe it is already closed.");
				
			}
			
			GW.host.new_host_frame = null;
			
		}
		
		var content = '<div class="modal-body" id="newhostdialog" style=\"font-size: 12px;\">'+
		   '<form>'+
		   '   <div class="form-group row required">'+
		   '     <label for="hosttype" class="col-sm-3 col-form-label control-label">Host Type </label>'+
		   '     <div class="col-sm-9">'+
//             '       <input type="text" class="form-control" id="hosttype" value="Host Type">'+
		   '        <select class="form-control" id="hosttype"> '+
		   '            <option value="ssh">SSH Linux/Macintosh/Windows</option> '+
		//    '            <option value="jupyter">Jupyter Notebook</option> '+
		//    '            <option value="jupyterhub">JupyterHub</option> '+
		//    '            <option value="jupyterlab">Jupyter Lab</option> '+
		//    '            <option value="gee">Google Earth Engine</option>'+
		   '        </select> '+
		   '     </div>'+
		   '   </div>'+
		   '   <div id="host_dynamic_form">'+
		   this.getNewDialogContentByHostType("ssh")+
		   '   </div>'+
		   ' </form>'+
		   '</div>';
		
		content += '<div class="modal-footer">' +
			"   <button type=\"button\" id=\"host-add-btn\" class=\"btn btn-outline-primary\">Add</button> "+
			"   <button type=\"button\" id=\"host-cancel-btn\" class=\"btn btn-outline-secondary\">Cancel</button>"+
			'</div>';
		
		GW.host.new_host_frame = GW.process.createJSFrameDialog(500, 450, content, "Add new host")
		
		$("#hosttype").change(function(){
			
			var op = $( "#hosttype option:selected" ).val()
			
			$("#host_dynamic_form").html(GW.host.getNewDialogContentByHostType(op));
			
		})
				
		$("#host-add-btn").click(function(){
			
			GW.host.add(function(){
			
				try{GW.host.new_host_frame.closeFrame();}catch(e){}
				
			});
			
		});
		
		$("#host-cancel-btn").click(function(){
			
			GW.host.new_host_frame.closeFrame();
			
		});
		
		if(category)
			$("#hosttype").val(category).trigger('change');
	},
	
}
