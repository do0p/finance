package at.brandl.finance.core;

import at.brandl.finance.common.Data;
import at.brandl.finance.common.NodeSet;

public interface Core<M extends Model> {

	M train(Data data);

	double[] predict(M model, NodeSet data);

}
