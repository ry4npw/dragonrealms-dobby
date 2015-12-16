package pw.ry4n.dr.proxy;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class InterceptingProxyTest {
	@Test
	public void testRepeat() throws IOException {
		OutputStream to = mock(OutputStream.class);
		AbstractProxy abstractProxy = mock(AbstractProxy.class);
		InterceptingProxy proxy = new InterceptingProxy(abstractProxy, to);

		proxy.filter("order 2".getBytes(), 7);
		proxy.filter("order 2".getBytes(), 7);
		proxy.filter("stow nugget".getBytes(), 11);
		proxy.repeat("3");

		assertEquals(3, proxy.commandsToSend.size());
	}

	@Test
	public void testParseEvery() {
		OutputStream to = mock(OutputStream.class);
		AbstractProxy abstractProxy = mock(AbstractProxy.class);
		InterceptingProxy proxy = new InterceptingProxy(abstractProxy, to);

		TimedThread tt = proxy.parseEvery("91 seconds PREDICT WEATHER");

		assertNotNull(tt);
		assertEquals(91, tt.getDuration());
		assertEquals(TimeUnit.SECONDS, tt.getTimeUnit());
		assertEquals("predict weather", tt.getCommand());
	}

	@Test
	public void testParseEveryWithShorthandTimeUnit() {
		OutputStream to = mock(OutputStream.class);
		AbstractProxy abstractProxy = mock(AbstractProxy.class);
		InterceptingProxy proxy = new InterceptingProxy(abstractProxy, to);

		TimedThread tt = proxy.parseEvery("91s PREDICT WEATHER");

		assertNotNull(tt);
		assertEquals(91, tt.getDuration());
		assertEquals(TimeUnit.SECONDS, tt.getTimeUnit());
		assertEquals("predict weather", tt.getCommand());
	}

	@Test(expected = NumberFormatException.class)
	public void testParseEveryThrowsNumberFormatException() {
		OutputStream to = mock(OutputStream.class);
		AbstractProxy abstractProxy = mock(AbstractProxy.class);
		InterceptingProxy proxy = new InterceptingProxy(abstractProxy, to);

		proxy.parseEvery("ten seconds PREDICT WEATHER");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testParseEveryThrowsIllegalArgumentExceptionForInvalidTimeUnit() {
		OutputStream to = mock(OutputStream.class);
		AbstractProxy abstractProxy = mock(AbstractProxy.class);
		InterceptingProxy proxy = new InterceptingProxy(abstractProxy, to);

		proxy.parseEvery("91 days PREDICT WEATHER");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testParseEveryThrowsIllegalArgumentExceptionForMissingCommand() {
		OutputStream to = mock(OutputStream.class);
		AbstractProxy abstractProxy = mock(AbstractProxy.class);
		InterceptingProxy proxy = new InterceptingProxy(abstractProxy, to);

		proxy.parseEvery("5s");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testParseEveryThrowsIllegalArgumentExceptionForBlankCommand() {
		OutputStream to = mock(OutputStream.class);
		AbstractProxy abstractProxy = mock(AbstractProxy.class);
		InterceptingProxy proxy = new InterceptingProxy(abstractProxy, to);

		proxy.parseEvery("5s ");
	}
}
