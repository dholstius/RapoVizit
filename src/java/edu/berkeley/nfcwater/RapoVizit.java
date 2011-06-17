package org.dsi.sanitrack;

import java.util.*;

import javax.microedition.contactless.ContactlessException;
import javax.microedition.contactless.DiscoveryManager;
import javax.microedition.contactless.TargetListener;
import javax.microedition.contactless.TargetProperties;
import javax.microedition.contactless.TargetType;

import javax.microedition.io.Connector;
import javax.microedition.io.ConnectionNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import javax.microedition.rms.*;

public class RapoVizit extends MIDlet implements TargetListener,
		CommandListener {

	private MessageDB outbox;

	private Display display;
	private Displayable current;

	private Form initialScreen, confirmationScreen, q1, finalScreen;
	private QuestionForm q2, q3;
	private Vector formList;
	private StringItem summaryField;
	
	// 31093960: DSI office
	// 38236859: Michael Ritter
	// was: private final TextField phoneNumberField = new TextField("#", "37027245", 255, TextField.PHONENUMBER);
	private final StringItem phoneNumberField = new StringItem("#", this.getServerNumber());

	private boolean done = false;
	
	public static final Command exitCommand = new Command("Sòti", Command.EXIT,
			0);

	public static final Command backCommand = new Command("Tounen",
			Command.CANCEL, 1);

	//public static final Command cancelCommand = new Command("Cancel",
	//		Command.CANCEL, 0);

	public static final Command okCommand = new Command("OK", Command.OK, 0);

	public static final Command selectCommand = new Command("OK",
			Command.OK, 1);

	public static final Command sendCommand = new Command("Voye", Command.OK, 0);

	public static final Command notNowCommand = new Command("Pita",
			Command.CANCEL, 0);

	private TextField householdField;

	private DateField dateField;

	public RapoVizit() {
		// TODO Auto-generated constructor stub
	}

	public void showAlert(String title, String message, AlertType alertType,
			int timeout) {
		try {
			Alert alert = new Alert(title, message, null, alertType);
			alert.setTimeout(timeout);
			Display display = Display.getDisplay(this);
			Displayable current = display.getCurrent();
			display.setCurrent(alert, current); // was: alert, current
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception in showAlert(): " + e);
		}
	}

	public void targetDetected(TargetProperties[] prop) {
		String tagId;
		tagId = prop[0].getUid();
		// Only change the nameField if it's showing
		Displayable d = Display.getDisplay(this).getCurrent();
		if (d == initialScreen) {
			householdField.setString(tagId);
			d.addCommand(okCommand);
		}
	}

	private void initialize() {
		
		// Initial screen
		initialScreen = new Form(null, null);
		initialScreen.addCommand(okCommand);
		initialScreen.addCommand(exitCommand);
		householdField = new TextField("Kay la", "", 255, TextField.ANY);
		initialScreen.append(householdField);
		initialScreen.append(new StringItem("", "Mete telefon sou tag la.  Apre sa, prese OK."));
		initialScreen.setItemStateListener(new ItemStateListener() {
			public void itemStateChanged(Item item) {
				initialScreen.addCommand(okCommand);
			}
		});
		initialScreen.setCommandListener(this);
		
		// First question: date and time
		q1 = new Form(null, null);
		Date now = new Date(System.currentTimeMillis());
		dateField = new DateField("Dat vizit", DateField.DATE_TIME);
		dateField.setDate(now);
		q1.append(dateField);
		q1.addCommand(okCommand);
		q1.addCommand(backCommand);
		q1.setCommandListener(this);

		q2 = new QuestionForm( this,
				"Ki rezilta ou jwenn nan tès klorin?", "Rezilta tès",
				new String[] { "Negatif (-)", "Positif (+)", "Pa gen dlo", "Pa konnen" });

		q3 = new QuestionForm( this,
				"Ki kantite likid ou vann pandan vizit la?", "Likid vann",
				new String[] { "Yon boutey", "4 mezi", "3 mezi", "2 mezi",
						"1 mezi", "Pa vann", "Pa konnen" });

		// Confirmation screen
		confirmationScreen = new Form(null, null);
		confirmationScreen.append(new StringItem("", "OK pou voye pa SMS?"));
		confirmationScreen.append(q2.responseField);
		confirmationScreen.append(q3.responseField);
		confirmationScreen.append(phoneNumberField);
		confirmationScreen.addCommand(backCommand);
		confirmationScreen.addCommand(okCommand);
		confirmationScreen.setCommandListener(this);
		
		// Final screen
		finalScreen = new Form(null, null);
		summaryField = new StringItem("", "Pwochen fwa ou jwenn ond la, ouvri RapoVizit pou ou ka voye yo ale.");
		finalScreen.append(summaryField);
		finalScreen.addCommand(okCommand);
		finalScreen.addCommand(exitCommand);
		finalScreen.setCommandListener(new CommandListener() {
			public void commandAction(Command c, Displayable d) {
				exitMIDlet();
			}
		});
		
		formList.addElement(q1);
		formList.addElement(q2);
		formList.addElement(q3);
		formList.addElement(confirmationScreen);

		display = Display.getDisplay(this);
		
	}

	/*
	protected class QuestionForm extends List {
		
		public String longForm;
		public String shortForm;
		public String[] choices;
		public StringItem responseField;

		protected QuestionForm(RapoVizit parent, String longForm, String shortForm, String[] choices) {
			super(longForm, List.IMPLICIT, choices, null);
			this.shortForm = shortForm;
			this.longForm = longForm;
			this.addCommand(okCommand);
			this.addCommand(backCommand);
			this.setCommandListener(parent);
		}
		
		public String getResponse() {
			return "FIXME";
		}

	}
	*/
	
	protected class QuestionForm extends Form implements CommandListener,
			ItemStateListener {

		public String longForm;
		public String shortForm;
		public String[] choices;
		public ChoiceGroup choiceField;
		public TextField commentField;
		public StringItem responseField;

		protected String getCurrentSelection() {
			return choiceField.getString(choiceField.getSelectedIndex());
		}

		public void commandAction(Command command, Displayable d) {
			if (command == okCommand || command == selectCommand) {
				showNext();
			} else if (command == backCommand) {
				showPrevious();
			}
		}

		public void itemStateChanged(Item item) {
			// Update the responseField to mirror the selection (and,
			// optionally,
			// the comment)
			String responseSummary = "";
			responseSummary += choiceField.getString(choiceField
					.getSelectedIndex());
			if (commentField.size() > 0) {
				responseSummary += "(" + commentField.getString() + ")";
			}
			responseField.setText(responseSummary);
			showNext(); // was: addCommand(okCommand);
		}

		protected QuestionForm(RapoVizit parent, String longForm, String shortForm,
				String[] choices) {
			
			super("RapoVizit");
			this.shortForm = shortForm;
			this.longForm = longForm;
			this.choices = choices;

			choiceField = new ChoiceGroup(longForm, Choice.EXCLUSIVE, choices,
					null);
			// choiceField.setSelectedIndex(choiceField.size() - 1, true);
			this.append(choiceField);

			this.responseField = new StringItem(shortForm,
					getCurrentSelection());
			// Don't append the responseField here (we'll append it to the
			// summaryScreen instead)

			this.commentField = new TextField("Nòt:", "", 48, TextField.ANY);
			//this.append(commentField);

			this.addCommand(backCommand);
			
			this.setCommandListener(this);
			this.setItemStateListener(this);
		}
	}
	
	public void commandAction(Command command, Displayable displayable) {

		Display display = Display.getDisplay(this);
		Displayable currentForm = display.getCurrent();

		if (command == exitCommand) {
			exitMIDlet();
		}
		
		if (currentForm == initialScreen) {
			if (command == okCommand) {
					// Must have input something (name, tag, etc.) to be valid
					if (householdField.size() > 0) {
						display.setCurrent(q1);
					} else {
						showAlert("Error",
								"Si tag la pa mache, ou ka tape non kay la.",
								AlertType.ERROR, Alert.FOREVER);
					}
			}
		} else if (currentForm == q1) {
			if (command == okCommand) {
				Display.getDisplay(this).setCurrent(q2);
			} else if (command == backCommand) {
				Display.getDisplay(this).setCurrent(initialScreen);
			}
		} else if (currentForm == q2) {
			if (command == okCommand) {
				Display.getDisplay(this).setCurrent(q1);
			} else if (command == backCommand) {
				Display.getDisplay(this).setCurrent(q3);
			}
		} else if (currentForm == q3) {
			if (command == okCommand) {
				Display.getDisplay(this).setCurrent(confirmationScreen);
			} else if (command == backCommand) {
				Display.getDisplay(this).setCurrent(q2);
			}
		} else if (currentForm == confirmationScreen) {
			if (command == okCommand) {
				finishSurvey();
			} else if (command == backCommand) {
				Display.getDisplay(this).setCurrent(q3);
			}
		} 
	}

	protected synchronized void startApp() throws MIDletStateChangeException {

		try {
			outbox = new MessageDB(this, "myDB");
		} catch (RecordStoreNotOpenException e) {
			error("RecordStoreNotOpenException in startApp(): " + e);
		} catch (Exception e) {
			error("Exception in startApp(): " + e);
		}

		// Keep track of UI screens (so as to support "Back" button)
		formList = new Vector();

		// Get instance of NFC Discovery Manager
		DiscoveryManager dm = DiscoveryManager.getInstance();

		// Register NDEF_TAG target to discovery
		try {
			dm.addTargetListener(this, TargetType.NDEF_TAG);
		} catch (IllegalStateException e) {
			// Catch IllegalStateException
		} catch (ContactlessException e) {
			// Catch ContactlessException
		}

		initialize();
		
		Display display = Display.getDisplay(this);
		display.setCurrent(initialScreen);

		try {
			confirmFlushOutbox();
		} catch (IOException e) {
			// fail silently
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

	}

	protected void showNext() {
		display = Display.getDisplay(this);
		Displayable current = display.getCurrent();
		int index = formList.indexOf(current);
		Displayable next = (Displayable) formList.elementAt(index + 1);
		display.setCurrent(next);
	}

	protected void showPrevious() {
		display = Display.getDisplay(this);
		current = display.getCurrent();
		int index = formList.indexOf(current);
		Displayable previous = (Displayable) formList.elementAt(index - 1);
		display.setCurrent(previous);
	}

	public void destroyApp(boolean unconditional) {
		try {
			outbox.close();
		} catch (Exception e) {
			error("Exception in outbox.close(): " + e);
		}
		notifyDestroyed();
	}

	protected void pauseApp() {
		// TODO Auto-generated method stub
	}

	public void exitMIDlet() {
		try {
			display.setCurrent(null);
			outbox.close();
		} catch (Exception e) {
			error("Exception in exitMIDlet(): " + e);
		}
		notifyDestroyed();
	}

	public void finishSurvey() {
		String payload = "RV-0.3";
		payload += ", " + householdField.getString();
		payload += ", "
				+ DateFormat.toString("YYYY-MM-DD hh:mm", dateField.getDate());
		for (int i = 1; i < formList.size() - 1; i++) {
			Object item = formList.elementAt(i);
			QuestionForm q = (QuestionForm) item;
			String response = q.responseField.getText();	// includes comment, if any!
			payload += ", " + response;
		}
		// FIXME: check bounds / message length
		Date now = Calendar.getInstance().getTime();
		Message msg = new Message(now, payload);
		outbox.put(msg);
		debug("invoked outbox.put() with: " + msg);
		this.done = true;
		try {
			confirmFlushOutbox();
		} catch (IOException e) {
			// connection unavailable?
			// TODO: handle it
		}
	}

	String getServerNumber() {
		// was: return phoneNumberField.getString();
		return "37027245";
	}

	public synchronized void confirmFlushOutbox() throws IOException {

		// Check for a viable connection
		try {
			MessageConnection conn = (MessageConnection) Connector.open("sms://" + getServerNumber());
		
			RecordEnumeration iter = outbox.enumerate();
			int initialOutboxCount = iter.numRecords();
			if (initialOutboxCount == 0) {
				return;
			} else {
				// Inform user we're about to send multiple SMS
				String msg = initialOutboxCount + " SMS poko ale. Voye yo kounye a?";
				Alert confirmationAlert = new Alert("", msg, null,
						AlertType.CONFIRMATION);
				confirmationAlert.setTimeout(Alert.FOREVER);
				confirmationAlert.addCommand(sendCommand);
				confirmationAlert.addCommand(notNowCommand);
				confirmationAlert.setCommandListener(new CommandListener() {
						public void commandAction(Command c, Displayable d) {
							if (c == sendCommand) {
								doFlushOutbox();
							} else {
								if(done) {
									display.setCurrent(finalScreen);
								} else {
									display.setCurrent(initialScreen);
								}
							}
						}
					});
				Display display = Display.getDisplay(this);
				if(done) {
					display.setCurrent(confirmationAlert, finalScreen);
				} else {
					display.setCurrent(confirmationAlert, q1);
				}
				
			} // end if
		} catch (RecordStoreException e) {
			debug(e.toString());
		} catch (NullPointerException e) {
			e.printStackTrace();
		} 
	}

	public synchronized void doFlushOutbox() {
		int sentMessageCount = 0;
		String summaryMessage = "";
		RecordEnumeration iter = null;
		try {
			MessageConnection conn = (MessageConnection) Connector
					.open("sms://" + getServerNumber());
			iter = outbox.enumerate();
			while (iter.hasNextElement()) {
				int recordId = iter.nextRecordId();
				try {
					Message m = outbox.get(recordId);
					String payload = m.payload;
					TextMessage msg = (TextMessage) conn
							.newMessage(MessageConnection.TEXT_MESSAGE);
					msg.setPayloadText(payload);
					conn.send(msg);
					debug("sent: " + payload);
					sentMessageCount++;
					outbox.deleteRecord(recordId);
					debug("deleted: record " + recordId);
				} catch (InvalidRecordIDException e) {
					warn("Invalid record ID: " + recordId);
				} catch (SecurityException e) {
					// pass; probably user declined
				}
			}
			summaryField.setText("Tout bagay ale byen.");
			try {
				conn.close();
				debug("connection closed.");
			} catch (IOException e2) {
				// pass
			}
		} catch (ConnectionNotFoundException e) {
			error("Connection not found");
		} catch (RecordStoreNotOpenException e) {
			debug("RecordStore not open");
		} catch (RecordStoreException e) {
			warn("RecordStoreException: " + e);
		} catch (IOException e) {
			warn("Connection unavailable. Message saved.");
			summaryField.setText(iter.numRecords() + " SMS poko ale.\n\nPwochen fwa ou jwenn ond la, ouvri RapoVizit pou ou ka voye yo ale.");
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		debug("doFlushOutbox() finished.");
		
		if(done) {
			Display.getDisplay(this).setCurrent(finalScreen);
		} else {
			Display.getDisplay(this).setCurrent(initialScreen);
		}
	}

	public synchronized void debug(String debugMessage) {
		System.out.println("[debug] " + debugMessage);
	}

	public synchronized void info(String infoMessage) {
		System.out.println("[info] " + infoMessage);
	}

	public synchronized void warn(String warnMessage) {
		System.out.println("[warn] " + warnMessage);
	}

	public synchronized void error(String errorMessage) {
		System.out.println("[error] " + errorMessage);
	}

}