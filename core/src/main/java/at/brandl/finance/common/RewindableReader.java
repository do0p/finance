package at.brandl.finance.common;

import java.io.IOException;

public interface RewindableReader  {

	void rewind() throws IOException;

	boolean ready() throws IOException;

	String readLine() throws IOException;

	void close() throws IOException;

	int read() throws IOException;
}
