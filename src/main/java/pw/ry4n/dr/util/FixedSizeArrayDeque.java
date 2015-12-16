package pw.ry4n.dr.util;

import java.util.ArrayDeque;
import java.util.Iterator;

public class FixedSizeArrayDeque<E> extends ArrayDeque<E> {
	private static final long serialVersionUID = -8786518798204513723L;

	int maxSize = -1;

	public FixedSizeArrayDeque(int size) {
		super(size);
		this.maxSize = size;
	}

	@Override
	public boolean add(E e) {
		if (size() == maxSize) {
			removeFirst();
		}
		super.addLast(e);
		return true;
	}

	@Override
	public boolean offer(E e) {
		addLast(e);
		return true;
	}

	@Override
	public void addLast(E e) {
		add(e);
	}

	@Override
	public boolean offerLast(E e) {
		return offer(e);
	}

	@Override
	public void addFirst(E e) {
		while (size() >= maxSize) {
			removeLast();
		}
		super.addFirst(e);
	}

	public boolean offerFirst(E e) {
		addFirst(e);
		return true;
	}

	@Override
	public void push(E e) {
		addFirst(e);
	}

	/**
	 * Returns the {@code i}<sup>th</sup> element from the first position in the
	 * queue.
	 * 
	 * @param i
	 * @return
	 */
	public E get(int i) {
		if (i >= size()) {
			throw new IndexOutOfBoundsException();
		}
		Iterator<E> itr = iterator();
		for (int index = 0; index < i; index++) {
			itr.next();
		}
		return itr.next();
	}

	/**
	 * Returns the {@code i}<sup>th</sup> element from the last position in the
	 * queue.
	 * 
	 * @param i
	 * @return
	 */
	public E getDescending(int i) {
		if (i >= size()) {
			throw new IndexOutOfBoundsException();
		}
		Iterator<E> itr = descendingIterator();
		for (int index = 0; index < i; index++) {
			itr.next();
		}
		return itr.next();
	}

	public boolean isFull() {
		return size() == maxSize;
	}
}
