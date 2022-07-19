package example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import denforest.DenForestStrided;
import denforest.datapoint.DataPoint;
import denforest.datapoint.Timestamp;



public class denforest_optimized_test {
	static String path;	
	static BufferedReader br = null;
	static String[] nnames = null;
	static int dim;
	static int window;
	static int stride;
	static int slide; 
	static int option;
	static int pminpts;
	static double peps; 
	static int iter = 0;
	
	public static void main(String[] args) {

		path = args[0];
		option = Integer.parseInt(args[1]); // select datase:  1. DTG, 2.GeoLife, 3.IRIS, 4.Household
		int pminpts = Integer.parseInt(args[2]); // density threshold: minPts 
		double peps = Double.parseDouble(args[3]); // distance threshold: epsilon
		window =  Integer.parseInt(args[4]); // window size
		stride =  Integer.parseInt(args[5]);  // stride size
		slide = Integer.parseInt(args[6]); // #slide 
		String filename = args[7];	

		
		System.out.println("[Example.DenforestOpt] Run DenForest...");
		System.out.print("[Example.DenforestOpt] Read the dataset...");

		
		System.out.println("[Example.DenforestOpt] Load Data");
		
		
		List<DataPoint> dataset =  load_data(option); 
	

		
		Timestamp time = new Timestamp(); // current time -  window_size ; ex) starting from zero; 	
		DenForestStrided batch_optimized_denforest = new DenForestStrided(dim, peps, pminpts, time); // DenForest
		
		System.out.println("[Example.DenforestOpt] Fill the initial window...");
		
		
		// Fill the window by stride
		for( int i = 0 ; i < window ; i+=stride)
		{
			List<DataPoint> in = dataset.subList(i,i+stride);
			batch_optimized_denforest.batch_insert(in);;		// insert multiple points in the stride (batch_optimized_insert)
		}
		
			
		System.out.println("[Example.DenforestOpt] Update the strides...");
				
			
		for( int i = 0 ; i < stride*slide ; i+=stride)
		{
			
			List<DataPoint> out = dataset.subList(i,i+stride); // old data points
			batch_optimized_denforest.batch_delete(out);  // delete multiple points in the stride (batch_optimized_delete)
			
			time.increment(); // increment time
			
			List<DataPoint> in = dataset.subList(window+i,window+i+stride);  // new data points
			batch_optimized_denforest.batch_insert(in);  // insert multiple points in the stride  (batch_optimized_insert)
			
		}
		
		System.out.println("[Example.DenforestOpt] Complete!");
		
		
		
		// Produce clustering result
		for(DataPoint p : dataset.subList(stride*slide,window+stride*slide)) p.label=-1;
		int resDenForest[]  = batch_optimized_denforest.labelAndReturn(); // DenForest's clustering result;
		
		
				
	}
	
	
	
	private static String getData() {
		String line = null;
		try {
			line = br.readLine();
			if (line != null && !line.equals("")) {
			}
			else{
				return null;
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		iter++;
		return line;
	}
	
	private static List<DataPoint> load_data(int option){
		
		try {
			List<DataPoint> dataset = new ArrayList<DataPoint>();
			
						
			if( option == 1){ /* DTG */ 
				dim = 2; 
				br = new BufferedReader(new FileReader(path+"/DTG_sample.csv"));
				
				System.out.println("[DTG]");

				for (int i = 0; i < window+stride*slide;) {
					String result;
					if ((result = getData()) != null) {					
						
							double x[] = new double[dim] ;
							
							String[] splited = result.split(",");

							x[0] = Double.parseDouble(splited[0]);
							x[1] = Double.parseDouble(splited[1]);
					
							DataPoint trj = new DataPoint((i), x, (i)/stride);  // id, location, time;
							dataset.add(trj);
							i++;
						}
					}
			}
			else if( option == 2){ /* GeoLife */ 
			dim = 3; 
			FileReader file = new FileReader(path+"/GeoLife_sample.csv");
			br = new BufferedReader(file);

			System.out.println("[Geolife]");


			
			for (int i = 0; i < window+stride*slide;) {
				String result;
				if ((result = getData()) != null) {					
						
						double x[] = new double[dim] ;
						
						String[] splited = result.split(",");
						x[0] = Double.parseDouble(splited[0]);
						x[1] = Double.parseDouble(splited[1]);
						x[2] = Double.parseDouble(splited[2])/300000;
						
						DataPoint trj = new DataPoint((i), x, (i)/stride);  
						dataset.add(trj);
						i++;
					}
				}
			}
			else if( option == 3){ /* IRIS */ 
				dim = 4; 
				br = new BufferedReader(new FileReader(path+"/IRIS_sample.csv"));
				
				System.out.println("[IRIS]");


				for (int i = 0; i < window+stride*slide;) {
					String result;
					if ((result = getData()) != null) {					
						
							double x[] = new double[dim] ;
							String[] splited = result.split(",");
							
							if(splited[2].trim().length() != 0  && splited[3] != "" )
							{
								x[0] = Double.parseDouble(splited[0]);
								x[1] = Double.parseDouble(splited[1]);
								x[2] = Double.parseDouble(splited[2])/10;
								x[3] = Double.parseDouble(splited[3])*10;
								if( x[3] >= 0 ){
									DataPoint trj = new DataPoint((i), x, (i)/stride);
									dataset.add(trj);
									i++;
								}
							}
						}
					}
			}
			else if( option == 4){ /* Household */ 
				dim = 7; 
				br = new BufferedReader(new FileReader(path+"/Household_sample.csv"));
				
				System.out.println("[Household]");
				for (int i = 0; i < window+stride*slide;) {
					String result;
					if ((result = getData()) != null) {					
						
							double x[] = new double[dim] ;
							String[] splited = result.split(",");
							
							x[0] = Double.parseDouble(splited[0]);
							x[1] = Double.parseDouble(splited[1]);
							x[2] = Double.parseDouble(splited[2]);
							x[3] = Double.parseDouble(splited[3]);
							x[4] = Double.parseDouble(splited[4]);
							x[5] = Double.parseDouble(splited[5]);
							x[6] = Double.parseDouble(splited[6]);
									
							DataPoint trj = new DataPoint((i), x, (i)/stride);
							dataset.add(trj);
							i++;
						}
					}
			}

			return dataset;
			
			} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

}

