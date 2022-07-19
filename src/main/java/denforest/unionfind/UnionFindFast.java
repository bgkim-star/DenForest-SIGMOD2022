package denforest.unionfind;

import java.util.HashMap;

public class UnionFindFast {
	private int[] id ;
	private int[] sz ;
	
	public UnionFindFast(int windowsize)
	{
		id = new int[windowsize];
		sz = new int[windowsize];
		for( int i = 0 ; i < windowsize; i++){
			id[i] = -1;
			sz[i] = 0;
		}
	}
	
	public void create(int i) {
		id[i] = i;
		sz[i] = 1;
	}
	
	public void union(int p, int q) {
		int pRoot = find(p);
		int qRoot = find(q);
		//size balancing
		
		if( pRoot == qRoot) return;
		if (sz[pRoot] <= sz[qRoot]) {
			id[pRoot] = qRoot;
			sz[qRoot] = sz[qRoot]+sz[pRoot];
		}
		else {
			id[qRoot] = pRoot;
			sz[pRoot] =sz[pRoot] + sz[qRoot];
		}
	}
	
	public int find(int i) {
		if( id[i] != -1  ){
		if (id[i] == i)
			return i;
		//path compression
		int parent = find(id[i]);
		id[i]= parent;
		return parent;
		}
		else{
			return -1;
		}
	}
	
	public void clear(int i) {
		id[i] = -1;
		sz[i] = 0;
	}
	
	public boolean connected(int p, int q) {
		return find(p) == find(q);
	}
	
//	public int size() {
//		return id.size();
//	}
}
