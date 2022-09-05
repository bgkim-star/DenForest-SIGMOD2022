package denforest.basicRtree;


import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Node <T>  {


	
	boolean isLeafNode = false;
	boolean isLeafEntry = false;
	int checkCount = 0 ; 
	int entryCount = 0 ;
	int dim;
	int height = -1;
	double[] ub  = null;
	double[] lb  = null;
	Node<T>[] children = null;
	Element<T> element = null;
	
	
	/* Node with one element */
	@SuppressWarnings("unchecked")
	public Node(int maxNodeEntries, int dim, double[] Ls, double[] Us, Element<T> e) {
		this.dim = dim;
		children = new Node[maxNodeEntries];
		ub = new double[dim];
		lb = new double[dim];
		ub = Us.clone();
		lb = Ls.clone();
		element = e;
	}
	@SuppressWarnings("unchecked")
	public Node(int maxNodeEntries, int dim) {
		this.dim = dim;
		children = new Node[maxNodeEntries];
		ub = new double[dim];
		Arrays.fill(ub, Double.MIN_VALUE);
		lb = new double[dim];		
		Arrays.fill(lb, Double.MAX_VALUE);
	}
	
	
	void addEntry(Node<T> child) {
		
		children[entryCount] = child;
		double[] clb = child.lb;
		double[] cub = child.ub;
		
		
		for( int i = 0 ; i < dim ; i++ )
		{
			if( lb[i] > clb[i] ) lb[i] = clb[i];
			if( ub[i] < cub[i] ) ub[i] = cub[i];
		}
		entryCount++;
	}

	
	// Return the index of the found entry, or -1 if not found
	List<Element<T>> SearchNodes(double[] ub, double[] lb) {
				
		List<Element<T>> result = new LinkedList<Element<T>>();
		for (int i = 0; i < entryCount; i++) {
			boolean flag = false;
			for( int j = 0; j < dim ; j++)
			{
				if( (children[i].ub[j] < lb[j]) || (children[i].lb[j] > ub[j]) )
				{
					flag = false ;
					break;
				}
				else 
				{
					flag = true;
				}
			}
						if( flag ){ result.add(children[i].element);}
		//	 result.add(children[i].element);
		}
				
		return result;
		
	}
	

// delete i th entry 
	void deleteEntry(int i) {
		int lastIndex = --entryCount;
		
		if (i != lastIndex) {
			children[i] = children[lastIndex];
		}
		// adjust the MBR
	}

	boolean checkIfInfluencedBy(double[] ub, double[] lb) {
		for( int i = 0 ; i < dim ; i++)
		{
			if( lb[i] == this.lb[i]  ||  ub[i] == this.ub[i] )
			{
				return true;
			}
		}
		return false;
		
	}

	void recalculateMBR() {
		
		
		double[] clb = children[0].lb;
		double[] cub = children[0].ub;
		
		for( int i = 0 ; i < dim ; i++ )
		{
			this.lb[i] = clb[i];
			this.ub[i] = cub[i];
		}
		
		for (int j = 1; j < entryCount; j++) {
			clb = children[j].lb;
			cub = children[j].ub;
			for( int i = 0 ; i < dim ; i++ )
			{
				if( this.lb[i] > clb[i] ) this.lb[i] = clb[i];
				if( this.ub[i] < cub[i] ) this.ub[i] = cub[i];
			}
		}
	}

	
	/**
	 * eliminate null entries, move all entries to the start of the source node
	 */
	void reorganize(int maxNodeEntries) {
		int position = 0;
		for (int index = 0; index < maxNodeEntries; index++) {
			if(children[index] != null)
			{
				children[position] = children[index]; 
				if( index != position ) children[index] = null;
				position++;
			}
		}
	}

	public int getEntryCount() {
		return entryCount;
	}

	public Node<T> getChild(int index) {
		if (index < entryCount) {
			return children[index];
		}
		return null;
	}

		
	
	
}