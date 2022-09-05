package denforest;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Collections;
import java.util.Comparator;

import denforest.basicRtree.Element;
import denforest.basicRtree.BasicRtree;

import java.util.HashMap;

import denforest.datapoint.DataPoint;
import denforest.datapoint.Timestamp;
import denforest.linkcuttree.*;

public class DenForest {
	Random rand = new Random();

	LinkedList<DataPoint> dataset; // Dataset

	BasicRtree<DataPoint> mrtree;
	HashMap<DataPoint, HashSet<DataPoint>> edgeTable;

	LinkCutTree lct;

	int dim; // Dimension
	double eps; // epsilon
	int minPts; // min points

	int window_size;

	int UNCLASSIFIED = 0;
	int NOISE = -1;
	int clusterID = 1;
	Timestamp time;
	HashMap<Integer, Integer> labels;

	HashMap<Long, HashSet<DataPoint>> expiredTable;

	/**
	 * @param all
	 * @param window
	 * @param stride
	 * @param Time
	 */
	public void label(List<DataPoint> all, int window, int stride, int Time) {
		for (DataPoint p : all.subList((Time) * stride, window + (Time)
				* stride)) {
			// core but not labeled
			if (p.core_expired >= time.getTime() && p.label == -1) {
				lct.Label(p.node, p.id, labels);
			}
			// core and already labeled
			else if (p.core_expired >= time.getTime() && p.label != -1) {
				// skip
			}
			// Border
			if (p.core_expired < time.getTime() && p.label != -1) {
				if (all.get(p.label).core_expired < time.getTime()) {
					p.label = -1;
				}
			}
		}
	}

	/**
	 * @param window
	 * @return
	 */
	public int[] labelAndReturn(List<DataPoint> window) {

		for (DataPoint p : window) {
			// core but not labeled
			if (p.core_expired >= time.getTime() && p.label == -1) {
				lct.Label(p.node, p.id, labels);
			}
			// core and already labeled
			else if (p.core_expired >= time.getTime() && p.label != -1) {
				// Skip
			}
		}

		// Border
		for (DataPoint p : window) {
			if (p.core_expired < time.getTime() && p.parentIfborder != null
					&& p.parentIfborder.core_expired >= time.getTime()) {
				p.label = p.parentIfborder.label;
			}

		}

		int res[] = new int[window.size()];

		int jj = 0;
		for (DataPoint p : window) {
			if (p.label == -1)
				res[jj] = -1;
			else
				res[jj] = labels.get(p.label);
			jj++;

		}
		return res;

	}

	public HashMap<Integer, Set<Integer>> labelAndCollect(List<DataPoint> all,
			int window, int stride, int Time) {
		label(all, window, stride, Time);
		HashMap<Integer, Set<Integer>> clusters = new HashMap<Integer, Set<Integer>>();
		for (DataPoint p : all.subList((Time) * stride, window + (Time)
				* stride)) {
			if (p.label == -1) {
				Set<Integer> S = clusters.getOrDefault(-1,
						new HashSet<Integer>());
				S.add(p.id);
				clusters.put(-1, S);
			} else {
				Set<Integer> S = clusters.getOrDefault(labels.get(p.label),
						new HashSet<Integer>());
				S.add(p.id);
				clusters.put(labels.get(p.label), S);
			}
		}
		return clusters;
	}

	/**
	 * @param dim
	 * @param eps
	 * @param minPts
	 * @param time
	 */
	public DenForest(int dim, double eps, int minPts, Timestamp time) {
		this.dim = dim;
		this.eps = eps;
		this.minPts = minPts;
		lct = new LinkCutTree();
		mrtree = new BasicRtree<DataPoint>(dim, 50, 20);
		expiredTable = new HashMap<Long, HashSet<DataPoint>>();
		edgeTable = new HashMap<DataPoint, HashSet<DataPoint>>();
		labels = new HashMap<Integer, Integer>();
		this.time = time;
	}

	

	/**
	 * @param p1
	 * @param p2
	 * @param dim
	 * @return
	 */
	public static double l2distance(double[] p1, double[] p2, int dim) {
		double dist = 0;
		for (int d = 0; d < dim; d++) {
			dist += (p1[d] - p2[d]) * (p1[d] - p2[d]);

		}

		return dist;
	}

	/**
	 * @param p
	 * @return
	 */
	Deque<DataPoint> findNeighbors_wIndex(DataPoint p) {

		double[] ls = new double[dim];
		double[] us = new double[dim];

		for (int d = 0; d < dim; d++) {
			ls[d] = p.values[d] - eps;

		}
		for (int d = 0; d < dim; d++) {
			us[d] = p.values[d] + eps;
		}

		List<Element<DataPoint>> le = mrtree.intersects(us, ls);

		Deque<DataPoint> neighbors = new ArrayDeque<DataPoint>();
		for (Element<DataPoint> e : le) {
			DataPoint pp = e.p;

			if (l2distance(p.values, pp.values, dim) <= eps * eps) {
				neighbors.push(pp);
			}

		}
		return neighbors;
	}

