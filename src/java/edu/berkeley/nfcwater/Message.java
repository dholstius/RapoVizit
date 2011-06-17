package org.dsi.sanitrack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Calendar;

import javax.microedition.rms.RecordStoreException;

public class Message {

	public long time;
	public String payload;
	
	public Message(long time, String payload) {
		this.time = time;
		this.payload = payload;
	}
	
	public Message(Date date, String payload) {
		this(date.getTime(), payload);
	}
	
	public String toString() {
		return "[" + Long.toString(time) + "] " + payload;
	}
	
	public static final Message unpickle(byte[] b) throws IOException {
		Message msg = null;
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		DataInputStream dis = new DataInputStream(bais);
		String unpickled = new String(dis.readUTF());
		System.out.println("unpickled is: " + unpickled);
		int pos = unpickled.indexOf(',');
		long time;
		String timeComponent = unpickled.substring(0, pos);
		System.out.println("timeComponent is: " + timeComponent);
		time = Long.parseLong(timeComponent);
		System.out.println("pos is: " + pos);
		String payload = new String(unpickled.substring(pos + 1, unpickled.length()));
		msg = new Message(time, payload);
		return msg;
	}
	
	public static final byte[] pickle(Message msg) throws IOException {
		byte b[] = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream outputStream = new DataOutputStream(baos);
		String timeComponent = Long.toString(msg.time);
		System.out.println("timeComponent is: " + timeComponent);
		outputStream.writeUTF(timeComponent + "," + msg.payload);
		b = baos.toByteArray();
		return b;
	}
	
}
