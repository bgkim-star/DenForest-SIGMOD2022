package denforest.rtree;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;



public class mRtree<T> {

	private final static int DEFAULT_MAX_NODE_ENTRIES = 50;
	private final static int DEFAULT_MIN_NODE_ENTRIES = 20;

	int dim;
	int maxNodeEntries;
	int minNodeEntries;
	
	
	public long m1=0;
	public long m2=0;
	public long m3=0;
	public long m4=0;
	
	
	
	public long ts,te;

	private final static int ENTRY_STATUS_ASSIGNED_TO_OLD = 1;
	private final static int ENTRY_STATUS_UNASSIGNED = 0;
	private final static int ENTRY_STATUS_ASSIGNED_TO_NEW = 2;

	private byte[] entryStatus = null;
	private byte[] initialEntryStatus = null;

	
	Node<T> root = null;


	private Deque<Node<T>> deque = new ArrayDeque<Node<T>>();
	private Deque<Integer> dequeID = new ArrayDeque<Integer>();
	private Deque<Node<T>> deleteNode = new ArrayDeque<Node<T>>();
	public List<Element<T>> Results = new ArrayList<Element<T>>(); 

	public int size = 0;
	public int height = 0;


	public mRtree(int dim, int maxN, int minN) {
		this.dim = dim;
		if (maxN == -1 && minN == -1) {

			maxNodeEntries = DEFAULT_MAX_NODE_ENTRIES;
			minNodeEntries = DEFAULT_MIN_NODE_ENTRIES;
		} else {
			maxNodeEntries = maxN;
			minNodeEntries = minN;
		}
		entryStatus = new byte[maxNodeEntries];
		initialEntryStatus = new byte[maxNodeEntries];

		for (int i = 0; i < maxNodeEntries; i++) {
			initialEntryStatus[i] = ENTRY_STATUS_UNASSIGNED;
		}

		root = new Node<T>(maxNodeEntries, dim);
		root.height = 0;
		root.isLeafNode = true;

	}


	public int  height() throws IOException
	{
		return height(root);
	}

	
	public int  height (Node<T> cur) throws IOException
	{
		if(cur.isLeafNode)
		{
			return 1;
		}
		else
		{
			int max = -1;
			for( int i = 0 ; i < cur.entryCount; i++){
				int h = height(cur.children[i]);
				if( max <  h )  max = h; 
			}
			return max+1;
		}	
	}

	
	
	

	private Node<T> ChooseNode(double[] ub, double[] lb, int height) {

		deque.clear();

		Node<T> cur = root;

		
		while (true) {
			if (cur == null) {
				System.out.println("ChooseNode ERROR");
			}

			if (cur.height == height+1 ) {
				return cur;
			}
			
			deque.push(cur);
			
			double leastEnlargement = enlargement(cur.children[0].ub, cur.children[0].lb, ub, lb);

			int index = 0; // index of rectangle in subtree
			for (int i = 1; i < cur.entryCount; i++) {

				double tempEnlargement = enlargement(cur.children[i].ub, cur.children[i].lb, ub, lb);

				if ((tempEnlargement < leastEnlargement)
						|| ((tempEnlargement == leastEnlargement) && 
						(getArea(cur.children[i].ub, cur.children[i].lb) < getArea(cur.children[index].ub, cur.children[index].lb)))) {
					index = i;
					leastEnlargement = tempEnlargement;
				}
			}

			cur = cur.children[index];
		}
	}

	
	
	
	
	public void ReInsert(Node<T> l)
	{
		Node<T> TargetNode = ChooseNode(l.ub, l.lb, l.height);
		Node<T> splitedNode = null;
		if (TargetNode.entryCount < maxNodeEntries) {
			TargetNode.addEntry(l);
		} else {
			splitedNode = splitNode(TargetNode, l);
		}
		Node<T> newNode = adjustTree(TargetNode, splitedNode);
		if (newNode != null) {
			Node<T> newRoot = new Node<T>(maxNodeEntries, dim);
			newRoot.height = root.height+1;
			newRoot.addEntry(newNode);
			newRoot.addEntry(root);
			root = newRoot;
		}
		
	}
	
	public void Insert(double[] Ls, double[] Us, Element<T> e) {

		

		Node<T> LeafEntry = new Node<T>(1, dim, Ls, Us, e);
		LeafEntry.isLeafEntry = true;		
		Node<T> LeafNode = ChooseLeaf(LeafEntry.ub, LeafEntry.lb);
		
		Node<T> newLeaf = null;
		if (LeafNode.entryCount < maxNodeEntries) {
			LeafNode.addEntry(LeafEntry);
		} else {
			newLeaf = splitNode(LeafNode, LeafEntry);
		}

		Node<T> newNode = adjustTree(LeafNode, newLeaf);
		
		
		if (newNode != null) {
			Node<T> newRoot = new Node<T>(maxNodeEntries, dim);
			newRoot.height = root.height+1;
			newRoot.addEntry(newNode);
			newRoot.addEntry(root);
			root = newRoot;
		}
		
		this.size++;

	}

