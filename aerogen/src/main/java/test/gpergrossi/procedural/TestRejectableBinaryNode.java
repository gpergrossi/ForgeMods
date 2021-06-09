package test.gpergrossi.procedural;

import java.util.List;
import java.util.Optional;

import com.gpergrossi.procedural.DataflowExecutor;
import com.gpergrossi.procedural.DataflowMethod;
import com.gpergrossi.procedural.DataflowObject;
import com.gpergrossi.procedural.DataflowResult;
import com.gpergrossi.util.data.Lists;

public class TestRejectableBinaryNode extends DataflowObject<TestRejectableBinaryNode> {
	
	protected final DataflowResult product = new DataflowResult();
	protected double result;
	
	public TestRejectableBinaryNode(String formula, int n) {
		super();
		initialize();
	}
	
	protected final DataflowMethod doOperation = new DataflowMethod() {
		@Override
		public List<DataflowResult> getPrerequisites() {
			return LIST_NO_PREREQUISITES;
		}

		@Override
		public void doWork() {
			// TODO Auto-generated method stub
		}

		@Override
		public List<DataflowResult> getResults() {
			return Lists.of(product);
		}
	};
	
	public Optional<Double> getResult(DataflowExecutor executor) {
		boolean success = require(executor, product);
		if (!success) return Optional.empty();
		
		return Optional.of(result);
	}
	
}
