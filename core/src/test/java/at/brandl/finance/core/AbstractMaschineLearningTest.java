package at.brandl.finance.core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import at.brandl.finance.common.Data;
import at.brandl.finance.common.NodeSet;
import at.brandl.finance.common.RewindableFileReader;

public abstract class AbstractMaschineLearningTest<T extends Model> {

	

	@Test
	public void learn() throws IOException {

		Data data = new Scale().scale(new RewindableFileReader(
				getDataFileName()), new BufferedWriter(new FileWriter(
				getSaveFileName())), null, getLower(), getUpper());
		T model = getCore().train(data);

		Data validation = new Scale().scale(new RewindableFileReader(
				getValidationFileName()), null, new RewindableFileReader(
				getSaveFileName()), getLower(), 5d);
		for (int i = 0; i < validation.getSize(); i++) {
			NodeSet input = validation.getNodeSets().get(i);
			double[] result = getCore().predict(model, input);
			System.out.println(validation.getLabels().get(i) + " = "
					+ result[0] + "(" + result[1] + ")");
			Assert.assertEquals(validation.getLabels().get(i), result[0], 0);
		}
	}

	protected abstract Core<T> getCore();

	protected abstract double getUpper();

	protected abstract double getLower();

	protected abstract String getValidationFileName();

	protected abstract String getSaveFileName();

	protected abstract String getDataFileName();

}