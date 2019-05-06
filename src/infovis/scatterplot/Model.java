package infovis.scatterplot;

import infovis.debug.Debug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.stream.FileImageInputStream;

public class Model {
	private ArrayList<Data> list  = new ArrayList<Data>();
	private ArrayList<Range> ranges = new ArrayList<Range>();
	private ArrayList<String> labels = new ArrayList<String>();
	private int dim = 0;
	
	public ArrayList<String> getLabels() {
		return labels;
	}
	public void setLabels(ArrayList<String> labels) {
		this.labels = labels;
	}
	public ArrayList<Data> getList() {
		return list;
	}
	public void setList(ArrayList<Data> list) {
		this.list = list;
	}
	public ArrayList<Range> getRanges() {
		return ranges;
	}
	public void setRanges(ArrayList<Range> ranges) {
		this.ranges = ranges;
	}
	public Model() {
		importValues();
	}
	public Iterator<Data> iterator() {
		return list.iterator();
	}
	public int getDim() {
		return dim;
	}
	public void setDim(int dim) {
		this.dim = dim;
	}
	
	
	public void importValues() {
		//File file = new File("cameras.ssv");
		File file = new File("cars.ssv");
	    Debug.p(file.getAbsoluteFile().toString());
	   
	    try {
	    	 String thisLine = null;
	    	 BufferedReader bufferreader = new BufferedReader(new FileReader(file));
	         try {
	        	 //Labels mit ; trennen
	        	 thisLine = bufferreader.readLine();
				 String l [] = thisLine.split(";");
				 
				// die Name der Labels in model speichern
				 for (int i = 1; i < l.length; i++) labels.add(l[i]); 
				 setDim(l.length-1);
				 
				  // Minimum value und maximum value festlegen
				 double minRange [] = new double[l.length-1];
				 for (int i = 0; i < minRange.length; i++) minRange[i] = Double.MAX_VALUE;
				 double maxRange [] = new double[minRange.length];
			     for (int i = 0; i < maxRange.length; i++) maxRange[i] = Double.MIN_VALUE;
	        	 
	        	 // Import Data and adapt Ranges
				 while ((thisLine = bufferreader.readLine()) != null) { // while loop begins here
					 String values [] = thisLine.split(";");
					 double dValues [] = new double[values.length -1];
					 
					 for (int j =1; j < values.length; j++) {
						 dValues[j-1] = Double.parseDouble(values[j]);
						 if (dValues[j-1] <  minRange[j-1]) minRange[j-1] = dValues[j-1];
						 if (dValues[j-1] >  maxRange[j-1]) maxRange[j-1] = dValues[j-1];
					 }	
					 list.add(new Data(dValues, values[0]));
	   			}
				for (int i = 0; i < maxRange.length; i++) {
					ranges.add(new Range(minRange[i],maxRange[i]));
				} 
				 
				
			} catch (IOException e) {
				e.printStackTrace();
			} // Ende while 

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	    /* Prints */
		for (String l : labels) {
			Debug.print(l);
			Debug.print(",  ");
			Debug.println("");
		}
		for (Range range : ranges) {
			Debug.print(range.toString());
			Debug.print(",  ");
			Debug.println("");
		}
		for (Data d : list) {
			Debug.print(d.toString());
			Debug.println("");
		}
		
	}
    
}
