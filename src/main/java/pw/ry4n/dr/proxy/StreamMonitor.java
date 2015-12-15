package pw.ry4n.dr.proxy;

/**
 * Interface describing a stream that can be monitored. Classes implementing the
 * {@link StreamListener} interface can subscribe and unsubscribe to receive
 * stream notifications.
 * 
 * @author cpowel
 */
public interface StreamMonitor {
	/**
	 * Subscribe a listener to this stream's output, classes subscribing will
	 * have their {@link StreamListener#notify(String)} methods called with text
	 * that flows through the monitored stream.
	 * 
	 * @param listener
	 */
	public void subscribe(StreamListener listener);

	/**
	 * Unsubscribe a listener from monitoring this stream.
	 * 
	 * @param listener
	 */
	public void unsubscribe(StreamListener listener);
}
