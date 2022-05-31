package denforest.rtree;


public class Element<T> 
{
	public T p;
	public Element(T in)
	{
		this.p = in;
	}
	
	public Element()
	{
		
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj == null) return false;
		else if(!Element.class.isAssignableFrom(obj.getClass())) return false;
		final Element<T> e = (Element<T>) obj;

		return p.equals(e.p);
	}
}
