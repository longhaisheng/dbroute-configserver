<%@ page language="java" contentType="text/html; charset=utf-8"	pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page session="false"%>
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<meta charset="utf-8">
<title>My HTML View</title>
<link href="<c:url value="/resources/form.css" />" rel="stylesheet"
	type="text/css" />
</head>
<body>
		<c:forEach var="app_name"  items="${all_app_names}">
			<h3><a href="zklist/app/${app_name}" target="_blank">${app_name}</a></h3>
		</c:forEach>
</body>
</html>