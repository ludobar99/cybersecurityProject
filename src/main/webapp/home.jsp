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

	<script>
		// Transforms string to ArrayBuffer
		function ab2str(buf) {
			return String.fromCharCode.apply(null, new Uint8Array(buf));
		}

		// Transforms ArrayBuffer to string
		function str2ab(str) {
			const buf = new ArrayBuffer(str.length);
			const bufView = new Uint8Array(buf);
			for (let i = 0, strLen = str.length; i < strLen; i++) {
				bufView[i] = str.charCodeAt(i);
			}
			return buf;
		}
	</script>
	<script>
		// Content decryption
		window.addEventListener("load", async () => {

			// Retrieving private key
			const userEmail = '${email}'
			const privateContents = window.localStorage.getItem(`email-client.private.` + userEmail)
			const privateString = window.atob(privateContents);
			const privateRsa = str2ab(privateString);
			const privateKey = await window.crypto.subtle.importKey(
					"pkcs8",
					privateRsa,
					{
						name: "RSA-OAEP",
						hash: "SHA-256"
					},
					true,
					["decrypt"]
			)

			// Decrypting received contents
			const elements = document.querySelectorAll(".email-subject, .email-body");
			for (const element of elements) {
				const contentEncryptedBase64 = element.innerHTML
				const contentEncrypted = window.atob(contentEncryptedBase64);
				const contentDecrypted = await window.crypto.subtle.decrypt(
						{
							name: "RSA-OAEP",
							hash: "SHA-256"
						},
						privateKey,
						str2ab(contentEncrypted)
				)
				// Sanitizing
				var sanitizedContent = DOMPurify.sanitize(contentDecrypted);
				
				element.innerHTML = ab2str(sanitizedContent);
			}
		})
	</script>
	<script>
		// Email sending
		window.addEventListener("load", () => {
			const form = document.getElementById("submitForm");
			async function sendEmail() {

				// Retrieving generic data
				const formData = new FormData(form);
				const receiver = formData.get("receiver");
				const digitalSignature = formData.get("digitalSignature");

				// Fetching receiver public key
				const publicKeyResult = await fetch("ReceiversServlet?" + new URLSearchParams({
					email: receiver
				}), {
					credentials: "same-origin"
				})
				if (!publicKeyResult.ok) throw Error("Error while fetching the public key of the receiver.")

				// Importing received key
				const publicKeyText = await publicKeyResult.text();
				const publicString = window.atob(publicKeyText);
				const publicAb = str2ab(publicString);
				const publicKey = await window.crypto.subtle.importKey(
						"spki",
						publicAb,
						{
							name: "RSA-OAEP",
							hash: "SHA-256"
						},
						true,
						["encrypt"]
				);

				// Content encryption
				const emailBody = formData.get("body");
				const encryptedBody = await window.crypto.subtle.encrypt(
						{
							name: "RSA-OAEP",
						},
						publicKey,
						str2ab(emailBody)
				)

				const emailSubject = formData.get("subject");
				const encryptedSubject = await window.crypto.subtle.encrypt(
						{
							name: "RSA-OAEP",
						},
						publicKey,
						str2ab(emailSubject)
				)

				// Encoding content and overwriting form data
				const encryptedBodyBase64 = window.btoa(ab2str(encryptedBody))
				const encryptedSubjectBase64 = window.btoa(ab2str(encryptedSubject))

				formData.set("body", encryptedBodyBase64);
				formData.set("subject", encryptedSubjectBase64);

				// Sending email
				const sendMailResult = await fetch("SendMailServlet?" + new URLSearchParams(formData), {
					method: "POST",
					credentials: "same-origin",
					headers: {
						'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
					},
				})
				if (!sendMailResult.ok) throw Error("Error while sending the email.")

				// Rendering received HTML
				document.body.innerHTML = await sendMailResult.text()
			}
			if (form) {
				form.addEventListener("submit", async (event) => {
					event.preventDefault();
					await sendEmail();
				});
			}
		})
	</script>

</head>
<body>
	<nav class="navbar">
	  	<div id="title">
			<p>E-Mail Client</p>
	  	</div>
	  	<div id="right">
			<p id="email">
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