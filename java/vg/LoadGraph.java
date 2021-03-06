package vg;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Graph loader
 * 
 * @author ksemer
 */
public class LoadGraph {
	// stores a map id -> author name
	private static Map<Integer, String> authorsNames;
	// stores a map id -> hash tag
	private static Map<Integer, String> hashtags;

	/**
	 * Create a labeled version graph in memory from a given DataSet nodeID \t
	 * nodeID \t time
	 * 
	 * @param iQ
	 * @throws IOException
	 */
	public static Graph loadDataset(BitSet iQ, String path) throws IOException {
		if (path.toLowerCase().contains("dblp")) {
			return loadDBLP(iQ, path);
		} else if (path.toLowerCase().contains("_days")) {
			return loadTimeReachData(path);
		} else {
			return loadOther(path);
		}
	}

	/**
	 * Load DBLP data
	 * 
	 * @param iQ
	 * @param path
	 * @return
	 * @throws IOException
	 */
	private static Graph loadDBLP(BitSet iQ, String path) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(path));
		String line = null;
		String[] edge;
		int n1_id, n2_id, time;

		Graph lvg = new Graph();

		while ((line = br.readLine()) != null) {
			edge = line.split("\t");

			// edge[2] has the year/time
			time = Integer.parseInt(edge[2]);

			if (time < 1959)
				continue;

			n1_id = Integer.parseInt(edge[0]);
			n2_id = Integer.parseInt(edge[1]);

			if (n1_id == n2_id)
				continue;

			time = convertTime(time);

			if (!iQ.get(time))
				continue;

			lvg.addNode(n1_id);
			lvg.addNode(n2_id);

			// src -> trg time label
			lvg.addEdge(n1_id, n2_id, time);
			// src -> trg time label
			lvg.addEdge(n2_id, n1_id, time);
		}
		br.close();

		// for DBLP10>2
		Iterator<Node> entries = lvg.getNodes().iterator();
		while (entries.hasNext()) {
			Node n = entries.next();

			for (Iterator<Entry<Node, BitSet>> e = n.getAdjacencyAsMap().entrySet().iterator(); e.hasNext();) {
				Entry<Node, BitSet> entry = e.next();
				if (entry.getValue().cardinality() < 3) {
					e.remove();
				}
			}

			if (n.getAdjacencyAsMap().size() == 0)
				entries.remove();
		}

		if (authorsNames == null)
			loadAuthors(path);

		return lvg;
	}

	/**
	 * Load DBLP authors names
	 * 
	 * @param path
	 * @throws IOException
	 */
	public static void loadAuthors(String path) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path + "_Authors"), "UTF-8"));
		String line = null;
		String[] token;
		authorsNames = new HashMap<>();

		while ((line = br.readLine()) != null) {
			token = line.split("\t");
			authorsNames.put(Integer.parseInt(token[0]), token[1]);
		}
		br.close();
	}

	/**
	 * Return name of author with the given id
	 * 
	 * @param id
	 * @return
	 */
	public static String getAuthor(int id) {
		return authorsNames.get(id);
	}

	/**
	 * Convert year to time instant
	 * 
	 * @param time
	 * @return
	 */
	private static int convertTime(int time) {
		return (time - 1959);
	}

	/**
	 * Load other dataset
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	private static Graph loadOther(String path) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(path));
		String line = null;
		String edge[];
		int n1_id, n2_id, t;

		Graph lvg = new Graph();

		while ((line = br.readLine()) != null) {
			edge = line.split("\t");

			n1_id = Integer.parseInt(edge[0]);
			n2_id = Integer.parseInt(edge[1]);

			lvg.addNode(n1_id);
			lvg.addNode(n2_id);
			t = Integer.parseInt(edge[2]);

			if (n1_id == n2_id)
				continue;

			// src -> trg time label
			lvg.addEdge(n1_id, n2_id, t);

			// src -> trg time label
			lvg.addEdge(n2_id, n1_id, t);
		}
		br.close();

		// load hashtags
		if (path.toLowerCase().contains("twitter"))
			loadHashTags(path);

		return lvg;
	}

	/**
	 * Load dataset from TimeReach paper
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	private static Graph loadTimeReachData(String path) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(path));
		String line = null;
		String edge[], interval[];
		int n1_id, n2_id;

		Graph lvg = new Graph();

		// ignore first line
		br.readLine();
		while ((line = br.readLine()) != null) {
			edge = line.split("\t");

			n1_id = Integer.parseInt(edge[0]);
			n2_id = Integer.parseInt(edge[1]);

			lvg.addNode(n1_id);
			lvg.addNode(n2_id);

			if (n1_id == n2_id)
				continue;

			interval = edge[2].split(",");

			for (int t = Integer.parseInt(interval[0]); t <= Integer.parseInt(interval[1]); t++) {
				// src -> trg time label
				lvg.addEdge(n1_id, n2_id, t);

				// src -> trg time label
				lvg.addEdge(n2_id, n1_id, t);
			}
		}
		br.close();
		return lvg;
	}

	/**
	 * Load Twitter Hashtags
	 * 
	 * @param path
	 * @throws IOException
	 */
	public static void loadHashTags(String path) throws IOException {
		BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(path + "_ids_hashtags"), "UTF-8"));
		String line = null;
		String[] token;
		hashtags = new HashMap<>();

		while ((line = br.readLine()) != null) {
			token = line.split("\t");
			hashtags.put(Integer.parseInt(token[0]), token[1]);
		}
		br.close();
	}

	/**
	 * Return HashTag name
	 * 
	 * @param id
	 * @return
	 */
	public static String getHashtag(int id) {
		return hashtags.get(id);
	}
}