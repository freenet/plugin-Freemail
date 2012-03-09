package freemail.imap;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class IMAPAppendTest extends IMAPTestBase {
	private static final List<String> INITIAL_RESPONSES;
	static {
		List<String> backing = new LinkedList<String>();
		backing.add("* OK [CAPABILITY IMAP4rev1 CHILDREN NAMESPACE] Freemail ready - hit me with your rhythm stick.");
		backing.add("0001 OK Logged in");
		backing.add("* FLAGS (\\Seen \\Answered \\Flagged \\Deleted \\Draft \\Recent)");
		backing.add("* OK [PERMANENTFLAGS (\\Seen \\Answered \\Flagged \\Deleted \\Draft \\Recent)] Limited");
		backing.add("* 10 EXISTS");
		backing.add("* 10 RECENT");
		backing.add("* OK [UIDVALIDITY 1] Ok");
		backing.add("0002 OK [READ-WRITE] Done");
		INITIAL_RESPONSES = Collections.unmodifiableList(backing);
	}

	public void testBasicAppendFromSelectedState() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 APPEND INBOX {23}");
		commands.add("Subject: Test message");
		commands.add("0004 UID FETCH 10:* FLAGS");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("+ OK");
		expectedResponse.add("0003 OK APPEND completed");
		expectedResponse.add("* 10 FETCH (FLAGS () UID 10)");
		expectedResponse.add("* 11 FETCH (FLAGS (\\Recent) UID 11)");
		expectedResponse.add("0004 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	public void testAppendWithFlag() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 APPEND INBOX (\\Seen) {23}");
		commands.add("Subject: Test message");
		commands.add("0004 UID FETCH 10:* FLAGS");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("+ OK");
		expectedResponse.add("0003 OK APPEND completed");
		expectedResponse.add("* 10 FETCH (FLAGS () UID 10)");
		expectedResponse.add("* 11 FETCH (FLAGS (\\Seen \\Recent) UID 11)");
		expectedResponse.add("0004 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	/*
	 * The expectation here is that the custom flag is ignored since they aren't supported, but the
	 * \Seen flag should still be saved.
	 */
	public void testAppendWithCustomFlag() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 APPEND INBOX (\\Seen custom) {23}");
		commands.add("Subject: Test message");
		commands.add("0004 UID FETCH 10:* FLAGS");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("+ OK");
		expectedResponse.add("0003 OK APPEND completed");
		expectedResponse.add("* 10 FETCH (FLAGS () UID 10)");
		expectedResponse.add("* 11 FETCH (FLAGS (\\Seen \\Recent) UID 11)");
		expectedResponse.add("0004 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	public void testAppendWithTwoStandardFlags() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 APPEND INBOX (\\Seen \\Flagged) {23}");
		commands.add("Subject: Test message");
		commands.add("0004 UID FETCH 10:* FLAGS");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("+ OK");
		expectedResponse.add("0003 OK APPEND completed");
		expectedResponse.add("* 10 FETCH (FLAGS () UID 10)");
		expectedResponse.add("* 11 FETCH (FLAGS (\\Seen \\Flagged \\Recent) UID 11)");
		expectedResponse.add("0004 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	public void testAppendWithFlagAndDate() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 APPEND INBOX (\\Seen) \"23-Oct-2007 19:05:17 +0100\" {39}");
		commands.add("Subject: Test message");
		commands.add("");
		commands.add("Test message");
		commands.add("0004 UID FETCH 10:* FLAGS");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("+ OK");
		expectedResponse.add("0003 OK APPEND completed");
		expectedResponse.add("* 10 FETCH (FLAGS () UID 10)");
		expectedResponse.add("* 11 FETCH (FLAGS (\\Seen \\Recent) UID 11)");
		expectedResponse.add("0004 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	public void testAppendWithBadLiteralLength() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 APPEND INBOX {BAD}");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 BAD Unable to parse literal length");

		runSimpleTest(commands, expectedResponse);
	}

	public void testMultilineAppend() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("7 append \"INBOX\" (\\Seen) {42}");
		commands.add("To: zidel@zidel.freemail");
		commands.add("");
		commands.add("Test message");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("+ OK");
		expectedResponse.add("7 OK APPEND completed");

		runSimpleTest(commands, expectedResponse);
	}

	public void testMultilineAppendWithTwoFlags() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("7 append \"INBOX\" (\\Seen \\Deleted) {42}");
		commands.add("To: zidel@zidel.freemail");
		commands.add("");
		commands.add("Test message");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("+ OK");
		expectedResponse.add("7 OK APPEND completed");

		runSimpleTest(commands, expectedResponse);
	}
}
