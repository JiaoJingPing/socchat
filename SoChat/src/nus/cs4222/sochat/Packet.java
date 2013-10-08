package nus.cs4222.sochat;

public class Packet {
	String ID;
	int type;
	int code;
	String content;

	private Packet(String iD, int type, int code, String content) {

		ID = iD;
		this.type = type;
		this.code = code;
		this.content = content;
	}

	public Packet(int type, int code, String content) {
		this(String.valueOf(System.currentTimeMillis()), type, code, content);
	}

	public Packet() {
	}

	public Packet(String pkt) {
		String[] pkt_content = pkt.split("#");
		ID = pkt_content[0];
		type = Integer.parseInt(pkt_content[1]);
		code = Integer.parseInt(pkt_content[2]);
		for (int i = 3; i < pkt_content.length; i++)
			content += pkt_content[i];
	}

	public String toString() {

		return "" + ID + "#" + type + "#" + code + "#" + content;

	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
