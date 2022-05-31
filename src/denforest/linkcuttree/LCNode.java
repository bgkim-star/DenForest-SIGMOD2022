package denforest.linkcuttree;

import denforest.datapoint.DataPoint;

public class LCNode {
		
		public LCNode left;
        public LCNode right;
        public LCNode parent;
        boolean revert;
        public boolean isedge = false;
        
        public long ts = 0;
        public long minval= Long.MAX_VALUE;
        public LCNode minNode = null;
        public DataPoint dp = null;
      
        
        boolean isRoot() {
            return parent == null || (parent.left != this && parent.right != this);
        }
        
        void push() {
            if (revert) {
            
                revert = false;
                LCNode t = left;
                left = right;
                right = t;
                if (left != null)   left.revert = !left.revert;
                if (right != null)  right.revert = !right.revert;
            }
        }
    }