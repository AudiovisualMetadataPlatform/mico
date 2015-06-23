<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<!DOCTYPE html>
<html lang="en">
<head>
<title>Mico Platform - configurations</title>
<style type="text/css">
body {
	
}
</style>
</head>
<body>${message}

	<br>
	<c:if test="${status != null}">
		<h3>status</h3>
		<pre>${status}</pre>

	</c:if>
	<div
		style="font-family: verdana; padding: 10px; border-radius: 10px; font-size: 12px; text-align: center;">

	</div>
	
	<div>
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