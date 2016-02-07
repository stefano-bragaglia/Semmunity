package neo4jsna.measures;

import neo4jsna.engine.algorithm.SingleNodeScanAlgorithm;
import neo4jsna.engine.algorithm.SingleRelationshipScanAlgorithm;
import org.neo4j.graphdb.*;

public class DirectedModularity implements SingleNodeScanAlgorithm, SingleRelationshipScanAlgorithm {
	protected final String attName = "community";
	protected double eii = 0.0, ai = 0.0;
	protected double divisor = 0.0;

	public DirectedModularity(GraphDatabaseService g) {
		try (Transaction tx = g.beginTx()) {
			for (Relationship r : g.getAllRelationships())
				divisor += 1.0;
			tx.success();
		}
	}

	@Override
	public void compute(Node n) {
		double degree = n.getDegree(Direction.INCOMING) * n.getDegree(Direction.OUTGOING);
		ai += degree;
	}

	@Override
	public void compute(Relationship r) {
		Node n1 = r.getStartNode();
		Node n2 = r.getEndNode();

		if (n1.getProperty(attName) == n2.getProperty(attName)) {
			double weight = r.hasProperty("weight") ? (double) r.getProperty("weight") : 1.0;
			eii += weight;
		}
	}

	@Override
	public String getName() {
		return "Directed Modularity";
	}

	@Override
	public Double getResult() {
		return (ai / Math.pow(divisor, 2)) - (eii / divisor);                // Directed
	}

}
