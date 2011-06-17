package org.dsi.sanitrack;

import javax.microedition.rms.RecordComparator;
import java.io.IOException;

public class MessageComparator implements RecordComparator {

	public int compare(byte[] rec1, byte[] rec2) {
		try {
			Message msg1 = Message.unpickle(rec1);
			Message msg2 = Message.unpickle(rec2);
			if(msg1.time < msg2.time) {
				return RecordComparator.PRECEDES;
			} else if (msg1.time > msg2.time) {
				return RecordComparator.FOLLOWS;
			} else {
				return RecordComparator.EQUIVALENT;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return RecordComparator.EQUIVALENT;
		}
	}
}
