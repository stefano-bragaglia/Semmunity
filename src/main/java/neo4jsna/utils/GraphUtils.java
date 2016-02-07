package neo4jsna.utils;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import neo4jsna.algorithms.Demon;
import org.neo4j.graphdb.*;

/**
 * Created by besil on 01/06/15.
 */
public class GraphUtils {
	private static final Logger log = Logger.getLogger(GraphUtils.class.getName());

	public static Iterator<Relationship> getRelationshisByNodeAndRelationshipType(
			GraphDatabaseService g, Label label, Demon.DemonRelType relType) {
		List<Relationship> rels = new LinkedList<>();
		ResourceIterator<Node> nodes = g.findNodes(label);
		while (nodes.hasNext()) {
			Node node = nodes.next();
			for (Relationship rel : node.getRelationships(Direction.OUTGOING, relType))
				rels.add(rel);
		}
		return rels.iterator();
	}

	public static void print(GraphDatabaseService db) {
		log.info("******************************");
		for (Node n : db.getAllNodes()) {
			StringBuilder sb = new StringBuilder(n.getId() + " " + n.getLabels() + " ");
			List<String> properties = new LinkedList<>();
			n.getPropertyKeys().forEach(properties::add);
			Collections.sort(properties);

			for (String pk : properties) {
				sb.append(pk).append(":").append(n.getProperty(pk)).append(" ");
			}
			log.info(sb.toString());
		}
		log.info("---------------------------");
		for (Relationship r : db.getAllRelationships()) {
			StringBuilder sb = new StringBuilder(r.getStartNode().getId() + " -[:" + r.getType() + ", ");
			List<String> properties = new LinkedList<>();
			r.getPropertyKeys().forEach(properties::add);
			Collections.sort(properties);

			for (String pk : properties) {
				sb.append(pk).append(":").append(r.getProperty(pk)).append(" ");
			}

			sb.append("] -> ").append(r.getEndNode().getId());
			log.info(sb.toString());
		}
		log.info("******************************");
	}

//    public static void print(GraphDatabaseService neo) {
//
//        log.info("---------------");
//        log.info("Node count: " + GraphUtils.getNodeCount(neo));
//        log.info("Rel count: " + GraphUtils.getEdgeCount(neo));
//
//        GlobalGraphOperations.at(neo).getAllNodes().forEach(node -> {
//            StringBuilder sb = new StringBuilder();
//            sb.append(node).append(" ").append(node.getLabels()).append("(");
//            for (String key : node.getPropertyKeys())
//                sb.append(key).append(":").append(node.getProperty(key)).append(" ");
//            sb.append(")");
//            log.info(sb.toString());
//        });
//        log.info("***************");
//        GlobalGraphOperations.at(neo).getAllRelationships().forEach(rel -> {
//            StringBuilder sb = new StringBuilder();
//            sb.append(rel.getId()).append(": ");
//            sb.append("(").append(rel.getStartNode()).append("-").append(rel.getEndNode()).append(") ");
//            sb.append(rel.getType().name()).append(" ");
//            for (String key : rel.getPropertyKeys())
//                sb.append(key).append(":").append(rel.getProperty(key)).append(" ");
//            log.info(sb.toString());
//        });
//        log.info("---------------");
//    }

	public static long getNodeCount(GraphDatabaseService db) {
		long count = 0;
		for (Node node : db.getAllNodes())
			count++;
		return count;
	}

	public static long getEdgeCount(GraphDatabaseService db) {
		long count = 0;
		for (Relationship relationship : db.getAllRelationships())
			count++;
		return count;
	}
}