	public boolean find(double[] ub, double[] lb, Element<T> e) {
		deque.clear();
		dequeID.clear();
		deque.push(root);

		return search(ub, lb ,e); 
	}
	
	
	private boolean search(double[] ub, double[] lb, Element<T> e) {
		Node<T> n = deque.peek();
		if (!n.isLeafNode) {
			for (int i = 0; i < n.entryCount; i++) {
				boolean contains = true;
				for (int k = 0; k < dim; k++) {
					if (lb[k] < n.lb[k] || ub[k] > n.ub[k]) {
						contains = false;
						break;
					}
				}

				if (contains) {
					deque.push(n.children[i]);
					dequeID.push(i);
					if (search(ub, lb, e)) {
						return true;
					}
					deque.pop();
					dequeID.pop();
				}				

			}
			return false;
		} else {
			for (int i = 0; i < n.entryCount; i++) {

				if (e.equals(n.children[i].element)) {
					dequeID.push(i);
					return true;
				}
			}
			return false;
		}
		
		

	}
	
	private void subBFS( )
	{
		if(deque.isEmpty()) return;
		
		Node<T> n = deque.remove();
		if(n.entryCount<8)System.out.println("HEIGHT: "+n.height+" Entry#:" + n.entryCount);
		if (!n.isLeafNode) {
			for (int i = 0; i < n.entryCount; i++) {
				deque.add(n.children[i]);		
			}	
		}
		subBFS();
	}

	public void BFSearch() {
		
		deque.clear();
		deque.add(root);
		subBFS();			
	}

	
	public boolean delete(double[] ub, double[] lb , Element<T> e) {

		deleteNode.clear();
		deque.clear();
		dequeID.clear();
		deque.push(root);
		

		if(  search(ub, lb,e) )
		{
				
			DeleteAndCondenseTree();
			size--;
					
		 
			while (root.entryCount == 1 && root.height > 0) {
				root = root.children[0];
			}
	
			if (size == 0) {
				double[] emptyLs = new double[dim];
				double[] emptyUs = new double[dim];
				for (int xxx = 0 ; xxx < dim ; xxx++ )
				{
					emptyLs[xxx] = Double.MAX_VALUE;
					emptyUs[xxx] = Double.MIN_VALUE;
				}
				
				root.lb = emptyLs;
				root.ub = emptyUs;
		
			}
	
			return true;
			
		}
		else
		{
			System.out.println("ERROR");
			return false;
		}

	}
	
	
	private void subdelete(Node<T> l, boolean check)
	{
		if( deque.isEmpty() )
		{
			if( check )
			{
				l.recalculateMBR();
			}
			 
			return;
		}
		
		Node<T> cur = deque.pop();
		int index = dequeID.pop();
		boolean check2 = cur.checkIfInfluencedBy(l.ub, l.lb );
		
		if( check )
		{
			l.recalculateMBR();
		}
		
		if(l.entryCount < minNodeEntries)
		{
			for(int j = 0 ; j< l.entryCount ; j++)
			{
				deleteNode.add(l.children[j]);
			}
			cur.deleteEntry(index);
			subdelete(cur, check2);
		}
		else
		{
			subdelete(cur, check2);
		}		
	}
	
	private void DeleteAndCondenseTree() {
		
		
		
		Node<T> cur = deque.pop();
		int deleteID = dequeID.pop();
		boolean check = cur.checkIfInfluencedBy(cur.children[deleteID].ub,cur.children[deleteID].lb);
		cur.deleteEntry(deleteID);
		subdelete(cur, check);
		
		for(Node<T> e :  deleteNode)
		{
			ReInsert(e);
		}
		
	}

	
	


