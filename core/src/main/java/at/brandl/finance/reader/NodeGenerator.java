package at.brandl.finance.reader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import at.brandl.finance.common.Line;

public class NodeGenerator {

	private String[] wordFeatures;
	private String[] labels;

	public String createNodeString(Line line, String[] wordFeatures) {

		Map<Integer, Double> nodes = createNodeEntries(line, wordFeatures);
		nodes.put(0, 0d);
		return createNodeString(nodes);
	}

	public List<String> createNodeStrings(Iterable<Line> lines) {

		Collection<Map<Integer, Double>> nodes = new ArrayList<>();

		wordFeatures = collectFeatures(lines);
		labels = collectLabels(lines);

		boolean writeLabels = labels.length > 0;

		for (Line line : lines) {

			Map<Integer, Double> features = createNodeEntries(line,
					wordFeatures);
			if (writeLabels) {
				String label = line.getLabel();
				int index = Arrays.binarySearch(labels, label);
				if (index >= 0) {
					features.put(0, Double.valueOf(index));
				}
			}

			nodes.add(features);
		}

		return write(nodes);

	}

	public String[] getLabels() {

		return labels;
	}

	public String[] getWordFeatures() {

		return wordFeatures;
	}

	Map<Integer, Double> createNodeEntries(Line line, String[] wordFeatures) {
		int featureNo = 1;
		Map<Integer, Double> features = new TreeMap<>();

		// String value = new BufferedReader(new
		// InputStreamReader(System.in)).readLine();
//		features.put(featureNo++, Double.valueOf(line.getDay()));
//		features.put(featureNo++, Double.valueOf(line.getMonth()));
//		features.put(featureNo++, Double.valueOf(line.getWeekDay()));
//		features.put(featureNo++,
//				Double.valueOf(line.getAmount().doubleValue()));
		features.put(featureNo++, line.isExpense() ? 1d : 0d);
		int magnitude = line.getMagnitude();
		for(int i = 0; i < 12; i++) {
			features.put(featureNo++, i == magnitude ? 1d : 0d);
		}
		for (String word : line.getWords()) {
			int pos = Arrays.binarySearch(wordFeatures, word);
			if (pos >= 0) {
				features.put(pos + featureNo, 1d);
			}
		}
		return features;
	}

	static String createNodeString(Map<Integer, Double> features) {
		StringBuilder builder = new StringBuilder();
		for (Entry<Integer, Double> feature : features.entrySet()) {

			Integer key = feature.getKey();
			if (key.intValue() != 0) {
				builder.append(key.toString());
				builder.append(":");
			}
			builder.append(feature.getValue().toString());
			builder.append(" ");
		}
		return builder.toString();
	}

	private List<String> write(Collection<Map<Integer, Double>> lines) {

		List<String> strings = new ArrayList<>();
		for (Map<Integer, Double> features : lines) {

			String nodeStr = createNodeString(features);

			strings.add(nodeStr + System.lineSeparator());

		}
		return strings;
	}

	private String[] collectFeatures(Iterable<Line> lines) {

		Map<String, Integer> features = new TreeMap<String, Integer>();
		for (Line line : lines) {
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

		Set<String> featureSet = features.keySet();
		return featureSet.toArray(new String[featureSet.size()]);
	}

	private String[] collectLabels(Iterable<Line> lines) {

		Set<String> labelSet = new TreeSet<String>();
		for (Line line : lines) {
			String label = line.getLabel();
			if (label != null) {
				labelSet.add(label);
			}
		}
		return labelSet.toArray(new String[labelSet.size()]);
	}

}
