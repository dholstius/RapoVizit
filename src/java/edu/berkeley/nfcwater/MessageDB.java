package org.dsi.sanitrack;

import javax.microedition.rms.*;

import java.util.Enumeration;
import java.util.Vector;
import java.io.*;

import javax.microedition.midlet.MIDlet;

public class MessageDB {
	
	RecordStore recordStore = null;
	RapoVizit parent = null;

	// Open a record store with the given name
	public MessageDB(RapoVizit parent, String fileName) throws RecordStoreException {
		this.parent = parent;
		this.recordStore = RecordStore.openRecordStore(fileName, true);
	}

	public void close() 
			throws RecordStoreNotOpenException, RecordStoreException {
		if (recordStore.getNumRecords() == 0) {
			parent.warn("RecordStore is empty");
			String fileName = recordStore.getName();
			recordStore.closeRecordStore();
			RecordStore.deleteRecordStore(fileName);
		} else {
			recordStore.closeRecordStore();
		}
	}

	public synchronized void put(Message msg) {
		try {
			byte[] b = Message.pickle(msg);
			recordStore.addRecord(b, 0, b.length);
			parent.debug("Added record of length " + b.length + " bytes");
			parent.debug("original is: " + msg);
			parent.debug("pickled is: " + b);
			parent.debug("unpickled(pickled) is: " + Message.unpickle(b));
		} catch(RecordStoreException e) {
			parent.error(e.toString());
		} catch(IOException e) {
			parent.error(e.toString());
		}
	}

	public synchronized int count() 
			throws RecordStoreNotOpenException {
		RecordEnumeration iter = enumerate();
		return iter.numRecords();
	}
	
	public Message get(int recordId) 
			throws RecordStoreException {
		Message msg = null;
		byte[] b = recordStore.getRecord(recordId);
		parent.debug("byte[] is : " + b + " (length " + b.length + " bytes)");
		try {
			msg = Message.unpickle(b);
		} catch(IOException e) {
			throw new RecordStoreException("Failed to convert UTF-encoded bytes from RecordStore");
		}
		parent.debug("Fetched message is: " + msg);
		return msg;
	}
	
	public synchronized void deleteRecord(int recordId) 
			throws RecordStoreException {
		recordStore.deleteRecord(recordId);
	}
	
	// Enumerate through the records.
	public synchronized RecordEnumeration enumerate()
			throws RecordStoreNotOpenException {
		return recordStore.enumerateRecords(null, new MessageComparator(), false);
	}
}