	private Node<T> splitNode(Node<T> oldNode, Node<T> newEntry) {

		System.arraycopy(initialEntryStatus, 0, entryStatus, 0, maxNodeEntries);
		Node<T> newNode = null;
		newNode = new Node<T>(maxNodeEntries, dim);
		if (oldNode.isLeafNode) {
			newNode.isLeafNode = true;
			newNode.height = 0;
		}
		else
		{
			newNode.height = oldNode.height;
		}

		pickSeeds(oldNode, newEntry, newNode); 
	

	
		while (oldNode.entryCount + newNode.entryCount < maxNodeEntries + 1) {
			if (maxNodeEntries + 1 - newNode.entryCount == minNodeEntries) {

				for (int i = 0; i < maxNodeEntries; i++) {
					if (entryStatus[i] == ENTRY_STATUS_UNASSIGNED) {
						entryStatus[i] = ENTRY_STATUS_ASSIGNED_TO_OLD;

						for (int j = 0; j < dim; j++) {
							if (oldNode.lb[j] > oldNode.children[i].lb[j])
								oldNode.lb[j] = oldNode.children[i].lb[j];
							if (oldNode.ub[j] < oldNode.children[i].ub[j])
								oldNode.ub[j] = oldNode.children[i].ub[j];
						}

						oldNode.entryCount++;
					}
				}
				break;
			}
			if (maxNodeEntries + 1 - oldNode.entryCount == minNodeEntries) {

				for (int i = 0; i < maxNodeEntries; i++) {
					if (entryStatus[i] == ENTRY_STATUS_UNASSIGNED) {
						entryStatus[i] = ENTRY_STATUS_ASSIGNED_TO_NEW;
						newNode.addEntry(oldNode.children[i]);
						oldNode.children[i] = null;

					}
				}
				break;
			}

			pickNext(oldNode, newNode);
		}

		
		oldNode.reorganize(maxNodeEntries);
				
		return newNode;
	}

	
	private void pickSeeds(Node<T> oldN, Node<T> newEntry, Node<T> newN) {
		
		double maxNormalizedSeparation = -1;
		int highestLowIndex = -1;
		int lowestHighIndex = -1;

		double[] olb = oldN.lb;
		double[] oub = oldN.ub;
		double[] blb = newEntry.lb;
		double[] bub = newEntry.ub;
		for (int i = 0; i < dim; i++) {
			if (olb[i] > blb[i])
				olb[i] = blb[i];
			if (oub[i] < bub[i])
				oub[i] = bub[i];
		}

		for (int z = 0; z < dim; z++) {

			double maxLen = bub[z] - blb[z];
			double tempHighestLow = blb[z];
			int tempHighestLowIndex = -1;

			double tempLowestHigh = bub[z];
			int tempLowestHighIndex = -1;

			for (int i = 0; i < oldN.entryCount; i++) {
				double tempLow = oldN.children[i].lb[z];
				double tempHigh = oldN.children[i].ub[z];
				if (tempLow >= tempHighestLow) {
					tempHighestLow = tempLow;
					tempHighestLowIndex = i;
				} else if (tempHigh <= tempLowestHigh) {
					tempLowestHigh = tempHigh;
					tempLowestHighIndex = i;
				}
				double normalizedSeparation = maxLen == 0 ? -1 	: (tempHighestLow - tempLowestHigh) / maxLen;
				if (normalizedSeparation >= maxNormalizedSeparation) {
					highestLowIndex = tempHighestLowIndex;
					lowestHighIndex = tempLowestHighIndex;
					maxNormalizedSeparation = normalizedSeparation;
				}
			}
		}

		
		if (highestLowIndex == lowestHighIndex) {
			lowestHighIndex = 0;
			highestLowIndex = -1;
			System.out.println("pickSeeds ERROR");
		}
		
		

		if (highestLowIndex == -1) {
			newN.addEntry(newEntry);
		} else {
			newN.addEntry(oldN.children[highestLowIndex]);
			oldN.children[highestLowIndex] = newEntry;
		}

		if (lowestHighIndex == -1) {
			lowestHighIndex = highestLowIndex;
		}

		entryStatus[lowestHighIndex] = ENTRY_STATUS_ASSIGNED_TO_OLD;
		oldN.entryCount = 1;

		for (int i = 0; i < dim; i++) {
			oldN.lb[i] = oldN.children[lowestHighIndex].lb[i];
			oldN.ub[i] = oldN.children[lowestHighIndex].ub[i];
		}
	}

