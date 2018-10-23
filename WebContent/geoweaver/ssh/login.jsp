<!DOCTYPE HTML>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
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
    <meta charset="utf-8"></meta>
    <meta http-equiv="cache-control" content="max-age=0" />
	<meta http-equiv="cache-control" content="no-cache" />
	<meta http-equiv="expires" content="0" />
	<meta http-equiv="expires" content="Tue, 01 Jan 1980 1:00:00 GMT" />
	<meta http-equiv="pragma" content="no-cache" />
    <meta name="${_csrf.headerName}" content="${_csrf.token}"/>
    <title>SSHW Terminal Emulator</title>
    <link href="static/css/jquery.terminal-0.7.7.css" rel="stylesheet"></link>
    <link href="static/css/login.css" rel="stylesheet"></link>
</head>

<body class="terminal" onload='document.loginForm.username.focus();'>
  <div class="terminal-output">
  
	<c:if test="${not empty error}">
		<div class="error">${error}</div>
	</c:if>
	<c:if test="${not empty message}">
		<div class="message">${message}</div>
	</c:if>

	<form name='loginForm' action="login" method='POST'>
	  Username:<input class="textbox" type='text' name='username' value=''></input><br/>
	  Password:<input class="textbox" type='password' name='password'></input><br/>
		<input class="button" name="submit" type="submit" value="login"></input>
		<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"></input>
	</form>
  </div>
</body>
</html>