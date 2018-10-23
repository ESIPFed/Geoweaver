<!DOCTYPE HTML>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<!-- 
The MIT License (MIT)

Copyright (c) 2013 The Authors

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 -->
  <head>
    <meta charset="utf-8" />
    <meta http-equiv="cache-control" content="max-age=0" />
	<meta http-equiv="cache-control" content="no-cache" />
	<meta http-equiv="expires" content="0" />
	<meta http-equiv="expires" content="Tue, 01 Jan 1980 1:00:00 GMT" />
	<meta http-equiv="pragma" content="no-cache" />
    <meta name="${_csrf.headerName}" content="${_csrf.token}"/>
    <title>SSHW Terminal Emulator</title>
<!--     <script src="js/aes.js"></script> -->
    <script src="static/js/sockjs-0.3.4.js"></script>
    <script src="static/js/jquery-1.7.2.js"></script>
    <!-- <script src="static/js/jquery.mousewheel-min.js"></script> -->
    <script src="static/js/jquery.terminal-0.7.7.js"></script>
    <link href="static/css/jquery.terminal-0.7.7.css" rel="stylesheet"/>
    <script>
    var websocket;
    var shell;
    var last_prompt = null;
	var host = window.location.hostname;
	var port = window.location.port;
	var pcol = window.location.protocol;
	var root = getContextURLPath();
	var key = '${key}';
	var username = '<sec:authentication property="principal" />';
	//alert('ID:' + key + ',USERNAME:' + username);
    var special = {
      black:   "\x1b[1;30m",
      red:     "\x1b[1;31m",
      green:   "\x1b[1;32m",
      yellow:  "\x1b[1;33m",
      blue:    "\x1b[1;34m",
      magenta: "\x1b[1;35m",
      cyan:    "\x1b[1;36m",
      white:   "\x1b[1;37m",
      reset:   "\x1b[0m",
      ready:   "]0;", 
      prompt:  "$"
    };

    var greetings_message = "Welcome to SSHW";

    jQuery(document).ready(init);

    function send(data) {
      shell.pause();
      if(websocket != null){
    	  websocket.send(data);
      } else {
        shell.error('not connected!');
      }
    }
    
    // *** DEPRECATED IN FAVOUR OF SPRING SECURITY
	function ssh_login(user, passwd, callback) {
      // var encrypted = CryptoJS.AES.encrypt("A Sample Message", "SecretPassphrase", { mode: CryptoJS.mode.CBC, padding: CryptoJS.pad.NoPadding, iv: iv });
		var result = $.get( root + '/service/login?user=' + user + '&passwd=' + passwd, function( data ) {
	           if (data == true) {
	               callback(user);
 			   }
	           else {
                   $.get( root + '/service/logout', function() {})
                   callback(false);
               }
		});
	}

	//onBeforeLogin: self.purge,
	//login: ssh_login,
	//login: false,

    function init() {
      shell = $('body').terminal(function(command, term) {
        send(command);
      }, {
        greetings: greetings_message,
        clear: false,
        exit: false,
        onBlur: function() {
            // prevent loosing focus
            return false;
        }
      });

      start_ws();
    }

    function start_ws() {
      shell.pause();
	  host = window.location.hostname;
	  port = window.location.port;
	  
	  if (window.location.protocol == 'http:') {
	      protocol = 'ws';
	  } else {
	      protocol = 'wss';
	  }
      url = protocol + '://' + host + ':' + port + '/sshw/ssh';
      
      //if(!("WebSocket" in window)) {
        websocket = new SockJS('/sshw/ssh', undefined, {protocols_whitelist: []});
      //} else {
      //  websocket = new WebSocket(url);
      //}
      
      websocket.onopen = function(e) { ws_onopen(e) };
      websocket.onclose = function(e) { ws_onclose(e) };
      websocket.onmessage = function(e) { ws_onmessage(e) };
      websocket.onerror = function(e) { ws_onerror(e) };
      shell.resume();
    }

    function ws_onopen(e) {
      shell.echo(special.white + "connected" + special.reset);
      // link the SSH session established with spring security logon to the websocket session...
      send(username);
    }

    function ws_onclose(e) {
    	//shell.logout();
        shell.echo(special.white + "disconnected" + special.reset);
        shell.destroy();
        shell.purge();
        //document.location.href = 'logout';
        document.forms['logout'].submit();
      }

    function ws_onerror(e) {
        shell.echo(special.red + e + special.reset);
    }

    function ws_onmessage(e) {
      try {
        if(e.data.indexOf(special.prompt) == -1 && e.data.indexOf(special.ready) == -1) {
            shell.echo(e.data);        	
        }
        if (e.data.indexOf(special.ready) != -1) {
            shell.resume();
		}
      } catch(err) {
        shell.error("** Invalid server response : " + e.data); 
        if(last_prompt) {
          shell.set_prompt(last_prompt);
        }
      }
    }

	function getContextURLPath() {
        var rootUrl = location.protocol;
        rootUrl = rootUrl+"//"+location.host;
        var path = location.pathname;
        var tempStr = path.split('/');
        rootUrl = rootUrl+"/"+tempStr[1];
        return rootUrl;
    }
    
    </script>
  </head>
<body>	
	<form name="logout" action="logout" method="POST" style="display:none;">
		<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"></input>
	</form>
</body>
