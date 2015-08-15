package at.brandl.finance.common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;

public class RewindableFileReader implements RewindableReader {

	private static final long serialVersionUID = 4004685227494044716L;
	private transient BufferedReader reader;
	private final String filename;

	public RewindableFileReader(String filename) throws FileNotFoundException {
		this.filename = filename;
		this.reader = new BufferedReader(new FileReader(filename));
	}

	public void rewind() throws IOException {
		reader.close();
		reader = new BufferedReader(new FileReader(filename));
	}
	
	public int hashCode() {
		return reader.hashCode();
	}

	public int read(CharBuffer target) throws IOException {
		return reader.read(target);
	}

	public int read() throws IOException {
		return reader.read();
	}

	public int read(char[] cbuf) throws IOException {
		return reader.read(cbuf);
	}

	public boolean equals(Object obj) {
		return reader.equals(obj);
	}

	public int read(char[] cbuf, int off, int len) throws IOException {
		return reader.read(cbuf, off, len);
	}

	public long skip(long n) throws IOException {
		return reader.skip(n);
	}

	public boolean ready() throws IOException {
		return reader.ready();
	}

	public boolean markSupported() {
		return reader.markSupported();
	}

	public void mark(int readAheadLimit) throws IOException {
		reader.mark(readAheadLimit);
	}

	public void reset() throws IOException {
		reader.reset();
	}

	public void close() throws IOException {
		reader.close();
	}

	public String toString() {
		return reader.toString();
	}

	@Override
	public String readLine() throws IOException {
		return reader.readLine();
	}
	
	 private void readObject(java.io.ObjectInputStream in)
		     throws IOException, ClassNotFoundException {
		 in.defaultReadObject();
		 this.reader = new BufferedReader(new FileReader(filename));
	 }

}
