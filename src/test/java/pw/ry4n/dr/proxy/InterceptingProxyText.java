package pw.ry4n.dr.proxy;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.OutputStream;

import org.junit.Test;

public class InterceptingProxyText {
	@Test
	public void testRepeat() throws IOException {
		OutputStream to = mock(OutputStream.class);
		InterceptingProxy proxy = new InterceptingProxy(to);
		proxy.filter("order 2".getBytes(), 7);
		proxy.filter("order 2".getBytes(), 7);
		proxy.filter("stow nugget".getBytes(), 11);
		proxy.repeat("3");
		assertEquals(3, proxy.commandsToSend.size());
	}
}
