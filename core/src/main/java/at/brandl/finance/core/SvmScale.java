package at.brandl.finance.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Formatter;
import java.util.StringTokenizer;

import libsvm.svm_node;

public class SvmScale {

	private String line = null;

	private double[] featureMax;
	private double[] featureMin;
	private double yMax = -Double.MAX_VALUE;
	private double yMin = Double.MAX_VALUE;
	private int maxIndex;
	private long numNonzeros = 0;
	private long newNumNonzeros = 0;

	private static void exit_with_help() {
		System.out
				.print("Usage: svm-scale [options] data_filename\n"
						+ "options:\n"
						+ "-l lower : x scaling lower limit (default -1)\n"
						+ "-u upper : x scaling upper limit (default +1)\n"
						+ "-y yLower yUpper : y scaling limits (default: no y scaling)\n"
						+ "-s save_filename : save scaling parameters to save_filename\n"
						+ "-r restore_filename : restore scaling parameters from restore_filename\n");
		System.exit(1);
	}

	private BufferedReader rewind(BufferedReader fp, String filename)
			throws IOException {
		fp.close();
		return new BufferedReader(new FileReader(filename));
	}

	private void output_target(double value, boolean yScaling, double yLower,
			double yUpper, SvmData data) {
		if (yScaling) {
			if (value == yMin)
				value = yLower;
			else if (value == yMax)
				value = yUpper;
			else
				value = yLower + (yUpper - yLower) * (value - yMin)
						/ (yMax - yMin);
		}

		data.setLabel(value);
	}

	private void output(int index, double value, double lower, double upper, SvmData data) {
		/* skip single-valued attribute */
		if (featureMax[index] == featureMin[index])
			return;

		if (value == featureMin[index])
			value = lower;
		else if (value == featureMax[index])
			value = upper;
		else
			value = lower + (upper - lower) * (value - featureMin[index])
					/ (featureMax[index] - featureMin[index]);

		if (value != 0) {
			svm_node node = new svm_node();
			node.index = index;
			node.value = value;
			newNumNonzeros++;
			data.addNode(node);
		}
	}

	private String readline(BufferedReader fp) throws IOException {
		line = fp.readLine();
		return line;
	}

	private void run(String[] argv) throws IOException {
		int i;

		String saveFilename = null;
		String restoreFilename = null;
		String dataFilename = null;

		double lower = -1.0;
		double upper = 1.0;
		double yLower = 0.0;
		double yUpper = 0.0;
		boolean yScaling = false;

		for (i = 0; i < argv.length; i++) {
			if (argv[i].charAt(0) != '-')
				break;
			++i;
			switch (argv[i - 1].charAt(1)) {
			case 'l':
				lower = Double.parseDouble(argv[i]);
				break;
			case 'u':
				upper = Double.parseDouble(argv[i]);
				break;
			case 'y':
				yLower = Double.parseDouble(argv[i]);
				++i;
				yUpper = Double.parseDouble(argv[i]);
				yScaling = true;
				break;
			case 's':
				saveFilename = argv[i];
				break;
			case 'r':
				restoreFilename = argv[i];
				break;
			default:
				System.err.println("unknown option");
				exit_with_help();
			}
		}

		if (argv.length != i + 1)
			exit_with_help();

		dataFilename = argv[i];

		scale(dataFilename, saveFilename, restoreFilename, lower, upper,
				yLower, yUpper, yScaling);
	}

