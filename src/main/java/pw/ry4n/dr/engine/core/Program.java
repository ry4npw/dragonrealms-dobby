package pw.ry4n.dr.engine.core;

public interface Program extends Runnable {
	String getName();
	void pause();
	State getState();
	void resume();
	void stop();
	Thread getThread();
	void setThread(Thread t);
}