	/**
	 * @param neighbors_list
	 */
	public void sort(List<DataPoint> neighbors_list) {
		Collections.sort(neighbors_list, new Comparator<DataPoint>() {
			public int compare(DataPoint p1, DataPoint p2) {
				return Long.valueOf(p2.timestamp).compareTo(
						Long.valueOf(p1.timestamp));
			}
		});
	}

	/**
	 * @param p
	 */
	public void onlyInsert(DataPoint p) {

		Deque<DataPoint> neighbors = findNeighbors_wIndex(p);

		if (neighbors.size() >= minPts) {

			LCNode newNode = new LCNode();
			p.node = newNode;
			p.node.dp = p;

			p.core_expired = Integer.MAX_VALUE - 1;

			for (DataPoint n : neighbors) {
				if (n.core_expired >= time.getTime())// coreflag
				{
					simple_connect(p.node, n.node);
					if (p.parentIfborder == null
							|| p.parentIfborder.core_expired < n.core_expired) {
						p.parentIfborder = n;
					} else if (n.parentIfborder == null
							|| n.parentIfborder.core_expired < p.core_expired) {
						n.parentIfborder = p;
					}
				} else {
					if (n.parentIfborder == null
							|| p.core_expired > n.parentIfborder.core_expired)
						n.parentIfborder = p;

				}
			}

		} else {
			for (DataPoint n : neighbors) {
				if (n.core_expired >= time.getTime())// coreflag
				{
					if (p.parentIfborder == null
							|| n.core_expired > p.parentIfborder.core_expired)
						p.parentIfborder = n;
				}
			}
		}

		double[] ls = new double[dim];
		double[] us = new double[dim];

		Element<DataPoint> e = new Element<DataPoint>(p);

		for (int d = 0; d < dim; d++) {
			ls[d] = p.values[d];
			us[d] = p.values[d];
		}
		mrtree.Insert(ls, us, e);

	}

	/**
	 * @param p
	 */
	public void insert(DataPoint p) {

		Deque<DataPoint> neighbors = findNeighbors_wIndex(p);

		if (neighbors.size() >= minPts) {

			LCNode newNode = new LCNode();
			p.node = newNode;
			p.node.dp = p;

			List<DataPoint> neighbors_list = new ArrayList<DataPoint>(neighbors);
			sort(neighbors_list);

			p.core_expired = neighbors_list.get(minPts - 1).timestamp;
			newNode.ts = p.core_expired;
			HashSet<DataPoint> res = expiredTable.getOrDefault(p.core_expired,
					new HashSet<DataPoint>());
			res.add(p);
			expiredTable.put(p.core_expired, res);

			for (DataPoint n : neighbors_list) {
				if (n.core_expired >= time.getTime())// coreflag
				{
					// LINK p and n with the weight<min(CET(n), CET(p)>
					connect(p.node, n.node);
					if (p.parentIfborder == null
							|| p.parentIfborder.core_expired < n.core_expired) {
						p.parentIfborder = n;
					} else if (n.parentIfborder == null
							|| n.parentIfborder.core_expired < p.core_expired) {
						n.parentIfborder = p;
					}
				} else {
					// System.out.println(-n.core_expired-2);
					if (n.parentIfborder == null
							|| p.core_expired > n.parentIfborder.core_expired)
						n.parentIfborder = p;

				}
			}

		} else {
			for (DataPoint n : neighbors) {
				if (n.core_expired >= time.getTime())// coreflag
				{
					if (p.parentIfborder == null
							|| n.core_expired > p.parentIfborder.core_expired)
						p.parentIfborder = n;
				}
			}
		}

		double[] ls = new double[dim];
		double[] us = new double[dim];

		Element<DataPoint> e = new Element<DataPoint>(p);

		for (int d = 0; d < dim; d++) {
			ls[d] = p.values[d];
			us[d] = p.values[d];
		}
		mrtree.Insert(ls, us, e);

	}

	public void batch_insert(List<DataPoint> pset){
		insert_multiple(pset);
	}
	
	/**
	 * @param pset
	 */
	public void insert_multiple(List<DataPoint> pset) {

		
		// Insert into the spatial index
		
		for (DataPoint p : pset) {
			double[] ls = new double[dim];
			double[] us = new double[dim];

			Element<DataPoint> e = new Element<DataPoint>(p);

			for (int d = 0; d < dim; d++) {
				ls[d] = p.values[d];
				us[d] = p.values[d];
			}
			mrtree.Insert(ls, us, e);

		}

		// Insert data points into the clusters 
		
		for (DataPoint p : pset) {

			Deque<DataPoint> neighbors = findNeighbors_wIndex(p);

			if (neighbors.size() >= minPts) { //Nostalgic core

				LCNode newNode = new LCNode();
				p.node = newNode;
				p.node.dp = p;
				List<DataPoint> neighbors_list = new ArrayList<DataPoint>(neighbors);
				

				// Compute core-expriation time
				sort(neighbors_list);
				p.core_expired = neighbors_list.get(minPts - 1).timestamp;
				newNode.ts = p.core_expired;
				
 
				// Record (core-expriation time, datapoint)
				HashSet<DataPoint> res = expiredTable.getOrDefault(p.core_expired, new HashSet<DataPoint>());
				res.add(p);
				expiredTable.put(p.core_expired, res);

				// Update DenTree
				for (DataPoint n : neighbors_list) {
					if (n.core_expired >= time.getTime() && n.id != p.id)// coreflag
					{
						connect(p.node, n.node);
						if (p.parentIfborder == null
								|| p.parentIfborder.core_expired < n.core_expired) {
							p.parentIfborder = n;
						}

						if (n.parentIfborder == null
								|| n.parentIfborder.core_expired < p.core_expired) {
							n.parentIfborder = p;
						}
					} else {
						if (n.parentIfborder == null
								|| p.core_expired > n.parentIfborder.core_expired)
							n.parentIfborder = p;
					}
				}
			} else { // Non-core
				for (DataPoint n : neighbors) {
					if (n.core_expired >= time.getTime())// coreflag
					{
						if (p.parentIfborder == null
								|| n.core_expired > p.parentIfborder.core_expired)
							p.parentIfborder = n;
					}
				}
			}

		}
	}

