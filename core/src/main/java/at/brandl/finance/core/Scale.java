package at.brandl.finance.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

import at.brandl.finance.common.Data;
import at.brandl.finance.common.Node;
import at.brandl.finance.common.RewindableReader;

public class Scale {

	private int maxIndex;
	private double[] featureMin;
	private double[] featureMax;
	private RewindableReader fp;
	private RewindableReader fpRestore;
	private double lower;
	private double upper;
	private BufferedWriter fpSave;
	private Data data;

	public Data scale(RewindableReader dataFile, BufferedWriter saveFile,
			RewindableReader restoreFile, double pLower, double pUpper)
			throws IOException {

		if (pUpper <= pLower) {
			throw new IllegalArgumentException(
					"inconsistent lower/upper specification");
		}
		if (restoreFile != null && saveFile != null) {
			throw new IllegalArgumentException(
					"cannot use -r and -s simultaneously");
		}

		lower = pLower;
		upper = pUpper;
		fp = dataFile;
		fpRestore = restoreFile;
		fpSave = saveFile;
		data = new Data();

		findMaxIndex();
		findMinMax();
		writeToSave();
		scale();

		return data;
	}

	private void findMaxIndex() throws IOException {

		if (fpRestore != null) {

			fpRestore.readLine();
			fpRestore.readLine();

			while (fpRestore.ready()) {

				StringTokenizer tokenizer = new StringTokenizer(
						fpRestore.readLine());
				int idx = Integer.parseInt(tokenizer.nextToken());
				maxIndex = Math.max(maxIndex,
						idx);
			}
			fpRestore.rewind();
		}

		while (fp.ready()) {

			StringTokenizer tokenizer = new StringTokenizer(fp.readLine(),
					" \t\n\r\f:");
			tokenizer.nextToken();

			while (tokenizer.hasMoreTokens()) {

				maxIndex = Math.max(maxIndex,
						Integer.parseInt(tokenizer.nextToken()));
				tokenizer.nextToken();
			}
		}

		fp.rewind();
	}

	private void findMinMax() throws NumberFormatException, IOException {

		featureMax = new double[(maxIndex + 1)];
		featureMin = new double[(maxIndex + 1)];

		for (int i = 0; i <= maxIndex; i++) {
			featureMax[i] = -Double.MAX_VALUE;
			featureMin[i] = Double.MAX_VALUE;
		}

		while (fp.ready()) {

			int nextIndex = 1;

			StringTokenizer tokenizer = new StringTokenizer(fp.readLine(),
					" \t\n\r\f:");
			tokenizer.nextToken();

			while (tokenizer.hasMoreTokens()) {

				int index = Integer.parseInt(tokenizer.nextToken());
				double value = Double.parseDouble(tokenizer.nextToken());

				for (int i = nextIndex; i < index; i++) {

					featureMax[i] = Math.max(featureMax[i], 0);
					featureMin[i] = Math.min(featureMin[i], 0);
				}

				featureMax[index] = Math.max(featureMax[index], value);
				featureMin[index] = Math.min(featureMin[index], value);

				nextIndex = index + 1;
			}

			for (int i = nextIndex; i <= maxIndex; i++) {

				featureMax[i] = Math.max(featureMax[i], 0);
				featureMin[i] = Math.min(featureMin[i], 0);
			}
		}

		fp.rewind();

		if (fpRestore != null) {

			if (fpRestore.read() == 'x') {

				fpRestore.readLine(); // pass the '\n' after 'x'

				StringTokenizer tokenizer = new StringTokenizer(
						fpRestore.readLine());
				lower = Double.parseDouble(tokenizer.nextToken());
				upper = Double.parseDouble(tokenizer.nextToken());

				while (fpRestore.ready()) {

					tokenizer = new StringTokenizer(fpRestore.readLine());
					int idx = Integer.parseInt(tokenizer.nextToken());
					double fmin = Double.parseDouble(tokenizer.nextToken());
					double fmax = Double.parseDouble(tokenizer.nextToken());
					if (idx <= maxIndex) {
						featureMin[idx] = fmin;
						featureMax[idx] = fmax;
					}
				}
			}

			fpRestore.close();
		}
	}

	private void writeToSave() throws IOException {

		if (fpSave != null) {

			Formatter formatter = new Formatter(new StringBuilder(),
					Locale.ENGLISH);
			formatter.format("x\n");
			formatter.format("%.16g %.16g\n", lower, upper);

			for (int i = 1; i <= maxIndex; i++) {
				if (featureMin[i] != featureMax[i])
					formatter.format("%d %.16g %.16g\n", i, featureMin[i],
							featureMax[i]);
			}

			fpSave.write(formatter.toString());
			fpSave.close();
			formatter.close();
		}
	}

	private void scale() throws IOException {

		while (fp.ready()) {

			data.nextLine();
			int nextIndex = 1;
			StringTokenizer st = new StringTokenizer(fp.readLine(),
					" \t\n\r\f:");

			data.setLabel(Double.parseDouble(st.nextToken()));

			while (st.hasMoreElements()) {

				int index = Integer.parseInt(st.nextToken());
				double value = Double.parseDouble(st.nextToken());

				for (int i = nextIndex; i < index; i++) {
					output(i, 0, data);
				}

				output(index, value, data);

				nextIndex = index + 1;
			}

			for (int i = nextIndex; i <= maxIndex; i++) {
				output(i, 0, data);
			}
		}

		fp.close();
	}

	private void output(int index, double value, Data data) {

		double min = featureMin[index];
		double max = featureMax[index];

		if (max == min) {
			value = lower;
		} else if (value == min) {
			value = lower;
		} else if (value == max) {
			value = upper;
		} else {
			value = lower + (upper - lower) * (value - min) / (max - min);
		}

		data.addNode(new Node(index, value));
	}
}