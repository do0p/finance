package at.brandl.finance.core;

import static at.brandl.finance.utils.TestProperties.getTestFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import at.brandl.finance.common.Data;
import at.brandl.finance.common.Node;
import at.brandl.finance.common.NodeSet;
import at.brandl.finance.common.RewindableFileReader;
import at.brandl.finance.common.RewindableReader;
import at.brandl.finance.common.RewindableStringReader;

public class ScaleTest {

	private static final String DATA_FILENAME = getTestFile("test2.txt");
	private static final String SAVE_FILENAME = DATA_FILENAME + ".scale";



	@Test
	public void scaleReal() throws IOException {
		Data data = new Scale(new RewindableFileReader(DATA_FILENAME),
				new BufferedWriter(new FileWriter(SAVE_FILENAME)), null, -5, 5).scale();
		List<NodeSet> nodeSets = data.getNodeSets();
		List<Double> labels = data.getLabels();
		Assert.assertEquals(nodeSets.size(), labels.size());
		int numNodes = nodeSets.get(0).getNodes().size();
		for (int i = 0; i < nodeSets.size(); i++) {
			NodeSet nodeSet = nodeSets.get(i);
			List<Node> nodes = nodeSet.getNodes();
			Assert.assertEquals("error in line " + i, numNodes, nodes.size());
			for (Node node : nodes) {
				Assert.assertTrue(node.getIndex() > 0);
			}
		}
	}

	@Test
	public void scale() throws IOException {

		RewindableReader dataFile1 = createDataFile1();

		StringWriter saveStringWriter = new StringWriter();
		BufferedWriter saveFile = new BufferedWriter(saveStringWriter);
		Data data1 = new Scale(dataFile1, saveFile, null, -1, 1).scale();

		Assert.assertEquals(3, data1.getSize());
		Assert.assertEquals(Arrays.asList(1.0, 1.0, 2.0), data1.getLabels());
		List<NodeSet> nodeSets = data1.getNodeSets();
		Assert.assertEquals(3, nodeSets.size());

		// line 1
		List<Node> nodes1 = nodeSets.get(0).getNodes();
		Assert.assertEquals(6, nodes1.size());
		assertNode(1, 1.0, nodes1.get(0));
		assertNode(2, -1.0, nodes1.get(1));
		assertNode(5, 1.0, nodes1.get(2));
		assertNode(6, -1.0, nodes1.get(3));
		assertNode(7, -1.0, nodes1.get(4));
		assertNode(10, 1.0, nodes1.get(5));

		// line 2
		List<Node> nodes2 = nodeSets.get(1).getNodes();
		Assert.assertEquals(6, nodes2.size());
		assertNode(1, -1.0, nodes2.get(0));
		assertNode(2, 1.0, nodes2.get(1));
		assertNode(5, -1.0, nodes2.get(2));
		assertNode(6, 1.0, nodes2.get(3));
		assertNode(7, -1.0, nodes2.get(4));
		assertNode(10, -1.0, nodes2.get(5));

		// line 3
		List<Node> nodes3 = nodeSets.get(2).getNodes();
		Assert.assertEquals(6, nodes3.size());
		assertNode(1, 0.8068669527896997, nodes3.get(0));
		assertNode(2, -0.5730337078651686, nodes3.get(1));
		assertNode(5, -1.0, nodes3.get(2));
		assertNode(6, -1.0, nodes3.get(3));
		assertNode(7, 1.0, nodes3.get(4));
		assertNode(10, -1.0, nodes3.get(5));

		// saveFile
		BufferedReader saveFileReader = new BufferedReader(new StringReader(
				saveStringWriter.toString()));
		RewindableStringReader restoreFile = new RewindableStringReader();
		Assert.assertEquals("x", readAndCopy(saveFileReader, restoreFile));
		Assert.assertEquals("-1.000000000000000 1.000000000000000",
				readAndCopy(saveFileReader, restoreFile));
		Assert.assertEquals("1 -50.10000000000000 -3.500000000000000",
				readAndCopy(saveFileReader, restoreFile));
		Assert.assertEquals("2 0.02000000000000000 1.800000000000000",
				readAndCopy(saveFileReader, restoreFile));
		Assert.assertEquals("5 0.000000000000000 1.000000000000000",
				readAndCopy(saveFileReader, restoreFile));
		Assert.assertEquals("6 0.000000000000000 1.000000000000000",
				readAndCopy(saveFileReader, restoreFile));
		Assert.assertEquals("7 0.000000000000000 1.000000000000000",
				readAndCopy(saveFileReader, restoreFile));
		Assert.assertEquals("10 0.000000000000000 1.000000000000000",
				readAndCopy(saveFileReader, restoreFile));

		// restore
		RewindableReader dataFile2 = createDataFile2();
		Data data2 = new Scale(dataFile2, null, restoreFile, 0, 2).scale();

		Assert.assertEquals(2, data2.getSize());
		Assert.assertEquals(Arrays.asList(1.0, 5.0), data2.getLabels());
		List<NodeSet> nodeSets2 = data2.getNodeSets();
		Assert.assertEquals(2, nodeSets2.size());

		List<Node> nodes21 = nodeSets2.get(0).getNodes();
		Assert.assertEquals(6, nodes21.size());
		assertNode(1, 1.0214592274678114, nodes21.get(0));
		assertNode(2, -0.4606741573033708, nodes21.get(1));
		assertNode(5, 1.0, nodes21.get(2));
		assertNode(6, -1.0, nodes21.get(3));
		assertNode(7, -1.0, nodes21.get(4));
		assertNode(10, -1.0, nodes21.get(5));

		List<Node> nodes22 = nodeSets2.get(1).getNodes();
		Assert.assertEquals(6, nodes22.size());
		assertNode(1, -1.2103004291845494, nodes22.get(0));
		assertNode(2, -0.4606741573033708, nodes22.get(1));
		assertNode(5, 1.0, nodes22.get(2));
		assertNode(6, -1.0, nodes22.get(3));
		assertNode(7, -1.0, nodes22.get(4));
		assertNode(10, -1.0, nodes22.get(5));
	}

	private String readAndCopy(BufferedReader saveFileReader,
			RewindableStringReader restoreFile) throws IOException {

		String line = saveFileReader.readLine();
		restoreFile.addLine(line);
		return line;
	}

	private void assertNode(int index, double value, Node node) {

		Assert.assertEquals(index, node.getIndex());
		Assert.assertEquals(value, node.getValue(), 0);
	}

	private RewindableReader createDataFile1() {

		RewindableStringReader reader = new RewindableStringReader();
		reader.addLine("1 1:-3.5 2:0.02 3:0 5:1 10:1");
		reader.addLine("1 1:-50.1 2:1.8 3:0 5:0 6:1");
		reader.addLine("2 1:-8 2:0.4 3:0 5:0 7:1");
		return reader;
	}

	private RewindableReader createDataFile2() {

		RewindableStringReader reader = new RewindableStringReader();
		reader.addLine("1 1:-3 2:0.5 3:0 5:1 8:1 10:0 11:1");
		reader.addLine("5 1:-55 2:0.5 3:0 5:1 8:0 10:0 11:0");
		return reader;
	}
}
