package at.brandl.finance.common;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

public class RewindableStringReader implements RewindableReader {

	private final List<String> lines = new ArrayList<String>();
	private int lineNo = 0;
	
	@Override
	public int read(CharBuffer cb) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void rewind() throws IOException {
		lineNo = 0;
	}

	@Override
	public boolean ready() throws IOException {
		return lineNo < lines.size();
	}

	@Override
	public String readLine() throws IOException {
		return lines.get(lineNo++);
	}

	@Override
	public void close() throws IOException {
		lineNo = 0;
		lines.clear();
	}

	@Override
	public int read() throws IOException {
		return lines.get(lineNo).charAt(0);
	}

	public void addLine(String line) {
		lines.add(line);
	}
	
	
}
