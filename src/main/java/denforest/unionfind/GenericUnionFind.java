package denforest.unionfind;

import java.util.HashMap;

public class GenericUnionFind<T> {
	private HashMap<T,T> id;
	private HashMap<T,Integer> sz;
	
	public GenericUnionFind()
	{
		id = new HashMap<T,T>();
		sz = new HashMap<T,Integer>();
	}
	
	public void create(T i) {
		id.put(i, i);
		sz.put(i, 1);
	}
	
	public void union(T p, T q) {
		T pRoot = find(p);
		T qRoot = find(q);
		//size balancing
		if (sz.get(pRoot) <= sz.get(qRoot)) {
			id.put(pRoot,qRoot);
			sz.put(qRoot, sz.get(qRoot)+sz.get(pRoot));
		}
		else {
			id.put(qRoot, pRoot);
			sz.put(pRoot, sz.get(pRoot) + sz.get(qRoot));
		}
	}
	
	public T find(T i) {
		if( id.containsKey(i)){
		if (id.get(i) == i)
			return i;
		//path compression
		T parent = find(id.get(i));
		id.put(i,parent);
		return parent;
		}
		else{
			return null;
		}
	}
	
	public boolean connected(T p, T q) {
		return find(p) == find(q);
	}
	
	public int size() {
		return id.size();
	}
}
