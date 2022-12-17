package client;

import java.security.PublicKey;

public class User {
	
	String email;
	String password;
	
	PublicKey publicKey;
	

	public User(String email, String pwd) {
		this.email = email;
		this.password = pwd;
	}

	public String getEmail() {
		return email;
	}


	public void setEmail(String name) {
		this.email = name;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}
	
	

}
