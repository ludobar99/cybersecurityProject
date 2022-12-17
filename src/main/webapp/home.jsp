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

		// Retrieves private key for decryption
		async function getDecryptionKey() {
			const userEmail = '${email}'
			const privateContents = window.localStorage.getItem(`email-client.private.` + userEmail)
			const privateString = window.atob(privateContents);
			const privateRsa = str2ab(privateString);
			return await window.crypto.subtle.importKey(
				"pkcs8",
				privateRsa,
				{
					name: "RSA-OAEP",
					hash: "SHA-256",
				},
				false,
				["decrypt"]
			)
		}

		// Retrieves private key for signing
		async function getSigningKey() {
			const userEmail = '${email}'
			const privateContents = window.localStorage.getItem(`email-client.private.` + userEmail)
			const privateString = window.atob(privateContents);
			const privateRsa = str2ab(privateString);
			return await window.crypto.subtle.importKey(
				"pkcs8",
				privateRsa,
				{
					name: "RSA-PSS",
					hash: "SHA-256"
				},
				false,
				["sign"]
			)
		}

		// Fetches public key of user from backend and imports it for encryption
		async function getEncryptionKey(email) {

			// Fetching public key
			const publicKeyResult = await fetch("ReceiversServlet?" + new URLSearchParams({
				email
			}), {
				credentials: "same-origin"
			})
			if (!publicKeyResult.ok) document.body.innerHTML = await publicKeyResult.text()

			// Importing received key
			const publicKeyString = await publicKeyResult.text();
			const publicString = window.atob(publicKeyString);
			const publicAb = str2ab(publicString);
			return await window.crypto.subtle.importKey(
				"spki",
				publicAb,
				{
					name: "RSA-OAEP",
					hash: "SHA-256"
				},
				false,
				["encrypt"]
			);
		}

		// Fetches public key of user from backend and imports it for verification
		async function getVerificationKey(email) {

			// Fetching public key
			const publicKeyResult = await fetch("ReceiversServlet?" + new URLSearchParams({
				email
			}), {
				credentials: "same-origin"
			})
			if (!publicKeyResult.ok) document.body.innerHTML = await publicKeyResult.text();

			// Importing received key
			const publicKeyString = await publicKeyResult.text();
			const publicString = window.atob(publicKeyString);
			const publicAb = str2ab(publicString);
			return await window.crypto.subtle.importKey(
				"spki",
				publicAb,
				{
					name: "RSA-PSS",
					hash: "SHA-256"
				},
				false,
				["verify"]
			);
		}
	</script>
	<script>
		// Content decryption
		window.addEventListener("load", async () => {

			// Instantiating sanitizer
			const sanitizer = new Sanitizer()

			// Retrieving private key
			const privateKey = await getDecryptionKey();

			// Decrypting received contents
			const mails = document.getElementsByClassName("mail-inbox");
			for (const mail of mails) {

				// Verifies signature if present
				const signatureElements = mail.getElementsByClassName("email-signature")
				let signature, publicKey;
				if (signatureElements.length > 0) {

					// 	Gets sender email
					const senderElements = mail.getElementsByClassName("email-sender")
					const sender = senderElements.item(0).innerHTML;

					// Setting properties
					publicKey = await getVerificationKey(sender);
					signature = signatureElements.item(0).innerHTML
				}

				const elements = mail.querySelectorAll(".email-body")
				for (const element of elements) {
					const contentEncryptedBase64 = element.innerHTML
					const contentEncrypted = window.atob(contentEncryptedBase64);
					const contentDecryptedBase64 = await window.crypto.subtle.decrypt(
							{
								name: "RSA-OAEP",
								hash: "SHA-256"
							},
							privateKey,
							str2ab(contentEncrypted)
					)

					// Decoding
					const contentDecrypted = window.atob(ab2str(contentDecryptedBase64));

					// Sanitizing
					element.setHTML(contentDecrypted, { sanitizer });

					// Verifies signature
					if (signature && publicKey) {
						const result = window.crypto.subtle.verify(
							{
								name: "RSA-PSS",
								saltLength: 32,
							},
							publicKey,
							signature,
							str2ab(contentEncryptedBase64)
						)
						if (!result) continue;
						element.innerHTML = element.innerHTML
								+ "<br><br>&#9989 This email was signed by the sender"

					}
				}
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
				const emailBody = formData.get("body");
				const digitalSignature = formData.get("digitalSignature");

				// Fetching receiver public key
				const publicKey = await getEncryptionKey(receiver);

				// Generating signature if digitalSignature is true
				if (digitalSignature) {
					const privateKey = await getSigningKey();
					const emailBodyEncoded = window.btoa(emailBody);
					const signature = await window.crypto.subtle.sign(
							{
								name: "RSA-PSS",
								saltLength: 32,
							},
							privateKey,
							str2ab(emailBodyEncoded)
					);

					// Adding signature to form data
					const signatureBase64 = window.btoa(ab2str(signature));
					formData.set("signature", signatureBase64);
				}

				console.log("hello")

				const bodyBase64 = window.btoa(emailBody)
				const encryptedBody = await window.crypto.subtle.encrypt(
					{
						name: "RSA-OAEP",
					},
					publicKey,
					str2ab(bodyBase64)
				)

				console.log("helooo")

				// Encoding content and overwriting form data
				const encryptedBodyBase64 = window.btoa(ab2str(encryptedBody))
				formData.set("body", encryptedBodyBase64);

				// Sending email
				const sendMailResult = await fetch("SendMailServlet?" + new URLSearchParams(formData), {
					method: "POST",
					credentials: "same-origin",
					headers: {
						'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
					},
				})
				if (!sendMailResult.ok) document.body.innerHTML = await sendMailResult.text()

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