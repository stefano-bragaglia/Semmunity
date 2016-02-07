package neo4jsna.algorithms;

import java.util.Map;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import neo4jsna.engine.algorithm.CypherAlgorithm;
import org.neo4j.graphdb.Result;

public class TriangleCount implements CypherAlgorithm {
	protected Long2LongMap triangleMap;

	public TriangleCount() {
		this.triangleMap = new Long2LongOpenHashMap();
	}

	@Override
	public String getName() {
		return "Triangle Count";
	}

	@Override
	public String getQuery() {
		// MEMENTO what about 'MATCH p = (a)--(b)--(c)--(a)'? Any faster?
		return "MATCH p = (a) -- (b) -- (c), (c) -- (a) " +
				"RETURN id(a) as nodeid, count(p) as triangleCount";
	}

	@Override
	public void collectResult(Result result) {
		while (result.hasNext()) {
			Map<String, Object> row = result.next();
			long nodeid = (long) row.get("nodeid");
			long triangleCount = (long) row.get("triangleCount");
			this.triangleMap.put(nodeid, triangleCount);
		}
	}

	@Override
	public Long2LongMap getResult() {
		return this.triangleMap;
	}

}
