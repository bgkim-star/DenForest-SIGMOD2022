package denforest.datapoint;

import denforest.linkcuttree.*;

public class DataPoint implements Comparable<DataPoint> {

	public int id ;
	public double[] values;
	
	public long timestamp; 
	public long core_expired = -1;
	
	public DataPoint parentIfborder = null;
	public LCNode node = null;
	public int label = -1;
	
	public boolean visited = false;
	public boolean covered = false; 

	public DataPoint(int i, double[] vals, long ts)
	{
		id = i;
		values = vals;
		timestamp = ts;
	}
	
	public void init()
	{
		core_expired = -1;
		parentIfborder =null;
		visited = false;
		covered = false;
		node = null;
		label = -1;
	}
	
	public DataPoint(int id, double[] values)
	{
		this.id = id;
		this.values = values;
	
	}
	
	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}
	
	@Override
	 public int compareTo(DataPoint dp) {
	      if (this.timestamp < dp.timestamp) return 1;
	      else if (this.timestamp > dp.timestamp) return - 1;
	      else return 0;
	}


}
