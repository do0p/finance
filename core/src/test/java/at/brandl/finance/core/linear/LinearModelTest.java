package at.brandl.finance.core.linear;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.Assert;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.Before;
import org.junit.Test;

public class LinearModelTest {

	private LinearModel model;
	private Double[] labels;
	private RealMatrix theta;

	@Before
	public void setUp() {

		labels = createLabels();
		theta = createTheta();
		model = new LinearModel(labels, theta);
	}

	@Test
	public void serialize() throws IOException, ClassNotFoundException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ObjectOutputStream os = new ObjectOutputStream(out)) {

			os.writeObject(model);
		}

		LinearModel deserializedModel;
		try (ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()))) {
			
			deserializedModel = (LinearModel) is.readObject();
		}
		
		Assert.assertEquals(model, deserializedModel);
	}

	private RealMatrix createTheta() {

		double[][] data = new double[2][];
		data[0] = new double[3];
		data[1] = new double[3];

		data[0][0] = 1;
		data[0][1] = 2;
		data[0][2] = 3;
		data[1][0] = 4;
		data[1][1] = 5;
		data[1][2] = 6;

		return MatrixUtils.createRealMatrix(data);
	}

	private Double[] createLabels() {

		Double[] labels = new Double[2];
		labels[0] = 7d;
		labels[1] = 8d;
		return labels;
	}
}
