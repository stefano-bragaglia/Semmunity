package boot.strategies;

import java.util.*;
import java.util.Map.Entry;

import boot.Strategy;
import nlp.Parser;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO Add some meaningful class description...
 */
public class Semantic implements Strategy {
	private static final Logger logger = LoggerFactory.getLogger(Semantic.class);

	private static Entry<Long, Long> build(String content, GraphDatabaseService db) {
		content = Objects.requireNonNull(content).trim();
		Objects.requireNonNull(db);

		long elapsed = System.currentTimeMillis();
		long wordMax = 1;
		long linkMax = 1;
		long nouns = 0;
		long verbs = 0;
		try (Transaction tx = db.beginTx()) {
			for (List<List<String>> sentence : Parser.parse(content)) {
				Node past = null;
				for (List<String> tokens : sentence) {
					Label label = "NP".equals(tokens.get(0)) ? Labels.NOUN : Labels.VERB;
					for (int i = 1; i < tokens.size(); i++) {
						String text = tokens.get(i);
						Node node = db.findNode(label, "text", text);
						if (null == node) {
							node = db.createNode(Labels.WORD, label);
							node.setProperty("text", text);
							node.setProperty("occurrences", 1L);
							if ("NP".equals(tokens.get(0))) {
								nouns += 1;
							} else {
								verbs += 1;
							}
						} else {
							long occurrences = 1L + (long) node.getProperty("occurrences", 1L);
							wordMax = Long.max(wordMax, occurrences);
							node.setProperty("occurrences", occurrences);
						}
						if (null != past) {
							RelationshipType type = 1 == i ? RelTypes.LEADS_TO : RelTypes.PRECEDES;
							Relationship relationship = null;
							for (Relationship rel : past.getRelationships(type, Direction.OUTGOING)) {
								if (node.equals(rel.getOtherNode(past))) {
									relationship = rel;
									break;
								}
							}
							if (null == relationship) {
								relationship = past.createRelationshipTo(node, type);
								relationship.setProperty("occurrences", 1L);
							} else {
								long occurrences = 1L + (long) relationship.getProperty("occurrences", 1L);
								linkMax = Long.max(linkMax, occurrences);
								relationship.setProperty("occurrences", occurrences);
							}
						}
						past = node;
					}
				}
			}

			tx.success();
		}
		elapsed = System.currentTimeMillis() - elapsed;
		logger.info("Semantic graph with {} nouns and {} verbs successfully build in {} ms",
					nouns, verbs, String.format("%.3f", elapsed / 1_000_000_000.0));
		return new AbstractMap.SimpleImmutableEntry<>(wordMax, linkMax);
	}

	private static void filter(long wordThreshold, long linkThreshold, GraphDatabaseService db) {
		if (wordThreshold < 1) {
			throw new IllegalArgumentException("'wordThreshold' is less than 1: " + wordThreshold);
		}
		if (linkThreshold < 1) {
			throw new IllegalArgumentException("'linkThreshold' is less than 1: " + linkThreshold);
		}
		Objects.requireNonNull(db);

		long elapsed = System.currentTimeMillis();
		try (Transaction tx = db.beginTx()) {
			for (Node node : db.getAllNodes()) {
				if ((long) node.getProperty("occurrences", 1L) <= wordThreshold) {
					node.getRelationships().forEach(Relationship::delete);
					node.delete();
				} else {
					for (Relationship relationship : node.getRelationships(RelTypes.PRECEDES, RelTypes.LEADS_TO)) {
						if ((long) relationship.getProperty("occurrences", 1L) <= linkThreshold) {
							relationship.delete();
						}
					}
				}
			}
			tx.success();
		}
		elapsed = System.currentTimeMillis() - elapsed;
		logger.info("Semantic graph successfully filtered in {} ms", String.format("%.3f", elapsed / 1_000_000_000.0));
	}

	private static void reset(GraphDatabaseService db) {
		Objects.requireNonNull(db);

		long elapsed = System.currentTimeMillis();
		try (Transaction tx = db.beginTx()) {
			db.getAllRelationships().forEach(Relationship::delete);
			db.getAllNodes().forEach(Node::delete);
			tx.success();
		}
		try (Transaction tx = db.beginTx()) {
			db.schema().getIndexes().forEach(IndexDefinition::drop);
			db.schema().indexFor(Labels.NOUN).on("text").create();
			db.schema().indexFor(Labels.VERB).on("text").create();
		}
		elapsed = System.currentTimeMillis() - elapsed;
		logger.info("Semantic graph successfully reset in {} ms", String.format("%.3f", elapsed / 1_000_000_000.0));
	}

	@Override
	public void execute(String content, GraphDatabaseService db) {
		content = Objects.requireNonNull(content).trim();
		Objects.requireNonNull(db);

		reset(db);
		Entry<Long, Long> max = build(content, db);
		long wordThreshold = Long.max(1L, (long) Math.ceil(5.0 * max.getKey() / 100.0));
		long linkThreshold = Long.max(1L, (long) Math.ceil(5.0 * max.getValue() / 100.0));
		filter(wordThreshold, linkThreshold, db);
	}

	public static void mark(GraphDatabaseService db) {
		Objects.requireNonNull(db);

		long elapsed = System.currentTimeMillis();
		int seq = 0;
		Map<Long, Node> cores = new HashMap<>();
		try (Transaction tx = db.beginTx()) {
			for (Node node : db.getAllNodes()) {
				if (node.hasProperty("community")) {
					Long num = (Long) node.getProperty("community");
					Node core = cores.get(num);
					if (null == core) {
						int id = ++seq;
						core = db.createNode(Labels.CORE);
						core.setProperty("id", id);
						cores.put(num, core);
					}
					core.createRelationshipTo(node, RelTypes.CONTAINS);
				}
			}
			tx.success();
		}
		elapsed = System.currentTimeMillis() - elapsed;
		logger.info("All {} communities successfully marked in {} ms",
					seq, String.format("%.3f", elapsed / 1_000_000_000.0));
	}

	public enum Labels implements Label {
		CORE, NOUN, VERB, WORD;
	}

	public enum RelTypes implements RelationshipType {
		CONTAINS, LEADS_TO, PRECEDES;
	}

}
