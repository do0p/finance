package at.brandl.finance.reader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import at.brandl.finance.common.Line;

public class TestFileGenerator {

	private final NodeGenerator nodeGenerator = new NodeGenerator();
	private final OutputStream outExpenses;
	private final OutputStream outIncome;

	public TestFileGenerator(OutputStream outExpenses, OutputStream outIncome) {
		this.outExpenses = outExpenses;
		this.outIncome = outIncome;
	}

	public void generate(FinanceDataReader reader) throws IOException,
			InterruptedException {

		Collection<Line> expenses = new ArrayList<>();
		Collection<Line> income = new ArrayList<>();

		Iterator<Line> lines = reader.getLines();

		while (lines.hasNext()) {
			Line line = lines.next();
			if (line.getAmount().doubleValue() > 0) {
				income.add(line);
			} else {
				expenses.add(line);
			}
		}

		BufferedWriter expensesWriter = new BufferedWriter(
				new OutputStreamWriter(outExpenses));
		write(nodeGenerator.createNodeStrings(expenses), expensesWriter);
		expensesWriter.flush();

		BufferedWriter incomeWriter = new BufferedWriter(
				new OutputStreamWriter(outIncome));
		write(nodeGenerator.createNodeStrings(expenses), incomeWriter);
		incomeWriter.flush();
	}

	private void write(Iterable<String> lines, BufferedWriter writer) {
		try {

			for (String line : lines) {

				writer.write(line);
			}
		} catch (IOException e) {
			new RuntimeException(e);
		}
	}

}
