package pw.ry4n.dr.proxy;

/**
 * An interface to receive notification of stream activity.
 * 
 * @author cpowel
 */
public interface StreamListener {
	/**
	 * Receive notification of some line of text passed through the stream. The
	 * notify method itself should perform only minimal processing and never
	 * call a sleep or wait. Processing of data should be handled in another
	 * thread if at all possible.
	 * 
	 * @param line
	 */
	void notify(String line);
}
