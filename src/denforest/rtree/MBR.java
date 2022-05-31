package denforest.rtree;


public class MBR {
	double[] lower = null;
	double[] upper = null;
	
	public MBR(int dim)
	{
		lower = new double[dim];
		upper = new double[dim];
		for(int i = 0 ; i < dim ; i++)
		{
			lower[i] = Double.MAX_VALUE;
			upper[i] = Double.MIN_VALUE;
		}
		
	}
	
	public double getArea()
	{
		double size = 1;
		for(int i = 0 ; i < lower.length ; i++)
		{
			size = size*(upper[i]-lower[i]);
		}
		return size;
	}
	
	public void SetL(double[] Ls)
	{
		for(int i = 0 ; i < lower.length ; i++)
		{
			lower[i] = Ls[i];
		}
	}
	public void SetU(double[] Us)
	{
		for(int i = 0 ; i < lower.length ; i++)
		{
			upper[i] = Us[i];
		}
	}
}