package denforest;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.Comparator;

import denforest.datapoint.DataPoint;
import denforest.datapoint.Timestamp;
import denforest.linkcuttree.*;
import denforest.basicRtree.Element;
import denforest.basicRtree.BasicRtree;
import denforest.unionfind.UnionFind;

import java.util.HashMap;

public class DenForestStrided {

	int UNCLASSIFIED = 0;
	int NOISE = -1;
	int clusterID = 1;

	LinkedList<DataPoint> dataset; // Dataset
	BasicRtree<DataPoint> mrtree;
	HashMap<DataPoint, HashSet<DataPoint>> edgeTable;

	static LinkCutTree LCT;

	int dim; // Dimension
	double eps; // epsilon
	int minPts; // min points

	UnionFind uf;
	int window_size;

	Timestamp time;
	HashMap<Integer, Integer> labels;

	int offset = 0;
	List<DataPoint> window;

	HashMap<Long, HashSet<DataPoint>> expiredTable;

	public void label(List<DataPoint> all, int windowsize, int stridesize,
			int Time) {

		for (DataPoint p : window) {
			// core but not labeled
			int id_in_window = uf.find(p.id) - offset;
			if (p.core_expired >= time.getTime()
					&& all.get(id_in_window).label == -1) {
				LCT.Label(all.get(id_in_window).node, uf.find(p.id), labels);
			}
			// core and already labeled
			else if (p.core_expired >= time.getTime()
					&& all.get(id_in_window).label != -1) {
				p.label = all.get(id_in_window).label;
			}
		}

		for (DataPoint p : window) {
			// Border
			if (p.core_expired < time.getTime() && p.parentIfborder != null
					&& p.parentIfborder.core_expired > time.getTime()) {
				p.label = p.parentIfborder.label;
			} else {
				p.label = -1;
			}
		}

	}

