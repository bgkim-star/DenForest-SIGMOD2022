package denforest;

import java.util.Objects;


public class UndirectedEdge{
	int first; 
		int second; 
		public UndirectedEdge(int ta, int tb)
		{
			if( ta >= tb){
			first= ta;
			second = tb;
			}
			else{
				first = tb ;
				second =ta ;
			}
			
		}
		
		@Override
		public boolean equals(Object o) {
		    // self check
		    if (this == o)
		        return true;
		    // null check
		    if (o == null)
		        return false;
		    // type check and cast
		    if (getClass() != o.getClass())
		        return false;
		    UndirectedEdge edge = (UndirectedEdge) o;
		    // field comparison
		    return ((first == edge.first) && (second == edge.second));
		
		}
		
		 @Override
		    public int hashCode() {
		        return Objects.hash(first, second);
		    }
		
	}