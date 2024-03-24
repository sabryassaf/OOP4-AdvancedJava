package solution;

import provided.*;

import java.io.Serial;
import java.util.List;

public abstract class StoryTestException extends Exception {
	@Serial
	private static final long serialVersionUID = 95576353840828036L;
	private String line;
	private List<String> expected;
	private List<String> actual;
    private int failures;

	public StoryTestException(String line, List<String> expected, List<String> actual) {
		this.line = line;
		this.expected = expected;
		this.actual = actual;
        this.failures = 0;
	}
	
	/**
	 * Returns a string representing the sentence 
	 * of the first Then sentence that failed
	 */
	public String getSentance() { return line; }
	
	/**
	 * Returns a string representing the expected value from the story 
	 * of the first Then sentence that failed.
	 */
	public List<String> getStoryExpected() { return expected; }
	
	/**
	 * Returns a string representing the actual value.
	 * of the first Then sentence that failed.
	 */
	public List<String> getTestResult() { return actual; }
	
	/**
	 * Returns an int representing the number of Then sentences that failed.
	 */
	public int getNumFail() { return failures; }

	void setNumFail(int num) {
		this.failures = num;
	}
}
