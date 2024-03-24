package solution;

import provided.*;

import java.io.Serial;
import java.util.List;

public  class StoryTestExceptionImpl extends StoryTestException {
    @Serial
    private static final long serialVersionUID = 95576353840828036L;
    private String line;
    private String expected;
    private String actual;
    private int failures;

    public StoryTestExceptionImpl() {
        this.failures = 0;
    }

    /**
     * Returns a string representing the sentence
     * of the first Then sentence that failed
     */
    public String getSentance() { return line.stripTrailing(); }

    /**
     * Returns a string representing the expected value from the story
     * of the first Then sentence that failed.
     */
    public String getStoryExpected() { return expected; }

    /**
     * Returns a string representing the actual value.
     * of the first Then sentence that failed.
     */
    public String getTestResult() { return actual; }

    /**
     * Returns an int representing the number of Then sentences that failed.
     */
    public int getNumFail() { return failures; }

    public void setNumFail(int num) {
        this.failures = num;
    }
}
