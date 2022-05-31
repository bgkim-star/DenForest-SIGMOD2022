package denforest.unionfind;

import java.util.HashMap;

public class UnionFind {
	private HashMap<Integer,Integer> id;
	private HashMap<Integer,Integer> sz;
	
	public UnionFind()
	{
		id = new HashMap<Integer,Integer>();
		sz = new HashMap<Integer,Integer>();
	}
	
	public void create(int i) {
		id.put(i, i);
		sz.put(i, 1);
	}
	
	public void union(int p, int q) {
		int pRoot = find(p);
		int qRoot = find(q);
		//size balancing
		
		if( pRoot == qRoot) return;
		if (sz.get(pRoot) <= sz.get(qRoot)) {
			id.put(pRoot,qRoot);
			sz.put(qRoot, sz.get(qRoot)+sz.get(pRoot));
		}
		else {
			id.put(qRoot, pRoot);
			sz.put(pRoot, sz.get(pRoot) + sz.get(qRoot));
		}
	}
	
	public int find(int i) {
		if( id.containsKey(i)){
		if (id.get(i) == i)
			return i;
		//path compression
		int parent = find(id.get(i));
		id.put(i,parent);
		return parent;
		}
		else{
			return -1;
		}
	}
	
	public boolean connected(int p, int q) {
		return find(p) == find(q);
	}
	
	public int size() {
		return id.size();
	}
}
