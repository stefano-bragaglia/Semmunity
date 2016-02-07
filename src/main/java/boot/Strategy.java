package boot;

import org.neo4j.graphdb.GraphDatabaseService;

/**
 * TODO Add some meaningful class description...
 */
public interface Strategy {
	void execute(String content, GraphDatabaseService db);
}
