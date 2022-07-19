//package denforest;
//
//import java.util.ArrayDeque;
//import java.util.ArrayList;
//import java.util.Deque;
//import java.util.HashSet;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.PriorityQueue;
//import java.util.Set;
//import java.util.Collections;
//import java.util.Comparator;
//
//import Data.DataPoint;
//import DenStream.Timestamp;
//import TestRtree.Element;
//import TestRtree.MBR;
//import TestRtree.mRtree;
//import UnionFind.UnionFind;
//import UnionFind.UnionFindFast;
//
//import java.util.HashMap;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//
//import denforest.linkcuttree.Edge;
//import denforest.linkcuttree.LCNode;
//import denforest.linkcuttree.LinkCutTree;
//
//public class DenForestBUwUFFAST {
//
//	public long range_query = 0;
//
//	public long sort = 0;
//
//	public long findingSNC = 0;
//
//	public long updMST = 0;
//
//	public long unionFind = 0;
//
//	public long initCore = 0;
//
//	LinkedList<DataPoint> dataset; // Dataset
//	static mRtree mrtree;
//	static mRtree Corertree;
//	static HashMap<DataPoint, HashSet<DataPoint>> edgeTable;
//
//	static LinkCutTree LCT;
//
//	int DIM; // Dimension
//	double eps; // epsilon
//	int minPts; // min points
//
//	UnionFindFast uf;
//	int window_size;
//
//	int UNCLASSIFIED = 0;
//	int NOISE = -1;
//	int clusterID = 1;
//	Timestamp time;
//	HashMap<Integer, Integer> labels;
//
//	int offset = 0;
//	List<DataPoint> window;
//
//	public long t1, t2, t3, t4, t5;
//	HashMap<Long, HashSet<DataPoint>> expiredTable;
//
//	public void Label(List<DataPoint> all, int windowsize, int stridesize,
//			int Time) {
//
//		for (DataPoint p : window) {
//			// core but not labeled
//			int id_in_window = Math.floorMod(uf.find(p.id % window_size)
//					- offset, window_size);
//			if (p.core_expired >= time.getTime()
//					&& all.get(id_in_window).label == -1) {
//				LCT.Label(all.get(id_in_window).node,
//						uf.find(p.id % window_size), labels);
//			}
//			// core and already labeled
//			else if (p.core_expired >= time.getTime()
//					&& all.get(id_in_window).label != -1) {
//				p.label = all.get(id_in_window).label;
//			}
//		}
//
//		for (DataPoint p : window) {
//			// Border
//			if (p.core_expired < time.getTime() && p.parentIfborder != null
//					&& p.parentIfborder.core_expired > time.getTime()) {
//				p.label = p.parentIfborder.label;
//			} else {
//				p.label = -1;
//			}
//		}
//
//	}
//
//	public void LabelandStore() {
//
//		for (DataPoint p : window) {
//			// core but not labeled
//			int id_in_window = Math.floorMod(uf.find(p.id % window_size)
//					- offset, window_size);
//			DataPoint q = window.get(id_in_window);
//			if (p.core_expired >= time.getTime() && q.label == -1) {
//				if (q.node == null) {
//					q.label = q.id;
//					labels.put(q.label, q.label);
//					p.label = q.label;
//				} else {
//					LCT.Label2(q.node, q.id, labels);
//					p.label = q.label;
//				}
//			}
//			// core and already labeled
//			else if (p.core_expired >= time.getTime() && q.label != -1) {
//
//				p.label = q.label;
//
//			}
//		}
//		for (DataPoint p : window) {
//			// Border
//			if (p.core_expired < time.getTime() && p.parentIfborder != null
//					&& p.parentIfborder.core_expired >= time.getTime()) {
//				p.label = p.parentIfborder.label;
//			} else if (p.core_expired < time.getTime()) {
//				p.label = -1;
//				for (DataPoint n : findNeighbors_wIndex(p)) {
//					if (n.core_expired >= time.getTime())
//						System.out.println(p.label + " " + n.core_expired);
//				}
//			}
//		}
//
//		File file = new File("DISC2.txt");
//		FileWriter writer = null;
//
//		try {
//			writer = new FileWriter(file, false);
//
//			for (DataPoint p : window) {
//				String message = "";
//				for (double val : p.values) {
//					message += val + ", ";
//				}
//
//				message += p.timestamp + ", ";
//				if (p.label != -1)
//					message += (labels.get(p.label) * 1897 % 123);
//				else
//					message += "-1";
//
//				// if( p.label != - 1 )
//				writer.write(message + "\n");
//			}
//			writer.flush();
//
//			System.out.println("DONE");
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				if (writer != null)
//					writer.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//
//	}
//
//	public int[] LabelandReturn() {
//		labels.clear();
//		for (DataPoint p : window) {
//			// core but not labeled
//			int id_in_window = Math.floorMod(uf.find(p.id % window_size)
//					- offset, window_size);
//			DataPoint q = window.get(id_in_window);
//			if (p.core_expired >= time.getTime() && q.label == -1) {
//				if (q.node == null) {
//					q.label = q.id;
//					labels.put(q.label, q.label);
//					p.label = q.label;
//				} else {
//					LCT.Label2(q.node, q.id, labels);
//					p.label = q.label;
//				}
//			}
//			// core and already labeled
//			else if (p.core_expired >= time.getTime() && q.label != -1) {
//
//				p.label = q.label;
//
//			}
//		}
//
//		for (DataPoint p : window) {
//			// Border
//			if (p.core_expired < time.getTime() && p.parentIfborder != null
//					&& p.parentIfborder.core_expired >= time.getTime()) {
//				p.label = p.parentIfborder.label;
//			} else if (p.core_expired < time.getTime()) {
//				p.label = -1;
//				for (DataPoint n : findNeighbors_wIndex(p)) {
//					if (n.core_expired >= time.getTime())
//						System.out.println(p.core_expired + " " + p.label + " "
//								+ n.core_expired + " " + n.id + " " + p.id);
//				}
//			}
//		}
//
//		int res[] = new int[window.size()];
//		int jj = 0;
//
//		for (DataPoint p : window) {
//			if (p.label == -1)
//				res[jj] = -1;
//			else
//				res[jj] = labels.get(p.label);
//			jj++;
//		}
//		return res;
//	}
//
//	public void check() {
//		System.out.println("CHECK");
//		for (DataPoint p : window) {
//			// Border
//			if (p.core_expired < time.getTime() && p.parentIfborder != null
//					&& p.parentIfborder.core_expired >= time.getTime()) {
//
//			} else if (p.core_expired < time.getTime()) {
//				for (DataPoint n : findNeighbors_wIndex(p)) {
//					if (n.core_expired >= time.getTime())
//						System.out.println(p.core_expired + " " + p.label + " "
//								+ n.core_expired + " " + n.id + " " + p.id);
//				}
//			}
//		}
//	}
//
//	public HashMap<Integer, Set<Integer>> LabelandCollect(List<DataPoint> all,
//			int window, int stride, int Time) {
//		label(all, window, stride, Time);
//		HashMap<Integer, Set<Integer>> clusters = new HashMap<Integer, Set<Integer>>();
//		for (DataPoint p : all.subList((Time) * stride, window + (Time)
//				* stride)) {
//			if (p.label == -1) {
//				Set<Integer> S = clusters.getOrDefault(-1,
//						new HashSet<Integer>());
//				S.add(p.id);
//				clusters.put(-1, S);
//			} else {
//				Set<Integer> S = clusters.getOrDefault(labels.get(p.label),
//						new HashSet<Integer>());
//				S.add(p.id);
//				clusters.put(labels.get(p.label), S);
//			}
//		}
//		return clusters;
//	}
//
//	public DenForestBUwUFFAST(int dim, double eps, int minPts, Timestamp time,
//			int pwindow_size) {
//		this.DIM = dim;
//		this.eps = eps;
//		this.minPts = minPts;
//		LCT = new LinkCutTree();
//		mrtree = new mRtree(DIM, 50, 20);
//		Corertree = new mRtree(DIM, 50, 20);
//		expiredTable = new HashMap<Long, HashSet<DataPoint>>();
//		edgeTable = new HashMap<DataPoint, HashSet<DataPoint>>();
//		labels = new HashMap<Integer, Integer>();
//		this.time = time;
//		window = new ArrayList<DataPoint>();
//		offset = 0;
//		uf = new UnionFindFast(pwindow_size);
//		window_size = pwindow_size;
//	}
//
//	public void init(int dim, double eps, int minPts, int pwindow_size) {
//		this.DIM = dim;
//		this.eps = eps;
//		this.minPts = minPts;
//		LCT = new LinkCutTree();
//		mrtree = new mRtree(DIM, 50, 20);
//		Corertree = new mRtree(DIM, 50, 20);
//		uf = new UnionFindFast(pwindow_size);
//		expiredTable = new HashMap<Long, HashSet<DataPoint>>();
//		window = new ArrayList<DataPoint>();
//		offset = 0;
//		window_size = pwindow_size;
//	}
//
//	public static double L2Distance(double[] p1, double[] p2, int dim) {
//		double dist = 0;
//		for (int d = 0; d < dim; d++) {
//			dist += (p1[d] - p2[d]) * (p1[d] - p2[d]);
//
//		}
//
//		return dist;
//	}
//
//	List<DataPoint> FindNeighbors_with_Index(DataPoint p) {
//
//		double[] ls = new double[DIM];
//		double[] us = new double[DIM];
//
//		for (int d = 0; d < DIM; d++) {
//			ls[d] = p.values[d] - eps;
//
//		}
//		for (int d = 0; d < DIM; d++) {
//			us[d] = p.values[d] + eps;
//		}
//
//		List<Element> le = mrtree.intersects(us, ls);
//
//		List<DataPoint> neighbors = new ArrayList<DataPoint>();
//		for (Element<DataPoint> e : le) {
//			DataPoint pp = e.p;
//
//			if (l2distance(p.values, pp.values, DIM) <= eps * eps) {
//				neighbors.add(pp);
//			}
//
//		}
//		return neighbors;
//	}
//
//	Set<DataPoint> FindCoreNeighbors_with_Index(DataPoint p) {
//
//		double[] ls = new double[DIM];
//		double[] us = new double[DIM];
//
//		for (int d = 0; d < DIM; d++) {
//			ls[d] = p.values[d] - eps;
//			us[d] = p.values[d] + eps;
//
//		}
//
//		List<Element> le = Corertree.intersects(us, ls);
//
//		Set<DataPoint> neighbors = new HashSet<DataPoint>();
//		for (Element<DataPoint> e : le) {
//			DataPoint pp = e.p;
//			if (l2distance(p.values, pp.values, DIM) <= eps * eps) {
//				neighbors.add(pp);
//			}
//
//		}
//		return neighbors;
//	}
//
//	public void sort(List<DataPoint> neighbors_list) {
//		Collections.sort(neighbors_list, new Comparator<DataPoint>() {
//			public int compare(DataPoint p1, DataPoint p2) {
//				return Long.valueOf(p2.timestamp).compareTo(
//						Long.valueOf(p1.timestamp));
//			}
//		});
//	}
//
//	// public void Increment(DataPoint p)
//	// {
//	// long tmp1,tmp2,tmp3,tmp4,tmp5,tmp6;
//	// tmp1 = System.nanoTime();
//	//
//	// List<DataPoint> neighbors = FindNeighbors_with_Index(p);
//	//
//	// // for( DataPoint dp : neighbors)
//	// // {
//	// // dp.number_of_neighbors++;
//	// // }
//	// // p.number_of_neighbors = neighbors.size();
//	//
//	// tmp2 = System.nanoTime();
//	// t1 += tmp2-tmp1;
//	//
//	// if( neighbors.size() >= minPts ){
//	//
//	//
//	// tmp3 = System.nanoTime();
//	//
//	// System.out.println(neighbors.size());
//	// sort(neighbors);
//	// System.out.println(neighbors.size());
//	//
//	// //newNode.values = p.values;
//	//
//	// tmp4 = System.nanoTime();
//	// t2 += tmp4-tmp3;
//	//
//	//
//	// /* Init Core Point */
//	// init_core_point(p,neighbors);
//	//
//	//
//	//
//	// tmp5 = System.nanoTime();
//	// t3 += tmp5-tmp4;
//	//
//	// LCNode newNode = new LCNode();
//	// p.node = newNode;
//	// p.node.dp = p;
//	// newNode.ts = p.core_expired;
//	//
//	// for(DataPoint n : neighbors)
//	// {
//	// if( n.core_expired >= time.getTime())// coreflag
//	// {
//	// // LINK p and n with the weight<min(CET(n), CET(p)>
//	// connect(p.node, n.node);
//	// }
//	// else
//	// {
//	// //System.out.println(-n.core_expired-2);
//	// if( -n.core_expired-2 < p.core_expired ){
//	//
//	// n.label = p.id;
//	// n.core_expired = -p.core_expired-2;
//	// };
//	// }
//	// }
//	//
//	// tmp6 = System.nanoTime();
//	// t4 += tmp6-tmp5;
//	//
//	// }
//	// else {
//	// for(DataPoint n : neighbors)
//	// {
//	// if( n.core_expired >= time.getTime())// coreflag
//	// {
//	// // LINK p and n with the weight<min(CET(n), CET(p)>
//	// if( -p.core_expired-2 < n.core_expired ){
//	// p.label = n.id;
//	// p.core_expired = -n.core_expired-2;
//	// }
//	// }
//	// }
//	// }
//	//
//	//
//	// // insert p into the Spatial Index
//	// double[] ls = new double[DIM];
//	// double[] us = new double[DIM];
//	// MBR mbr = new MBR(DIM);
//	//
//	// Element<DataPoint> e = new Element<DataPoint>(p);
//	//
//	// for( int d = 0; d < DIM; d++)
//	// {
//	// ls[d] = p.values[d];
//	// us[d] = p.values[d];
//	// }
//	// mrtree.Insert(ls, us, e);
//	//
//	// }
//
//	static void connect() {
//
//	}
//
//	static void RemovefromEdgeTable(LCNode node1, LCNode node2) {
//		edgeTable.get(node1.dp).remove(node2.dp);
//		edgeTable.get(node2.dp).remove(node1.dp);
//	}
//
//	static void InsertintoEdgeTable(LCNode node1, LCNode node2) {
//		HashSet<DataPoint> A = edgeTable.getOrDefault(node1.dp,
//				new HashSet<DataPoint>());
//		HashSet<DataPoint> B = edgeTable.getOrDefault(node2.dp,
//				new HashSet<DataPoint>());
//
//		if (A.size() == 0) {
//			A.add(node2.dp);
//			edgeTable.put(node1.dp, A);
//		} else {
//			A.add(node2.dp);
//		}
//
//		if (B.size() == 0) {
//			B.add(node1.dp);
//			edgeTable.put(node2.dp, B);
//		} else {
//			B.add(node1.dp);
//		}
//
//	}
//
//	static void connect(LCNode newNode, LCNode leafNode) {
//		long ts = Math.min(newNode.ts, leafNode.ts);
//
//		Edge oldestEdge = LCT.FindOldestEdge(leafNode, newNode);
//		if (oldestEdge == null) {
//			LCT.link(leafNode, newNode);
//			InsertintoEdgeTable(leafNode, newNode);
//		} else {
//			// remove oldestedge and
//			if (ts > oldestEdge.ts) {
//				LCT.cut(oldestEdge.v2, oldestEdge.v1);
//				RemovefromEdgeTable(oldestEdge.v1, oldestEdge.v2);
//				LCT.link(newNode, leafNode);
//				InsertintoEdgeTable(leafNode, newNode);
//
//			}
//		}
//	}
//
//	public void Decrement(DataPoint p) {
//
//		/** Delete p from the Spatial Index **/
//		double[] lb = new double[DIM];
//		double[] ub = new double[DIM];
//
//		for (int i = 0; i < DIM; i++) {
//			lb[i] = p.values[i];
//			ub[i] = p.values[i];
//		}
//
//		Element<DataPoint> e = new Element<DataPoint>(p);
//		mrtree.delete(ub, lb, e);
//
//		HashSet<DataPoint> res_temp = expiredTable.getOrDefault(time.getTime(),
//				new HashSet<DataPoint>());
//		HashSet<DataPoint> res = new HashSet<DataPoint>();
//		expiredTable.remove(time.getTime());
//		for (DataPoint ep : res_temp) {
//
//		}
//
//		for (DataPoint ep : res) {
//			HashSet<DataPoint> R = edgeTable.get(ep);
//			if (R != null) {
//				edgeTable.remove(ep);
//				long maxept = -1;
//				int label = -1;
//				for (DataPoint nn : R) {
//					// MORE THAN TWO ===> CAUSE DELETE
//					LCT.cut(ep.node, nn.node);
//					edgeTable.get(nn).remove(ep);
//					if (nn.core_expired > maxept) {
//						maxept = nn.core_expired;
//						label = nn.id;
//					}
//					ep.label = label;
//					ep.core_expired = -nn.core_expired;
//				}
//
//			}
//		}
//
//	}
//
//	public void BatchIncrement(List<DataPoint> in) {
//		/* Insert all points into R-tree */
//		/* Initialize UF for each point */
//
//		double[] ls = new double[DIM];
//		double[] us = new double[DIM];
//
//		for (DataPoint p : in) {
//			Element<DataPoint> e = new Element<DataPoint>(p);
//			for (int d = 0; d < DIM; d++) {
//				ls[d] = p.values[d];
//				us[d] = p.values[d];
//			}
//			mrtree.Insert(ls, us, e);
//			uf.create(p.id % window_size);
//		}
//
//		window.addAll(in);
//
//		/**
//		 * Generate SC
//		 ** 
//		 **/
//
//		Set<UndirectedEdge> sc2sc = new HashSet<UndirectedEdge>();
//
//		for (DataPoint p_in : in) {
//			if (!p_in.visited) {
//				if (p_in.core_expired != -1) {
//					System.out.println("ERROR1");
//					return;
//				}
//
//				long range_query_start = System.nanoTime();
//				List<DataPoint> neighbors = findNeighbors_wIndex(p_in);
//				long range_query_end = System.nanoTime();
//				;
//				range_query += range_query_end - range_query_start;
//
//				if (neighbors.size() >= minPts) {
//
//					/* Init core p */
//
//					long findingSNC_start = System.nanoTime();
//
//					long init_core_start = System.nanoTime();
//					init_core_point(p_in, neighbors);
//					long init_core_end = System.nanoTime();
//					initCore += init_core_end - init_core_start;
//					findingSNC -= init_core_end - init_core_start;
//
//					Pair<DataPoint, List<DataPoint>> pair = new Pair(p_in,
//							neighbors);
//					Deque<Pair<DataPoint, List<DataPoint>>> stack = new ArrayDeque<Pair<DataPoint, List<DataPoint>>>();
//					stack.push(pair);
//
//					while (!stack.isEmpty()) {
//						Pair<DataPoint, List<DataPoint>> elem = stack.pop();
//						DataPoint p = elem.first;
//						for (DataPoint n : elem.second) {
//
//							/* Core visits visited NonCore */
//							if (n.visited && n.core_expired < time.getTime()) {
//
//								if (n.parentIfborder == null
//										|| p.core_expired > n.parentIfborder.core_expired)
//									n.parentIfborder = p;
//							}
//							/* Core visits visited Core */
//							else if (n.visited
//									&& n.core_expired >= time.getTime()
//									&& p.id != n.id) {
//								/* New Stride and Same CET */
//								if (n.timestamp == p.timestamp
//										&& n.core_expired == p.core_expired) {
//
//									long unionFind_start = System.nanoTime();
//									uf.union(n.id % window_size, p.id
//											% window_size);
//									long unionFind_end = System.nanoTime();
//									unionFind += unionFind_end
//											- unionFind_start;
//								}
//								/* Else */
//								else {
//
//									if (p.parentIfborder == null
//											|| p.parentIfborder.core_expired < n.core_expired) {
//										p.parentIfborder = n;
//									}
//									if (n.parentIfborder == null
//											|| n.parentIfborder.core_expired < p.core_expired) {
//										n.parentIfborder = p;
//									}
//
//									long unionFind_start = System.nanoTime();
//									sc2sc.add(new UndirectedEdge(uf.find(p.id
//											% window_size), uf.find(n.id
//											% window_size)));
//									long unionFind_end = System.nanoTime();
//									unionFind += unionFind_end
//											- unionFind_start;
//								}
//							}
//							/* Core visits unvisited unclassfied point */
//							else if (!n.visited) {
//
//								if (n.timestamp != p.timestamp) {
//									System.out.println("ERROR2");
//
//								}
//
//								range_query_start = System.nanoTime();
//								List<DataPoint> neighbors_of_n = findNeighbors_wIndex(n);
//								range_query_end = System.nanoTime();
//								;
//								range_query += range_query_end
//										- range_query_start;
//								findingSNC -= range_query_end
//										- range_query_start;
//
//								if (neighbors_of_n.size() >= minPts) {
//
//									/* Init Core */
//									init_core_start = System.nanoTime();
//									init_core_point(n, neighbors_of_n);
//									init_core_end = System.nanoTime();
//									initCore += init_core_end - init_core_start;
//									findingSNC -= init_core_end
//											- init_core_start;
//
//									if (n.core_expired == p.core_expired) {
//										long unionFind_start = System
//												.nanoTime();
//										uf.union(n.id % window_size, p.id
//												% window_size);
//										long unionFind_end = System.nanoTime();
//										unionFind += unionFind_end
//												- unionFind_start;
//									} else {
//
//										if (p.parentIfborder == null
//												|| p.parentIfborder.core_expired < n.core_expired) {
//											p.parentIfborder = n;
//										}
//
//										if (n.parentIfborder == null
//												|| n.parentIfborder.core_expired < p.core_expired) {
//											n.parentIfborder = p;
//										}
//
//										/* Option 1 */
//										long unionFind_start = System
//												.nanoTime();
//										sc2sc.add(new UndirectedEdge(uf
//												.find(p.id % window_size), uf
//												.find(n.id % window_size)));
//										long unionFind_end = System.nanoTime();
//										unionFind += unionFind_end
//												- unionFind_start;
//										/* Option 2 */
//										// //sc2sc.add(new Pair(p.id, n.id));
//									}
//
//									Pair<DataPoint, List<DataPoint>> pair_of_n = new Pair(
//											n, neighbors_of_n);
//									stack.push(pair_of_n);
//								} else {
//									n.visited = true;
//									for (DataPoint nn : neighbors_of_n) {
//										if (nn.core_expired >= time.getTime())// coreflag
//										{
//											if (n.parentIfborder == null
//													|| n.parentIfborder.core_expired < nn.core_expired) {
//												n.parentIfborder = nn;
//											}
//										}
//									}
//								}
//							}
//						}
//					}
//
//					long findingSNC_end = System.nanoTime();
//					findingSNC += findingSNC_end - findingSNC_start;
//
//				} else {
//					for (DataPoint n : neighbors) {
//						if (n.core_expired >= time.getTime())// coreflag
//						{
//							if (p_in.parentIfborder == null
//									|| n.core_expired > p_in.parentIfborder.core_expired)
//								p_in.parentIfborder = n;
//						}
//					}
//					p_in.visited = true;
//
//				}
//			}
//		}
//
//		long updMST_start = System.nanoTime();
//
//		/* Update LinkCut Tree */
//		Set<UndirectedEdge> reduced_sc2sc = new HashSet<UndirectedEdge>();
//		for (UndirectedEdge e : sc2sc) {
//			reduced_sc2sc.add(new UndirectedEdge(
//					uf.find(e.first % window_size), uf.find(e.second
//							% window_size)));
//		}
//
//		for (UndirectedEdge e : reduced_sc2sc) {
//
//			DataPoint REPp = window.get(Math.floorMod(e.first - offset,
//					window_size));
//			DataPoint REPq = window.get(Math.floorMod(e.second - offset,
//					window_size));
//
//			if (REPp.node == null) {
//				LCNode newNode = new LCNode();
//				REPp.node = newNode;
//				REPp.node.dp = REPp;
//				newNode.ts = REPp.core_expired;
//				HashSet<DataPoint> res = expiredTable.getOrDefault(
//						REPp.core_expired, new HashSet<DataPoint>());
//				res.add(REPp);
//				expiredTable.put(REPp.core_expired, res);
//
//			}
//
//			if (REPq.node == null) {
//				LCNode newNode = new LCNode();
//				REPq.node = newNode;
//				REPq.node.dp = REPq;
//				newNode.ts = REPq.core_expired;
//				HashSet<DataPoint> res = expiredTable.getOrDefault(
//						REPq.core_expired, new HashSet<DataPoint>());
//				res.add(REPq);
//				expiredTable.put(REPq.core_expired, res);
//
//			}
//
//			connect(REPp.node, REPq.node);
//		}
//
//		long updMST_end = System.nanoTime();
//		updMST += updMST_end - updMST_start;
//	}
//
//	public void BatchDecrement(List<DataPoint> out) {
//
//		long t1 = System.nanoTime();
//		double[] lb = new double[DIM];
//		double[] ub = new double[DIM];
//		for (DataPoint p : out) {
//			for (int i = 0; i < DIM; i++) {
//				lb[i] = p.values[i];
//				ub[i] = p.values[i];
//			}
//			Element<DataPoint> e = new Element<DataPoint>(p);
//			mrtree.delete(ub, lb, e);
//			uf.clear(p.id % window_size);
//		}
//
//		HashSet<DataPoint> res_temp = expiredTable.getOrDefault(time.getTime(),
//				new HashSet<DataPoint>());
//		expiredTable.remove(time.getTime());
//
//		HashSet<DataPoint> res = new HashSet<DataPoint>();
//		for (DataPoint ep : res_temp) {
//			int id_in_window = Math.floorMod(ep.id - offset, window_size);
//			res.add(window.get(id_in_window));
//		}
//
//		for (DataPoint ep : res) {
//			HashSet<DataPoint> R = edgeTable.get(ep);
//			if (R != null) {
//				edgeTable.remove(ep);
//				for (DataPoint nn : R) {
//					// MORE THAN TWO ===> CAUSE DELETE
//					LCT.cut(ep.node, nn.node);
//					if (!edgeTable.get(nn).remove(ep)) {
//						System.out.println("ERROR");
//					}
//				}
//
//			}
//		}
//
//		window = window.subList(out.size(), window.size());
//		offset += out.size();
//
//	}
//
//	public void init_core_point(DataPoint p, List<DataPoint> neighbors) {
//
//		// sort(neighbors);
//		// p.core_expired = neighbors.get(minPts-1).timestamp;
//
//		PriorityQueue<DataPoint> pq = new PriorityQueue<>(neighbors);
//		int k = minPts;
//		while (--k > 0) {
//			pq.poll();
//		}
//		p.core_expired = pq.peek().timestamp;
//
//		p.visited = true;
//		if (p.core_expired == -1) {
//			System.out.println("ERROR101");
//		}
//
//		/* Update ExpiredTable */
//		// HashSet<DataPoint> res = expiredTable.getOrDefault(p.core_expired,
//		// new HashSet<DataPoint>());
//		// res.add(p);
//		// expiredTable.put(p.core_expired, res);
//	}
//
//	double L2Distance(double[] pos1, double[] pos2) {
//		double distance = 0;
//		for (int i = 0; i < DIM; i++) {
//			distance += (pos1[i] - pos2[i]) * (pos1[i] - pos2[i]);
//		}
//
//		return Math.sqrt(distance);
//	}
//
//}
