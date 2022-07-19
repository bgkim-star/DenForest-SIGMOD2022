package denforest.linkcuttree;

import java.util.HashMap;

public class LinkCutTree {
	
	// left  depth >=  right depth 
	public boolean link(LCNode x, LCNode y) {
        if (connected(x, y))
        {
        	return false;
        }
        else{
        makeRoot(x);
        x.parent = y;
        return true;
        }
     }
    
	public void Label(LCNode x, int label, HashMap<Integer, Integer> labels) {
		if(x.dp.label == -1){
			x.dp.label = label;
			if(x.parent == null) { 	labels.put(label, label);  }
			else{ 
				Label(x.parent, label, labels); 
			}
		}
		else labels.put(label, labels.get(x.dp.label)); 
	}
	
	public void Label2(LCNode x, int label, HashMap<Integer, Integer> labels) {
		if(x.dp.label == -1){
			x.dp.label = label;
			if(x.parent == null) { 	labels.put(label, label);  }
			else{ Label2(x.parent, label, labels); }
		}
		else labels.put(label, labels.get(x.dp.label)); 
	}
	

	
    public boolean cut(LCNode x, LCNode y) {
        makeRoot(x);
        expose(y);
        if (y.right != x || x.left != null || x.right != null)
        {
        	return false;
        }
        else{

        	y.right.parent = null;
        	y.right = null;
    	    manageMin(y);
    	    return true;
        }
    }
    
    public LCNode FindParentInSplay(LCNode x)
    {
    	splay(x);
		x.push();
		if( x.right != null){
		
    		LCNode leftmost = x.right;
    		leftmost.push();
    		
    		while( leftmost.left != null)
    		{
    			leftmost = leftmost.left;
    			leftmost.push();
    		}
    		
    		return leftmost;
		}
		else{
    		return null;
		}
    }
    
    
    
