package neo4jsna.engine.algorithm;

import org.neo4j.graphdb.Result;

public interface CypherAlgorithm extends Algorithm {
	String getQuery();

	void collectResult(Result result);

}
