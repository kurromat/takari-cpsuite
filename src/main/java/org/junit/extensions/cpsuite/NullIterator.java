/*
 * @author Johannes Link (business@johanneslink.net)
 * 
 * Published under Apache License, Version 2.0 (http://apache.org/licenses/LICENSE-2.0)
 */
package org.junit.extensions.cpsuite;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class NullIterator extends FilenameIterator {

	public Iterator<String> iterator() {
		return this;
	}

	public boolean hasNext() {
		return false;
	}

	public String next() {
		throw new NoSuchElementException();
	}

	public void remove() {
	}

}
