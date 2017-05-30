package TestCode;

import java.io.Serializable;

public class Message implements Serializable {
	public enum MSG_TYPE {
		LOGIN, REGISTER_TOKEN, SEARCH
	};
	
	public MSG_TYPE msgType;
	private static final long serialVersionUID = 427849097562345L;
	public String userId, token, links, password;

	public Message() {
		userId = null;
		token = null;
		links = null;
	}
}
