package boot;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import boot.strategies.Semantic;
import neo4jsna.algorithms.LabelPropagation;
import neo4jsna.algorithms.louvain.Louvain;
import neo4jsna.engine.GraphAlgoEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TODO Add some meaningful class description...
 */
@SpringBootApplication(scanBasePackages = {"boot.configuration", "boot.service"})
public class Application implements CommandLineRunner {
	private static final Path PATH = Paths.get("article.txt");

	private static final String SILLY = "All the faith he had had had, had no effect on the outcome of his life.";

	private static final Strategy SEMANTIC = new Semantic();

	// MEMENTO Set to 'true' to use Louvain and expose an issue
	private static boolean FLAG = false;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		System.out.println("Done.");
	}

	@Override
	public void run(String... args) throws Exception {
		String content = String.join("\n", Files.readAllLines(PATH));
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(Paths.get("./graph.db").toFile());
		SEMANTIC.execute(content, db);
		if (FLAG) {
			Louvain louvain = new Louvain(db);
			louvain.execute();
		} else {
			GraphAlgoEngine engine = new GraphAlgoEngine(db);
			engine.execute(new LabelPropagation(RelTypes.PRECEDES));
		}
		Semantic.mark(db);
		db.shutdown();
	}

	public enum Labels implements Label {
		CORE, NOUN, VERB, WORD;
	}

	public enum RelTypes implements RelationshipType {
		CONTAINS, LEADS_TO, PRECEDES;
	}

}
