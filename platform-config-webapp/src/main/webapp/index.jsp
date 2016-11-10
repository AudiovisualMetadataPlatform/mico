<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%--
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<!DOCTYPE html>
<html lang="en">
<head>
<title>Mico Platform - configurations</title>
<style type="text/css">
body {
	background-image: url('<c:url value="img/bg.png" />');;
}
</style>
</head>
<body>
	<br>
	<div style="text-align:center">
		<h2>
			This is the platform configuration web site<br> <br>
		</h2>
<jsp:forward page="configs.html" />
		<h3>
			<a href="run.html">Click here to See available configurations... </a>
		</h3>
	</div>
</body>
</html>