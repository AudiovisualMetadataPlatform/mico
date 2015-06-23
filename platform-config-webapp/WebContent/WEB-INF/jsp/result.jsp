<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8" />
<title>Mico Platform - configurations results</title>
<style type="text/css">
body {
	
}
</style>
</head>
<body>

	<c:if test="${error != null}">
	<div>
		<h3>ERROR</h3>
		<pre>${error}</pre>

	</div>
	</c:if>

	<c:if test="${status != null}">
	<div>
		<h3>status</h3>
		<pre>${status}</pre>
	</div>
	</c:if>

	<c:if test="${message != null}">
	<h2>command output:</h2>
	<div>
		<pre style="text-align:left;">${message}</pre>
	</div>
	</c:if>

	
	<div>
	<h2>select configuration:</h2>
	
		<form:form method="POST" action="run.html">
			<select id="command" name=command>
				<c:forEach var="name" items="${commands}">
					<option>${name}</option>
				</c:forEach>
			</select>
			<br>
			<input type="submit" value="start selected configuration" />
		</form:form>
	</div>
	
</body>
</html>