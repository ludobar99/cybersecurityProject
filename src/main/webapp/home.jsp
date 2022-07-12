<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>



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
	  <div class="box">
	  	<div>
			<img src="images/email_icon.jpg" align="left" />
			<p>e-mail client
				<br><% out.println(request.getAttribute("email")); %>
				
				
			</p>
	  	</div>
	  	<div id="right">
	  	<form class="btn-group" action="LogoutServlet" method="post">
			<input type="submit" name="logout" value="Logout">
		</form>
	  	</div>
	  </div>
	</nav>
	
	<div class="grid-container">
		
		<form class="btn-group" action="NavigationServlet" method="post">
			<input type="hidden" name="email" value="<%= request.getAttribute("email") %>">
			<input type="text" name="search"  placeholder="Search..." id="item">
			<input type="submit" name="search"  value="Search">
			<input type="submit" name="newMail" value="New Mail">
			<input type="submit" name="inbox" value="Inbox">
			<input type="submit" name="sent" value="Sent">
		</form>
		
		<%= request.getAttribute("content")!=null ? request.getAttribute("content") : "" %>
	</div>
	
	<script>
	function sanitizeHTML(text) {
		  var element = document.createElement('div');
		  element.innerText = text;
		  return element.innerHTML;
		}
	</script>
</body>
</html>