	/**
	 * @param node1
	 * @param node2
	 */
	void removeEdgeFromTable(LCNode node1, LCNode node2) {
		edgeTable.get(node1.dp).remove(node2.dp);
		edgeTable.get(node2.dp).remove(node1.dp);
	}

	void insertEdgeIntoTable(LCNode node1, LCNode node2) {
		HashSet<DataPoint> A = edgeTable.getOrDefault(node1.dp,
				new HashSet<DataPoint>());
		HashSet<DataPoint> B = edgeTable.getOrDefault(node2.dp,
				new HashSet<DataPoint>());
		if (A.size() == 0) {
			A.add(node2.dp);
			edgeTable.put(node1.dp, A);
		} else {
			A.add(node2.dp);
		}

		if (B.size() == 0) {
			B.add(node1.dp);
			edgeTable.put(node2.dp, B);
		} else {
			B.add(node1.dp);
		}
	}

	void connect(LCNode newNode, LCNode leafNode) {
		LCNode edge = new LCNode();
		edge.ts = Math.min(newNode.ts, leafNode.ts);
		edge.minval = edge.ts;
		edge.isedge = true;

		Edge oldestEdge = lct.FindOldestEdge(leafNode, newNode);
		if (oldestEdge == null) {
			lct.link(leafNode, newNode);
			insertEdgeIntoTable(leafNode, newNode);
		} else {
			// remove oldestedge and
			if (edge.ts > oldestEdge.ts) {
				lct.cut(oldestEdge.v2, oldestEdge.v1);
				removeEdgeFromTable(oldestEdge.v1, oldestEdge.v2);
				lct.link(newNode, leafNode);
				insertEdgeIntoTable(leafNode, newNode);

			}
		}
	}

	void simple_connect(LCNode newNode, LCNode leafNode) {
		LCNode edge = new LCNode();
		edge.ts = Math.min(newNode.ts, leafNode.ts);
		edge.minval = edge.ts;
		edge.isedge = true;

		lct.link(leafNode, newNode);

	}

	public void delete(DataPoint p) {

		/** Delete p from the Spatial Index **/
		double[] lb = new double[dim];
		double[] ub = new double[dim];

		for (int i = 0; i < dim; i++) {
			lb[i] = p.values[i];
			ub[i] = p.values[i];
		}

		Element<DataPoint> e = new Element<DataPoint>(p);
		mrtree.delete(ub, lb, e);

		HashSet<DataPoint> res = expiredTable.getOrDefault(time.getTime(),
				new HashSet<DataPoint>());
		expiredTable.remove(time.getTime());
		for (DataPoint ep : res) {
			HashSet<DataPoint> R = edgeTable.get(ep);
			if (R != null) {
				edgeTable.remove(ep);
				for (DataPoint nn : R) {
					// MORE THAN TWO ===> CAUSE DELETE
					lct.cut(ep.node, nn.node);
					edgeTable.get(nn).remove(ep);
				}

			}
		}

	}

	
	public void batch_delete(List<DataPoint> pset){
		delete_multiple(pset);
	}
	
	public void delete_multiple(List<DataPoint> pset) {

		for (DataPoint p : pset) {
			/** Delete p from the Spatial Index **/
			double[] lb = new double[dim];
			double[] ub = new double[dim];

			for (int i = 0; i < dim; i++) {
				lb[i] = p.values[i];
				ub[i] = p.values[i];
			}

			Element<DataPoint> e = new Element<DataPoint>(p);
			mrtree.delete(ub, lb, e);
		}

		HashSet<DataPoint> res = expiredTable.getOrDefault(time.getTime(),
				new HashSet<DataPoint>());
		expiredTable.remove(time.getTime());
		for (DataPoint ep : res) {
			HashSet<DataPoint> R = edgeTable.get(ep);
			if (R != null) {
				edgeTable.remove(ep);
				for (DataPoint nn : R) {
					// MORE THAN TWO ===> CAUSE DELETE
					lct.cut(ep.node, nn.node);
					edgeTable.get(nn).remove(ep);

				}

			}
		}

	}

}
