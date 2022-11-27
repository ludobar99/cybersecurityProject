package mail;

public class EMail {

	private String receiver;
	private byte[] subject;
	private byte[] body;
	private byte[] digitalSignature;
	private String timestamp;
	
	private String sender;
	public EMail(String sender, String receiver, byte[] subject, byte[] body, String timestamp) {
		super();
		this.sender = sender;
		this.receiver = receiver;
		this.subject = subject;
		this.body = body;
		this.timestamp = timestamp;
	}
	public EMail(String sender, String receiver, byte[] subject, byte[] body, byte[] digitalSignature, String timestamp) {
		super();
		this.sender = sender;
		this.receiver = receiver;
		this.subject = subject;
		this.body = body;
		this.digitalSignature = digitalSignature;
		this.timestamp = timestamp;
	}
	
	public String getReceiver() {
		return receiver;
	}
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
	public byte[] getSubject() {
		return subject;
	}
	public void setSubject(byte[] subject) {
		this.subject = subject;
	}
	public byte[] getBody() {
		return body;
	}
	public void setBody(byte[] body) {
		this.body = body;
	}
	public byte[] getDigitalSignature() {
		return digitalSignature;
	}
	public void setDigitalSignature(byte[] digitalSignature) {
		this.digitalSignature = digitalSignature;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}

}