	public SvmData scale(String dataFilename, String saveFilename,
			String restoreFilename, double lower, double upper, double yLower,
			double yUpper, boolean yScaling) throws IOException {

		BufferedReader fp = null, fp_restore = null;
		int i, index;

		if (!(upper > lower) || (yScaling && !(yUpper > yLower))) {
			System.err.println("inconsistent lower/upper specification");
			System.exit(1);
		}
		if (restoreFilename != null && saveFilename != null) {
			System.err.println("cannot use -r and -s simultaneously");
			System.exit(1);
		}

		try {
			fp = new BufferedReader(new FileReader(dataFilename));
		} catch (Exception e) {
			System.err.println("can't open file " + dataFilename);
			System.exit(1);
		}

		/* assumption: min index of attributes is 1 */
		/* pass 1: find out max index of attributes */
		maxIndex = 0;

		if (restoreFilename != null) {
			int idx;

			try {
				fp_restore = new BufferedReader(new FileReader(restoreFilename));
			} catch (Exception e) {
				System.err.println("can't open file " + restoreFilename);
				System.exit(1);
			}
			if ((fp_restore.read()) == 'y') {
				fp_restore.readLine();
				fp_restore.readLine();
				fp_restore.readLine();
			}
			fp_restore.readLine();
			fp_restore.readLine();

			String restore_line = null;
			while ((restore_line = fp_restore.readLine()) != null) {
				StringTokenizer st2 = new StringTokenizer(restore_line);
				idx = Integer.parseInt(st2.nextToken());
				maxIndex = Math.max(maxIndex, idx);
			}
			fp_restore = rewind(fp_restore, restoreFilename);
		}

		int numLines = 0;
		while (readline(fp) != null) {
			numLines++;
			StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
			st.nextToken();
			while (st.hasMoreTokens()) {
				index = Integer.parseInt(st.nextToken());
				maxIndex = Math.max(maxIndex, index);
				st.nextToken();
				numNonzeros++;
			}
		}

		try {
			featureMax = new double[(maxIndex + 1)];
			featureMin = new double[(maxIndex + 1)];
		} catch (OutOfMemoryError e) {
			System.err.println("can't allocate enough memory");
			System.exit(1);
		}

		for (i = 0; i <= maxIndex; i++) {
			featureMax[i] = -Double.MAX_VALUE;
			featureMin[i] = Double.MAX_VALUE;
		}

		fp = rewind(fp, dataFilename);

		/* pass 2: find out min/max value */
		while (readline(fp) != null) {
			int next_index = 1;
			double target;
			double value;

			StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
			target = Double.parseDouble(st.nextToken());
			yMax = Math.max(yMax, target);
			yMin = Math.min(yMin, target);

			while (st.hasMoreTokens()) {
				index = Integer.parseInt(st.nextToken());
				value = Double.parseDouble(st.nextToken());

				for (i = next_index; i < index; i++) {
					featureMax[i] = Math.max(featureMax[i], 0);
					featureMin[i] = Math.min(featureMin[i], 0);
				}

				featureMax[index] = Math.max(featureMax[index], value);
				featureMin[index] = Math.min(featureMin[index], value);
				next_index = index + 1;
			}

			for (i = next_index; i <= maxIndex; i++) {
				featureMax[i] = Math.max(featureMax[i], 0);
				featureMin[i] = Math.min(featureMin[i], 0);
			}
		}

		fp = rewind(fp, dataFilename);

		/* pass 2.5: save/restore featureMin/featureMax */
		if (restoreFilename != null) {
			// fp_restore rewinded in finding maxIndex
			int idx;
			double fmin, fmax;

			fp_restore.mark(2); // for reset
			if ((fp_restore.read()) == 'y') {
				fp_restore.readLine(); // pass the '\n' after 'y'
				StringTokenizer st = new StringTokenizer(fp_restore.readLine());
				yLower = Double.parseDouble(st.nextToken());
				yUpper = Double.parseDouble(st.nextToken());
				st = new StringTokenizer(fp_restore.readLine());
				yMin = Double.parseDouble(st.nextToken());
				yMax = Double.parseDouble(st.nextToken());
				yScaling = true;
			} else
				fp_restore.reset();

			if (fp_restore.read() == 'x') {
				fp_restore.readLine(); // pass the '\n' after 'x'
				StringTokenizer st = new StringTokenizer(fp_restore.readLine());
				lower = Double.parseDouble(st.nextToken());
				upper = Double.parseDouble(st.nextToken());
				String restore_line = null;
				while ((restore_line = fp_restore.readLine()) != null) {
					StringTokenizer st2 = new StringTokenizer(restore_line);
					idx = Integer.parseInt(st2.nextToken());
					fmin = Double.parseDouble(st2.nextToken());
					fmax = Double.parseDouble(st2.nextToken());
					if (idx <= maxIndex) {
						featureMin[idx] = fmin;
						featureMax[idx] = fmax;
					}
				}
			}
			fp_restore.close();
		}

		if (saveFilename != null) {
			Formatter formatter = new Formatter(new StringBuilder());
			BufferedWriter fp_save = null;

			try {
				fp_save = new BufferedWriter(new FileWriter(saveFilename));
			} catch (IOException e) {
				System.err.println("can't open file " + saveFilename);
				System.exit(1);
			}

			if (yScaling) {
				formatter.format("y\n");
				formatter.format("%.16g %.16g\n", yLower, yUpper);
				formatter.format("%.16g %.16g\n", yMin, yMax);
			}
			formatter.format("x\n");
			formatter.format("%.16g %.16g\n", lower, upper);
			for (i = 1; i <= maxIndex; i++) {
				if (featureMin[i] != featureMax[i])
					formatter.format("%d %.16g %.16g\n", i, featureMin[i],
							featureMax[i]);
			}
			fp_save.write(formatter.toString());
			fp_save.close();
			formatter.close();
		}

		/* pass 3: scale */
		SvmData data = new SvmData(numLines, maxIndex);
		int lineNo = 0;
		while (readline(fp) != null) {
			data.setLine(lineNo++);
			int next_index = 1;
			double target;
			double value;

			StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
			target = Double.parseDouble(st.nextToken());
			output_target(target, yScaling, yLower, yUpper, data);
			while (st.hasMoreElements()) {
				index = Integer.parseInt(st.nextToken());
				value = Double.parseDouble(st.nextToken());
				for (i = next_index; i < index; i++) {
					output(i, 0, lower, upper, data);
				}
				output(index, value, lower, upper, data);
				next_index = index + 1;
			}

			for (i = next_index; i <= maxIndex; i++){
				output(i, 0, lower, upper, data);
			}
		}
		if (newNumNonzeros > numNonzeros)
			System.err.print("WARNING: original #nonzeros " + numNonzeros
					+ "\n" + "         new      #nonzeros " + newNumNonzeros
					+ "\n"
					+ "Use -l 0 if many original feature values are zeros\n");

		fp.close();
		return data;
	}

	public static void main(String argv[]) throws IOException {
		SvmScale s = new SvmScale();
		s.run(argv);
	}
}
