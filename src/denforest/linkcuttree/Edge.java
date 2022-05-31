package denforest.linkcuttree;

public class Edge {
	public LCNode v1 = null;
	public LCNode v2 = null;
	public Long ts = Long.MAX_VALUE;
	public Edge(LCNode v1, LCNode v2, Long ts)
	{
		this.v1 = v1; 
		this.v2 = v2;
		this.ts = ts;
	}
}
