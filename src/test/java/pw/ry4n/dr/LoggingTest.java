package pw.ry4n.dr;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class LoggingTest {
	/**
	 * Utility method for having mocks log method calls to the console.
	 * 
	 * Example usage:
	 * <pre>
	 * doAnswer(log("sendToClient")).when(sendToClient).send(anyString());
	 * </pre>
	 * 
	 * @param proxyName
	 * @return
	 */
	protected Answer<Object> log(final String proxyName) {
		return new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				for (Object arg : args) {
					System.out.println(proxyName + "." + invocation.getMethod().getName() + "(" + arg + ")");
				}
				return null;
			}
		};
	}
}
