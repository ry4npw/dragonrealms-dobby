package pw.ry4n.dr.proxy;

public interface StreamMonitor {
	public void subscribe(StreamListener listener);
	public void unsubscribe(StreamListener listener);
}