	public int[] labelAndReturn() {
		labels.clear();
		for (DataPoint p : window) {
			// core but not labeled
			int id_in_window = uf.find(p.id) - offset;
			DataPoint q = window.get(id_in_window);
			if (p.core_expired >= time.getTime() && q.label == -1) {
				if (q.node == null) {
					q.label = q.id;
					labels.put(q.label, q.label);
					p.label = q.label;
				} else {
					LCT.Label2(q.node, q.id, labels);
					p.label = q.label;
				}
			}
			// core and already labeled
			else if (p.core_expired >= time.getTime() && q.label != -1) {

				p.label = q.label;

			}
		}

		for (DataPoint p : window) {
			// Border
			if (p.core_expired < time.getTime() && p.parentIfborder != null
					&& p.parentIfborder.core_expired >= time.getTime()) {
				p.label = p.parentIfborder.label;
			} else if (p.core_expired < time.getTime()) {
				p.label = -1;
				for (DataPoint n : findNeighbors_wIndex(p)) {
					if (n.core_expired >= time.getTime())
						System.out.println(p.core_expired + " " + p.label + " "
								+ n.core_expired + " " + n.id + " " + p.id);
				}
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

	public void check() {
		for (DataPoint p : window) {
			// Border
			if (p.core_expired < time.getTime() && p.parentIfborder != null
					&& p.parentIfborder.core_expired >= time.getTime()) {

			} else if (p.core_expired < time.getTime()) {
				for (DataPoint n : findNeighbors_wIndex(p)) {
					if (n.core_expired >= time.getTime())
						System.out.println(p.core_expired + " " + p.label + " "
								+ n.core_expired + " " + n.id + " " + p.id);
				}
			}
		}
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

	public DenForestStrided(int dim, double eps, int minPts, Timestamp time) {
		this.dim = dim;
		this.eps = eps;
		this.minPts = minPts;
		LCT = new LinkCutTree();
		mrtree = new BasicRtree<DataPoint>(dim, 50, 20);
		expiredTable = new HashMap<Long, HashSet<DataPoint>>();
		edgeTable = new HashMap<DataPoint, HashSet<DataPoint>>();
		labels = new HashMap<Integer, Integer>();
		this.time = time;
		window = new ArrayList<DataPoint>();
		offset = 0;
		uf = new UnionFind();
	}

	public void init(int dim, double eps, int minPts) {
		this.dim = dim;
		this.eps = eps;
		this.minPts = minPts;
		LCT = new LinkCutTree();
		mrtree = new BasicRtree<DataPoint>(dim, 50, 20);

		uf = new UnionFind();
		expiredTable = new HashMap<Long, HashSet<DataPoint>>();
		window = new ArrayList<DataPoint>();
		offset = 0;
	}

	public double l2distance(double[] p1, double[] p2, int dim) {
		double dist = 0;
		for (int d = 0; d < dim; d++) {
			dist += (p1[d] - p2[d]) * (p1[d] - p2[d]);

		}

		return dist;
	}

	List<DataPoint> findNeighbors_wIndex(DataPoint p) {

		double[] ls = new double[dim];
		double[] us = new double[dim];

		for (int d = 0; d < dim; d++) {
			ls[d] = p.values[d] - eps;

		}
		for (int d = 0; d < dim; d++) {
			us[d] = p.values[d] + eps;
		}

		List<Element<DataPoint>> le = mrtree.intersects(us, ls);

		List<DataPoint> neighbors = new ArrayList<DataPoint>();
		for (Element<DataPoint> e : le) {
			DataPoint pp = e.p;

			if (l2distance(p.values, pp.values, dim) <= eps * eps) {
				neighbors.add(pp);
			}

		}
		return neighbors;
	}

	public void sort(List<DataPoint> neighbors_list) {
		Collections.sort(neighbors_list, new Comparator<DataPoint>() {
			public int compare(DataPoint p1, DataPoint p2) {
				return Long.valueOf(p2.timestamp).compareTo(
						Long.valueOf(p1.timestamp));
			}
		});
	}

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
		long ts = Math.min(newNode.ts, leafNode.ts);

		Edge oldestEdge = LCT.FindOldestEdge(leafNode, newNode);
		if (oldestEdge == null) {
			LCT.link(leafNode, newNode);
			insertEdgeIntoTable(leafNode, newNode);
		} else {
			// remove oldestedge and
			if (ts > oldestEdge.ts) {
				LCT.cut(oldestEdge.v2, oldestEdge.v1);
				removeEdgeFromTable(oldestEdge.v1, oldestEdge.v2);
				LCT.link(newNode, leafNode);
				insertEdgeIntoTable(leafNode, newNode);

			}
		}
	}
	
	public void batch_insert(List<DataPoint> in){
		insert_stride(in);
	}

	public void insert_stride(List<DataPoint> in) {
		/* Insert all points into R-tree */
		/* Initialize UF for each point */

		double[] ls = new double[dim];
		double[] us = new double[dim];

		for (DataPoint p : in) {
			Element<DataPoint> e = new Element<DataPoint>(p);
			for (int d = 0; d < dim; d++) {
				ls[d] = p.values[d];
				us[d] = p.values[d];
			}
			mrtree.Insert(ls, us, e);
			uf.create(p.id);
		}

		window.addAll(in);

		Set<UndirectedEdge> sc2sc = new HashSet<UndirectedEdge>();

		for (DataPoint p_in : in) {
			if (!p_in.visited) {
				if (p_in.core_expired != -1) {
					System.out.println("ERROR1");
					return;
				}

				List<DataPoint> neighbors = findNeighbors_wIndex(p_in);

				if (neighbors.size() >= minPts) {

					/* Init core p */
					sort(neighbors);

					init_core_point(p_in, neighbors);

					Pair<DataPoint, List<DataPoint>> pair = new Pair<DataPoint, List<DataPoint>>(
							p_in, neighbors);
					Deque<Pair<DataPoint, List<DataPoint>>> stack = new ArrayDeque<Pair<DataPoint, List<DataPoint>>>();
					stack.push(pair);

					while (!stack.isEmpty()) {
						Pair<DataPoint, List<DataPoint>> elem = stack.pop();
						DataPoint p = elem.first;
						for (DataPoint n : elem.second) {

							/* Core visits visited NonCore */
							if (n.visited && n.core_expired < time.getTime()) {

								if (n.parentIfborder == null
										|| p.core_expired > n.parentIfborder.core_expired)
									n.parentIfborder = p;
							}
							/* Core visits visited Core */
							else if (n.visited
									&& n.core_expired >= time.getTime()
									&& p.id != n.id) {
								/* New Stride and Same CET */
								if (n.timestamp == p.timestamp
										&& n.core_expired == p.core_expired) {

									uf.union(n.id, p.id);
								}
								/* Else */
								else {

									if (p.parentIfborder == null
											|| p.parentIfborder.core_expired < n.core_expired) {
										p.parentIfborder = n;
									}
									if (n.parentIfborder == null
											|| n.parentIfborder.core_expired < p.core_expired) {
										n.parentIfborder = p;
									}

									sc2sc.add(new UndirectedEdge(uf.find(p.id),
											uf.find(n.id)));
								}
							}
							/* Core visits unvisited unclassfied point */
							else if (!n.visited) {

								if (n.timestamp != p.timestamp) {
									System.out.println("ERROR2");

								}

								List<DataPoint> neighbors_of_n = findNeighbors_wIndex(n);

								if (neighbors_of_n.size() >= minPts) {

									sort(neighbors_of_n);

									/* Init Core */
									init_core_point(n, neighbors_of_n);

									if (n.core_expired == p.core_expired) {
										uf.union(n.id, p.id);
									} else {

										if (p.parentIfborder == null
												|| p.parentIfborder.core_expired < n.core_expired) {
											p.parentIfborder = n;
										}

										if (n.parentIfborder == null
												|| n.parentIfborder.core_expired < p.core_expired) {
											n.parentIfborder = p;
										}

										/* Option 1 */
										sc2sc.add(new UndirectedEdge(uf
												.find(p.id), uf.find(n.id)));

										/* Option 2 */
										// //sc2sc.add(new Pair(p.id, n.id));
									}

									Pair<DataPoint, List<DataPoint>> pair_of_n = new Pair<DataPoint, List<DataPoint>>(
											n, neighbors_of_n);
									stack.push(pair_of_n);
								} else {
									n.visited = true;
									for (DataPoint nn : neighbors_of_n) {
										if (nn.core_expired >= time.getTime())// coreflag
										{
											if (n.parentIfborder == null
													|| n.parentIfborder.core_expired < nn.core_expired) {
												n.parentIfborder = nn;
											}
										}
									}
								}
							}
						}
					}

				} else {
					for (DataPoint n : neighbors) {
						if (n.core_expired >= time.getTime())// coreflag
						{
							if (p_in.parentIfborder == null
									|| n.core_expired > p_in.parentIfborder.core_expired)
								p_in.parentIfborder = n;
						}
					}
					p_in.visited = true;

				}
			}
		}

		/* Update LinkCut Tree */
		Set<UndirectedEdge> reduced_sc2sc = new HashSet<UndirectedEdge>();
		for (UndirectedEdge e : sc2sc) {
			reduced_sc2sc.add(new UndirectedEdge(uf.find(e.first), uf
					.find(e.second)));
		}

		for (UndirectedEdge e : reduced_sc2sc) {

			DataPoint REPp = window.get(e.first - offset);
			DataPoint REPq = window.get(e.second - offset);

			if (REPp.node == null) {
				LCNode newNode = new LCNode();
				REPp.node = newNode;
				REPp.node.dp = REPp;
				newNode.ts = REPp.core_expired;
				HashSet<DataPoint> res = expiredTable.getOrDefault(
						REPp.core_expired, new HashSet<DataPoint>());
				res.add(REPp);
				expiredTable.put(REPp.core_expired, res);

			}

			if (REPq.node == null) {
				LCNode newNode = new LCNode();
				REPq.node = newNode;
				REPq.node.dp = REPq;
				newNode.ts = REPq.core_expired;
				HashSet<DataPoint> res = expiredTable.getOrDefault(
						REPq.core_expired, new HashSet<DataPoint>());
				res.add(REPq);
				expiredTable.put(REPq.core_expired, res);

			}

			connect(REPp.node, REPq.node);
		}

	}
	
	
	public void batch_delete(List<DataPoint> in){
		delete_stride(in);
	}


	public void delete_stride(List<DataPoint> out) {

		double[] lb = new double[dim];
		double[] ub = new double[dim];
		for (DataPoint p : out) {
			for (int i = 0; i < dim; i++) {
				lb[i] = p.values[i];
				ub[i] = p.values[i];
			}
			Element<DataPoint> e = new Element<DataPoint>(p);
			mrtree.delete(ub, lb, e);
		}

		HashSet<DataPoint> res_temp = expiredTable.getOrDefault(time.getTime(),
				new HashSet<DataPoint>());
		expiredTable.remove(time.getTime());

		HashSet<DataPoint> res = new HashSet<DataPoint>();
		for (DataPoint ep : res_temp) {
			int id_in_window = ep.id - offset;
			res.add(window.get(id_in_window));
		}

		for (DataPoint ep : res) {
			HashSet<DataPoint> R = edgeTable.get(ep);
			if (R != null) {
				edgeTable.remove(ep);
				for (DataPoint nn : R) {
					// MORE THAN TWO ===> CAUSE DELETE
					LCT.cut(ep.node, nn.node);
					if (!edgeTable.get(nn).remove(ep)) {
						System.out.println("ERROR");
					}
				}

			}
		}

		window = window.subList(out.size(), window.size());
		offset += out.size();

	}

	public void init_core_point(DataPoint p, List<DataPoint> neighbors) {
		p.core_expired = neighbors.get(minPts - 1).timestamp;
		p.visited = true;
		if (p.core_expired == -1) {
			System.out.println("ERROR101");
		}

	}

}
