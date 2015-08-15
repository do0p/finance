package at.brandl.finance.common;

import java.io.IOException;
import java.io.Serializable;

public interface RewindableReader extends Serializable {

	void rewind() throws IOException;

	boolean ready() throws IOException;

	String readLine() throws IOException;

	void close() throws IOException;

	int read() throws IOException;
}
