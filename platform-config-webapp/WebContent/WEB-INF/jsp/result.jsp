<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8" />
<title>Mico Platform - configurations</title>
<style type="text/css">
body {
	background-image: url('img/bg.png');
	background: grey;
}

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

.mainbody, .header, .footer {
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
	background-color: lightgrey;
	font-size: 1.3em;
	text-align: center;
	-webkit-border-top-left-radius: 5px;
	-webkit-border-top-right-radius: 5px;
	-moz-border-radius-topleft: 5px;
	-moz-border-radius-topright: 5px;
	border-top-left-radius: 5px;
	border-top-right-radius: 5px;
}

.footer, .header:last-child {
	height: 30px;
	background-color: whiteSmoke;
	border-top: 1px solid #DDD;
	-webkit-border-bottom-left-radius: 5px;
	-webkit-border-bottom-right-radius: 5px;
	-moz-border-radius-bottomleft: 5px;
	-moz-border-radius-bottomright: 5px;
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

	<main class="inline-block-center">
	<div class="container">
		<div class="header">Mico Platform Configurations</div>
	</div>

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
		</div>
	</div>
	</main>
</body>
</html>