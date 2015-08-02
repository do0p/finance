package at.brandl.finance.reader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class TestFileGenerator {

	private final OutputStream outExpenses;
	private final OutputStream outIncome;

	public TestFileGenerator(OutputStream outExpenses, OutputStream outIncome) {
		this.outExpenses = outExpenses;
		this.outIncome = outIncome;
	}

	public void generate(FinanceDataReader reader) throws IOException,
			InterruptedException {

		Collection<Map<Integer, Double>> expenses = new ArrayList<>();
		Collection<Map<Integer, Double>> income = new ArrayList<>();

		String[] wordFeatures = collectFeatures(reader);
		String[] labels = collectLabels(reader);

		boolean writeLabels = labels.length > 0;
		Iterator<Line> lines = reader.getLines();
		while (lines.hasNext()) {
			int featureNo = 0;
			Map<Integer, Double> features = new TreeMap<>();
			Line line = lines.next();

			// String value = new BufferedReader(new
			// InputStreamReader(System.in)).readLine();
			if(writeLabels) {
				String label = line.getLabel();
				features.put(featureNo, Double.valueOf(Arrays.binarySearch(labels, label)));
			} 
			featureNo++;
			features.put(featureNo++, Double.valueOf(line.getDay()));
			features.put(featureNo++, Double.valueOf(line.getMonth()));
			features.put(featureNo++, Double.valueOf(line.getWeekDay()));
			features.put(featureNo++,
					Double.valueOf(line.getAmount().doubleValue()));
			for (String word : line.getWords()) {
				int pos = Arrays.binarySearch(wordFeatures, word);
				features.put(pos + featureNo, 1d);
			}
			if (line.getAmount().doubleValue() > 0) {
				income.add(features);
			} else {
				expenses.add(features);
			}
		}

		BufferedWriter expensesWriter = new BufferedWriter(
				new OutputStreamWriter(outExpenses));
		write(expenses, expensesWriter);
		expensesWriter.flush();

		BufferedWriter incomeWriter = new BufferedWriter(
				new OutputStreamWriter(outIncome));
		write(income, incomeWriter);
		incomeWriter.flush();
	}

	private void write(Collection<Map<Integer, Double>> lines,
			BufferedWriter writer) {

		for (Map<Integer, Double> features : lines) {

			try {
				for (Entry<Integer, Double> feature : features.entrySet()) {

					Integer key = feature.getKey();
					if (key.intValue() != 0) {
						writer.write(key.toString());
						writer.write(":");
					}
					writer.write(feature.getValue().toString());
					writer.write(" ");
				}

				writer.write(System.lineSeparator());
			} catch (IOException e) {
				new RuntimeException(e);
			}
		}
	}

	private String[] collectFeatures(FinanceDataReader reader) {

		Map<String, Integer> features = new HashMap<String, Integer>();
		Iterator<Line> lines = reader.getLines();
		while (lines.hasNext()) {
			Line line = lines.next();
			for (String word : line.getWords()) {
				Integer count = features.get(word);
				if (count == null) {
					count = 1;
				} else {
					count = count.intValue() + 1;
				}
				features.put(word, count);
			}
		}

		// Map<Integer, String> sorted = new TreeMap<Integer, String>();
		// for(Entry<String, Integer> entry : features.entrySet()) {
		// sorted.put(entry.getValue(), entry.getKey());
		// }

		Set<String> featureSet = new TreeSet<String>(features.keySet());
		return featureSet.toArray(new String[featureSet.size()]);
	}

	private String[] collectLabels(FinanceDataReader reader) {

		Set<String> labelSet = new TreeSet<String>();
		Iterator<Line> lines = reader.getLines();
		while (lines.hasNext()) {
			Line line = lines.next();
			String label = line.getLabel();
			if (label != null) {
				labelSet.add(label);
			}
		}
		return labelSet.toArray(new String[labelSet.size()]);
	}

}
