<%@ page language="java" contentType="text/html; charset=utf-8"	pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
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
	<c:forEach var="app" items="${app_list}">
		<c:if test="${success}">
			<font color=red>${success}</font>
		</c:if>
		<h3>appName:${app.appName}</h3>
		<c:forEach var="config" items="${app.dbConfigs}">
			<form:form id="form" method="post" modelAttribute="model"
				cssClass="cleanform" acceptCharset="utf-8">
				<h5>
					php db config name:<input id="db_config_name" name="db_config_name"
						value="${config.dbConfigName}" style="width: 350px"
						readonly="readonly"> <input name="app_name" id="app_name"
						value="${app.appName}" type="hidden" readonly="readonly">
				</h5>
				<br>
				<h5>
					baseConfigs:<br>
					<textarea rows="40" cols="100" name="base_config" id="base_config">${config.baseConfig}</textarea>
				</h5>
				<br>
				<div>
					master config:
					<c:forEach var="master" items="${config.masterDbs}" varStatus="i">
						<h5>
							<input id="master_${i.index+1}" name="master_${i.index+1}"
								value="${master}" style="width: 1000px">
						</h5>
						<br>
					</c:forEach>

				</div>
				<div>
					slave config:
					<c:forEach var="slave" items="${config.slaveDbs}" varStatus="j">
						<h5>
							<input id="slave_${j.index+1}" name="slave_${j.index+1}"
								value="${slave}" style="width: 1000px">
						</h5>
						<br>
					</c:forEach>
				</div>
				<input type="submit" value="submit">
			</form:form>
		</c:forEach>
	</c:forEach>
</body>
</html>