package pw.ry4n.dr.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class FixedSizeArrayDequeTest {
	@Test
	public void shouldDropElements() {
    	FixedSizeArrayDeque<String> ring = new FixedSizeArrayDeque<>(3);
        ring.push("A");
        ring.push("B");
        ring.push("C");
        ring.push("D");
        ring.push("E");
        assertFalse(ring.contains("A"));
        assertFalse(ring.contains("B"));
        assertTrue(ring.contains("C"));
        assertTrue(ring.contains("D"));
        assertTrue(ring.contains("E"));
	}

    @Test
    public void shouldAddElementsAtTheFront() {
    	FixedSizeArrayDeque<String> ring = new FixedSizeArrayDeque<>(3);
        ring.push("A");
        ring.push("B");
        ring.push("C");
        ring.push("D");
        ring.push("E");
        assertEquals("E", ring.get(0));
        assertEquals("D", ring.get(1));
        assertEquals("C", ring.get(2));
    }

    @Test
    public void shouldAddElementsAtTheEnd() {
    	FixedSizeArrayDeque<String> ring = new FixedSizeArrayDeque<>(3);
        ring.addLast("A");
        ring.addLast("B");
        ring.addLast("C");
        ring.addLast("D");
        ring.addLast("E");
        assertEquals("C", ring.get(0));
        assertEquals("D", ring.get(1));
        assertEquals("E", ring.get(2));
    }
}
