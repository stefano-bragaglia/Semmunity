package neo4jsna.algorithms.louvain;

import java.util.Collections;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * Created by besil on 7/24/15.
 */
public class LouvainResult {
	private Int2ObjectMap<LouvainLayer> layerMap;

	public LouvainResult() {
		this.layerMap = new Int2ObjectOpenHashMap<>();
	}

	public LouvainLayer layer(int level) {
		if (!layerMap.containsKey(level)) {
			this.layerMap.put(level, new LouvainLayer(level));
		}
		return this.layerMap.get(level);
	}

	public List<Integer> layers() {
		List<Integer> keys = new IntArrayList(layerMap.keySet());
		Collections.sort(keys);
		return keys;
	}

}