	private int pickNext(Node<T> oldNode, Node<T> newNode) {
		Double maxDifference = Double.NEGATIVE_INFINITY;
		int next = 0;
		int nextGroup = 0;
		if (oldNode.isLeafEntry) {
			System.out.println("pickNext ERROR!");
		}

		maxDifference = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < maxNodeEntries; i++) {
			if (entryStatus[i] == ENTRY_STATUS_UNASSIGNED) {

				double oldNodeInc = enlargement(oldNode.ub,oldNode.lb,	oldNode.children[i].ub, oldNode.children[i].lb);
				double newNodeInc = enlargement(newNode.ub, newNode.lb, oldNode.children[i].ub, oldNode.children[i].lb);

				double difference = Math.abs(oldNodeInc - newNodeInc);

				if (difference > maxDifference) {
					next = i;

					if (oldNodeInc < newNodeInc) {
						nextGroup = 0; // old
					} else if (newNodeInc < oldNodeInc) {
						nextGroup = 1; // new
					} else if (getArea(oldNode.ub, oldNode.lb) < getArea(newNode.ub, newNode.lb)) {
						nextGroup = 0; // old
					} else if (getArea(oldNode.ub, oldNode.lb) > getArea(newNode.ub, newNode.lb)) {
						nextGroup = 1;// new
					} else if (newNode.entryCount < maxNodeEntries / 2) {
						nextGroup = 0;
					} else {
						nextGroup = 1;
					}
					maxDifference = difference;
				}
			}
		}

		;

		if (nextGroup == 0) {
			for (int j = 0; j < dim; j++) {
				if (oldNode.lb[j] > oldNode.children[next].lb[j])
					oldNode.lb[j] = oldNode.children[next].lb[j];
				if (oldNode.ub[j] < oldNode.children[next].ub[j])
					oldNode.ub[j] = oldNode.children[next].ub[j];
			}
			oldNode.entryCount++;
			entryStatus[next] = ENTRY_STATUS_ASSIGNED_TO_OLD;

		} else {
			// move to new node.
			newNode.addEntry(oldNode.children[next]);
			oldNode.children[next] = null;
			entryStatus[next] = ENTRY_STATUS_ASSIGNED_TO_NEW;
		}

		return next;
	}



	public List<Element<T>> intersects(double[] ub, double[] lb, Node<T> cur) {
	
		boolean flag = false;
		for( int j = 0; j < dim ; j++)
		{
			if( cur.ub[j] < lb[j] || cur.lb[j] > ub[j] )
			{
				
				flag = false ;
				break;
			}
			else 
			{

				flag = true;
			}
		}
		
		if( flag ){
			if (cur.isLeafNode) {
					Results.addAll(cur.SearchNodes(ub,lb));
			}
			else{
				for (int i = 0; i < cur.entryCount; i++) {
					
					intersects(ub,lb, cur.children[i]);
				}
			}
		}
		
		return Results;
	}
	
	
	public List<Element<T>> intersects(double[] ub, double[] lb) {
		Results.clear();
		return intersects(ub, lb,root);
	}
	
	

	private double enlargement(double[] bub, double[] blb,  double[] nub, double[] nlb) {
	
		
		double size1 = 1;
		double size2 = 1;
		

		for (int i = 0; i < dim; i++) {
			size2 = size2 * (Math.max(bub[i], nub[i]) - Math.min(blb[i], nlb[i]));
			size1 = size1 * (bub[i] - blb[i]);
		}
		
	
		return(size2-size1);
	}

	public double getArea(double[] upper, double[] lower)
	{
		double size = 1;
		for(int i = 0 ; i < lower.length ; i++)
		{
			size = size*(upper[i]-lower[i]);
		}
		return size;
	}
	private Node<T> ChooseLeaf(double[] ub, double[] lb) {

		deque.clear();
		Node<T> cur = root;
		while (true) {
			if (cur == null) {
				System.out.println("ChooseLeaf ERROR");
			}
			if (cur.isLeafNode) {
				return cur;
			}
			deque.push(cur);
			double leastEnlargement = enlargement(cur.children[0].ub, cur.children[0].lb, ub, lb);
			int index = 0; // index of rectangle in subtree
			for (int i = 1; i < cur.entryCount; i++) {
				double tempEnlargement = enlargement(cur.children[i].ub, cur.children[i].lb, ub, lb);
				if ((tempEnlargement < leastEnlargement) || 
						((tempEnlargement == leastEnlargement) && 
							(getArea(cur.children[i].ub, cur.children[i].lb) < getArea(cur.children[index].ub, cur.children[index].lb)))){
					index = i;
					leastEnlargement = tempEnlargement;
				}
			}

			cur = cur.children[index];
		}
	}


	private Node<T> adjustTree(Node<T> oldNode, Node<T> newNode) {
		if (deque.isEmpty()) {
			return newNode;
		} else {
			Node<T> parent = deque.pop();


			parent.recalculateMBR();

			Node<T> newNode2 = null;
			if (newNode != null) {
				if (parent.entryCount < maxNodeEntries) {
					parent.addEntry(newNode);
				} else {
					newNode2 = splitNode(parent, newNode);
				}
			}

			return adjustTree(parent, newNode2);

		}
	}

	
}