    public Edge FindOldestEdge(LCNode x, LCNode y )
    {

     	LCNode lca ;
      expose(x);
      lca = expose(y);
        
      if ( x.parent == null ) return null;
      if(lca == null) return null;
        

    
        /** FIND OLDEST EDGE **/
        long m0, m1, m2;
        LCNode n0, n1, n2;
        
        if( lca != x && lca != y ){ 
        	
        	expose(lca);
        	n0 = lca; 
        	m0 = lca.ts;
        	
        	splay(x);
        	n1 = x.minNode;
        	m1 = x.minval;
        	
        	splay(y);
        	n2 = y.minNode;
        	m2 = y.minval;
        	
        	if( m0 <= m1 && m0 <= m2 ){
            	x.push();
        		LCNode rightmost = x;
        		while( rightmost.right != null)
        		{
        			rightmost = rightmost.right;
        			rightmost.push();
        		}
        		Edge e = new Edge(n0,rightmost,m0);
        		return e;
        	}
        	else if( m1 <= m0 && m1 <= m2){
        		LCNode n = FindParentInSplay(n1);
        		if( n == null ){
        			Edge e = new Edge(n1,n0,m1);
            		return e;
        		}
        		else{
	        		Edge e = new Edge(n1,n,m1);
	        		return e;
	        	}      		
        	}
        	else{
        		LCNode n = FindParentInSplay(n2);
        		if( n == null ){
        			Edge e = new Edge(n2,n0,m2);
            		return e;
        		}
        		else{
	        		Edge e = new Edge(n2,n,m2);
	        		return e;
	        	}
        	}
        }
        else if(lca == x){
        	
        	expose(x);
        	
           	n0 = lca; 
        	m0 = lca.ts;
        	
        	splay(y);

        	n1 = y.minNode;
        	m1 = y.minval;
        	
   	
        	if( m0 <= m1 ){
            	y.push();
        		LCNode rightmost = y;
        		while( rightmost.right != null)
        		{	
        			rightmost = rightmost.right;
        			rightmost.push();
        		}
        		Edge e = new Edge(n0,rightmost,m0);
        		return e;
        	}else{

        		LCNode n = FindParentInSplay(n1);
        		if( n == null ){
        			Edge e = new Edge(n1,n0,m1);
            		return e;
        		}
        		else{
	        		Edge e = new Edge(n1,n,m1);
	        		return e;
	        	}
        	}
        }
        else{
           	n0 = lca; 
        	m0 = lca.ts;
        	
        	splay(x);
        	n1 = x.minNode;
        	m1 = x.minval;
        	
           	if( m0 <= m1 ){
            	x.push();
        		LCNode rightmost = x;
        		while( rightmost.right != null)
        		{
        			rightmost = rightmost.right;
        			rightmost.push();
        		}
        		Edge e = new Edge(n0,rightmost,m0);
        		return e;
        	}
           	else{
        		LCNode n = FindParentInSplay(n1);
        		if( n == null ){
        			Edge e = new Edge(n1,n0,m1);
            		return e;
        		}
        		else{
	        		Edge e = new Edge(n1,n,m1);
	        		return e;
	        	}
        	}        	
        }
    }
        
     
    static void manageMin( LCNode p)
    {
    	LCNode l = p.left;
    	LCNode r = p.right;
    	long pval = p.ts;
    	long lval = l==null? Long.MAX_VALUE : l.minval; 
    	long rval = r==null? Long.MAX_VALUE : r.minval;
    	
   
    	
    	if( pval <= lval )
    	{
    		if( pval <= rval){
    			p.minNode = p;
    			p.minval = p.ts;
    		}
    		else{
    			p.minNode = r.minNode;
    			p.minval = r.minval;
    		}
    	}
    	else{
    		if( lval <= rval){
    			p.minNode = l.minNode;
    			p.minval = l.minval;
    		}
    		else{
    			p.minNode = r.minNode;
    			p.minval = r.minval;
    		}
    	}
    }
 
    
    public void traversal(LCNode n) {
    	
    	System.out.println(n.ts);
    	if( n.left != null ){
    		System.out.println("left");
    		traversal(n.left);
    		System.out.println("end");
    	}
    		        
    	if( n.right != null ){
    		System.out.println("right");
    		traversal(n.right);
    		System.out.println("end");
    	}
    }
    
    
    void connect(LCNode ch, LCNode p, Boolean isLeftChild) {
        if (ch != null)
            ch.parent = p;
        if (isLeftChild != null) {
            if (isLeftChild){     
        		p.left = ch;
        	    manageMin(p);
        	}
            else{
            	p.right = ch;
        	    manageMin(p);
            }
       }
    }
    
    void rotate(LCNode x) {
        LCNode p = x.parent;
        LCNode g = p.parent;
        boolean isRootP = p.isRoot();
        boolean leftChildX = (x == p.left);

        connect(leftChildX ? x.right : x.left, p, leftChildX);
        connect(p, x, !leftChildX);
        connect(x, g, !isRootP ? p == g.left : null);
    }
    
    
    void splay(LCNode x) {
        while (!x.isRoot()) {
            LCNode p = x.parent;
            LCNode g = p.parent;
            if (!p.isRoot())
                g.push();
            p.push();
            x.push();
            if (!p.isRoot())
                rotate((x == p.left) == (p == g.left) ? p : x);
            rotate(x);
        }
      x.push();
    }
    
    public LCNode lca(LCNode cur, LCNode nxt) {
        expose(cur);
        LCNode lca = expose(nxt);
        if ( cur.parent == null ) return null;
        else return lca; 
    }

   
    LCNode expose(LCNode x) {
        LCNode last = null;
        for (LCNode y = x; y != null; y = y.parent) {
            splay(y);
            y.left = last;
    	    manageMin(y);
            last = y;
        }
        splay(x);
        return last;
    }
    
    public void makeRoot(LCNode x) {
        expose(x);
        x.revert = !x.revert;
    }
    
    public boolean connected(LCNode x, LCNode y) {
        if (x == y)
            return true;
        expose(x);
        // now x.parent is null
        expose(y);
        return x.parent != null;
    }
    
}
