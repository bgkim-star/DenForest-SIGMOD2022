package denforest.datapoint;

public class Timestamp {
	private long tick;
	
	public Timestamp(long t)
	{
		tick = t;
	}
	
	public Timestamp()
	{
		
	}
	
	public void increment()
	{
		tick++;
	}
	
	public long getTime()
	{
		return tick;
	}
	
	public void setTime(long ts)
	{
		tick = ts;
	}
}