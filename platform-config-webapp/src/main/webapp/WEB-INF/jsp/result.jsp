<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8" />
<title>Mico Platform - configurations</title>
    <link rel="stylesheet" href="webjars/bootstrap/3.1.1/css/bootstrap.css"/>

    <!-- fix Bootstrap CSS for AngularJS -->
    <style type="text/css">
        .nav, .pagination, .carousel, .panel-title a { cursor: pointer; }

        body {
            margin-left: 10%;
            margin-right: 10%;
            font-family: 'Open Sans', Helvetica, Arial, sans-serif;
        }

        .page-header {
            padding-right: 300px;
            background-image: url('../broker/img/mico_logo.png');
            background-repeat: no-repeat;
            background-position: right top;
            border-bottom: 2px solid #ACCD6C;
        }

        .page-header h1 {
            font-size: 3.2em;
            color: #3a6548;
        }

        .page-header h1 small {
            color: #ACCD6C
        }

        .colorpick, .colorpick:hover {
            border: 1px solid #000000;
            color: #000000;
        }

        .infobox {
            margin-top: 10px;
            margin-bottom: 10px;
        }
    </style>
    <style type="text/css">

.container {
    min-width: 500px;
    max-width: 50%;
	max-height: 500px;
	margin: 15px auto;
	border: 1px solid #fff;
	background-color: #ffffff;
	box-shadow: 0px 2px 7px #292929;
	-moz-box-shadow: 0px 2px 7px #292929;
	-webkit-box-shadow: 0px 2px 7px #292929;
	border-radius: 10px;
	-moz-border-radius: 10px;
	-webkit-border-radius: 10px;
}

.mainbody, .header {
	padding: 5px;
}

.mainbody {
	margin-top: 0px;
	min-height: 50px;
	max-height: 388px;
	overflow: auto;
}

.header {
	height: 25px;
	border-bottom: 1px solid #EEE;
	font-size: 1.3em;
	text-align: center;
	border-top-left-radius: 5px;
	border-top-right-radius: 5px;
}

.footer, .header:last-child {
	height: 30px;
	background-color: whiteSmoke;
	border-top: 1px solid #DDD;
	border-bottom-left-radius: 5px;
	border-bottom-right-radius: 5px;
}

select, input {
	min-width: 180px;
	margin: 10px;
}

</style>
</head>
<body>

<div class="container-fluid page-header">
    <h1>MICO <small>Platform Configuration</small></h1>
</div>

<div class="container-fluid">
    <ul class="nav nav-tabs">
        <li role="presentation"><a href="/broker/">Broker</a></li>
        <li role="presentation" class="active"><a href="/mico-configuration/">Platform Configuration</a></li>
        <li role="presentation"><a href="/marmotta/">Marmotta</a></li>
    </ul>
</div>

<div class="container-fluid" style="margin-top: 10px; margin-bottom: 10px;">
	<main class="inline-block-center">

	<c:if test="${error != null}">
		<div class="container">
			<div class="header">Error</div>
			<div class="mainbody">
				<pre>${error}</pre>
			</div>
		</div>
	</c:if> <c:if test="${status != null}">
		<div class="container">
			<div class="header">Status</div>
			<div class="mainbody">
				<pre>${status}</pre>
			</div>
		</div>
	</c:if> <c:if test="${message != null}">
		<div class="container">
			<div class="header">command output:</div>
			<div class="mainbody">
				<pre>${message}</pre>
			</div>
		</div>
	</c:if>


	<div class="container">
		<div class="header">Available Configurations</div>
		<div class="mainbody" style="text-align: center;">
			<form:form method="POST" action="run.html">
				<select id="command" name=command>
					<c:forEach var="name" items="${commands}">
						<option>${name}</option>
					</c:forEach>
				</select>
				<input type="submit" value="start selected configuration" />
			</form:form>
			<a href="stopAll.html"> <button>Stop all extractors</button></a>
		</div>
	</div>
	</main>
</div>
</body>
</html>