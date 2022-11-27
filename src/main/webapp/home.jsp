<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<!DOCTYPE html>
<html>
<head>
	<meta charset="ISO-8859-1">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<link rel="stylesheet" href="style.css" type="text/css" />
	
	<title>Home page</title>

</head>
<body>
	<nav class="navbar">
	  	<div id="title">
			<p>E-Mail Client</p>
	  	</div>
	  	<div id="right">
			<p>
				<%
					String email = (String) request.getAttribute("email");
				%>
				${fn:escapeXml(email)}
			</p>
			<form class="navbar-controls" action="LogoutServlet" method="post">
				<input type="submit" name="logout" value="Logout">
			</form>
	  	</div>
	</nav>
	
	<div class="grid-container">
		<form class="btn-group" action="NavigationServlet" method="post">
			<input type="hidden" name="email" value=<c:out value="${email}" escapeXml=" true"/>>
			<input type="text" name="search"  placeholder="Search..." id="item">
			<input type="submit" name="search"  value="Search">
			<input type="submit" name="newMail" value="New Mail">
			<input type="submit" name="inbox" value="Inbox">
			<input type="submit" name="sent" value="Sent">
		</form>
	
		<%= request.getAttribute("content")!=null ? request.getAttribute("content") : "" %>
	</div>
</body>
</html>