package infovis.paracoords;

import infovis.scatterplot.Data;
import infovis.scatterplot.Model;
import infovis.scatterplot.Range;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JPanel;

public class View extends JPanel {
	private Model model = null;

	//private ArrayList<Integer> axis_x_positions;
	private ArrayList<Double> achse_x_rel_position;
	private ArrayList<Integer> axis_order;
	private ArrayList<Boolean> axis_is_ascending;

	private Rectangle2D selektor = new Rectangle2D.Double(0,0,0,0);

	private int Achse_Bereite;
	private int Achse_Hoehe;
	//offset der Achse an jedem Ende
	private final double Achse_offset_end = 0.1; 
	private int Y_Achse_Padding_top;
	private int Y_Achse_Padding_bottom;
	private int Invertierer_button_top;
	private int Invertierer_button_groesse;

	//Überschreiben der paint-Methoden
	@Override
	public void paint(Graphics g) {

		Graphics2D g2D = (Graphics2D) g;
		//Aktivierung der Anti-Aliasing
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

		
		final int padding_general = (int)(getWidth() * 0.08);
		Y_Achse_Padding_top = (int)(getHeight() * 0.2);
		Y_Achse_Padding_bottom = (int)(getHeight() * 0.2);
		Achse_Bereite = (int)(getWidth() * 0.005);
		Achse_Hoehe = (int)(getHeight() - (Y_Achse_Padding_top+Y_Achse_Padding_bottom));
		Invertierer_button_groesse = Achse_Bereite*8;
		Invertierer_button_top = getHeight() - (int)(Y_Achse_Padding_bottom*0.8);
		ArrayList<String> labels = model.getLabels();
		int Anzahl_Achsen = labels.size();

		if(achse_x_rel_position == null){
			achse_x_rel_position = new ArrayList<>();
			final int AXIS_STEP = (getWidth() - padding_general*2 - Achse_Bereite) / (Anzahl_Achsen - 1);
			for (int i = 0; i < Anzahl_Achsen; i++){
				achse_x_rel_position.add((padding_general+(i*AXIS_STEP))/(double)getWidth() );
			}
		}
		//set initial ascending variables if necessary
		if (axis_is_ascending == null){
			axis_is_ascending = new ArrayList<>();
			for (int i = 0; i < Anzahl_Achsen; i++)
				axis_is_ascending.add(true);
		}
		//update order that parallel axes should be drawn in
		axis_order = getAxisOrder(achse_x_rel_position);
		
		
		//Achsen und Beschreibungen zeichnen
		ArrayList<Range> ranges = model.getRanges();
		Font att_font = new Font("Serif", Font.BOLD, Y_Achse_Padding_top/10);
		Font label_font = new Font("Serif", Font.BOLD, Y_Achse_Padding_top/9);
		g2D.setColor(new Color(0xff333333));
		for (int i = 0; i < Anzahl_Achsen; i++){


			int axis_x = (int)(achse_x_rel_position.get(i) * getWidth());
			//Achsen definieren
			g2D.setColor(new Color(0xFF7A7A7A));
			g2D.fillRect(axis_x, Y_Achse_Padding_top, Achse_Bereite, Achse_Hoehe);

			//Beschreibung der Achsen
			g2D.setFont(att_font);
			String l = labels.get(i);
			int width = g.getFontMetrics().stringWidth(l);
			g2D.drawString(l, axis_x - (width/2), (int)(Y_Achse_Padding_top * 0.8));

			//draw axis scale labels
			g2D.setFont(label_font);
			String min_s = Double.toString(ranges.get(i).getMin());
			String max_s = Double.toString(ranges.get(i).getMax());
			int min_w = g.getFontMetrics().stringWidth(min_s);
			int max_w = g.getFontMetrics().stringWidth(max_s);
			if (axis_is_ascending.get(i)){
				//draw min at bottom
				g2D.drawString(min_s, axis_x - (min_w+Achse_Bereite),
						(int)(getHeight() - Y_Achse_Padding_bottom - (Achse_offset_end*Achse_Hoehe*0.7)));
				g2D.drawString(max_s, axis_x - (max_w+Achse_Bereite),
						(int)(Y_Achse_Padding_top + (Achse_offset_end*Achse_Hoehe*0.9)));
			}
			else {
				g2D.drawString(max_s, axis_x - (max_w+Achse_Bereite),
						(int)(getHeight() - Y_Achse_Padding_bottom - (Achse_offset_end*Achse_Hoehe*0.7)));
				g2D.drawString(min_s, axis_x - (min_w+Achse_Bereite),
						(int)(Y_Achse_Padding_top + (Achse_offset_end*Achse_Hoehe*0.9)));
			}

			//Invertieren Button zeichnen
			g2D.setColor(new Color((0xFF7A3131)));
			g2D.fillRoundRect(axis_x - Achse_Bereite*2, Invertierer_button_top, Invertierer_button_groesse, Invertierer_button_groesse,
					(int)(Invertierer_button_groesse*0.3), (int)(Invertierer_button_groesse*0.3));
			String arrow;
			if (axis_is_ascending.get(i))
				arrow = "|^|";
			else
				arrow = "|v|";
			Font arrow_font = new Font("Sans-Serif", Font.PLAIN, (int)(Y_Achse_Padding_top/4));
			g2D.setFont(arrow_font);
			g2D.setColor(new Color(0xffffff));
			g2D.drawString(arrow, axis_x - (int)(Achse_Bereite*1.7), Invertierer_button_top+Invertierer_button_groesse);



		}




		//plot lines for items
		ArrayList<Data> data = model.getList();
		//for each item...
		for (Data d : data){
			ArrayList<Integer> item_y_points = new ArrayList<>();
			double [] values = d.getValues();
			//...calculate the position at which it's line crosses each axis
			for (int i = 0; i < Anzahl_Achsen; i++){

				int axis_id = axis_order.get(i);

				item_y_points.add(absPointOnRange(values[axis_id], ranges.get(axis_id), Achse_Hoehe, axis_is_ascending.get(axis_id)));

				//then draw line
				if (i > 0){

					int prev_axis_id = axis_order.get(i-1);
					g2D.setColor(d.getColor());
					g2D.drawLine((int)(achse_x_rel_position.get(prev_axis_id)*getWidth()) + Achse_Bereite, item_y_points.get(i-1)+Y_Achse_Padding_top,
							(int)(achse_x_rel_position.get(axis_id)*getWidth()), item_y_points.get(i)+Y_Achse_Padding_top);
				}
			}
		}

		//Zeichne den Selektor
		g2D.setColor(new Color((0xFF008A00)));
		g2D.draw(selektor);

	}

	//returns an int describing how many pixels along an axis a value is
	private Integer absPointOnRange(double value, Range range, int Achse_Hoehe, boolean ascending){



		if (value < range.getMin() || value > range.getMax()){
			System.err.println("absPointOnRange: Value out of range");
			return -1;
		}
		//get normalised position of value within range
		double norm_pos = (value - range.getMin()) / (range.getMax() - range.getMin());

		//scale and shift to get normalised value within offset range
		norm_pos = norm_pos * (1.0 - Achse_offset_end *2);
		norm_pos += Achse_offset_end;

		//convert to absolute value depending on ascending or descending
		if (ascending){
			return (int)((1.0-norm_pos) * Achse_Hoehe);
		}
		else {
			return (int)(norm_pos*Achse_Hoehe);
		}
	}

	public Rectangle2D getSelektor(){
		return selektor;
	}
	public void setSelektor(int markerStartX, int markerStartY, int draggedToX, int draggedToY) {

		//if start and dragged are the same, just set w and h as 0
		if (markerStartX == draggedToX){
			selektor = new Rectangle2D.Double(markerStartX, markerStartY,0,0);
		}
		else {
			selektor = new Rectangle2D.Double(markerStartX,markerStartY,
					draggedToX-markerStartX, draggedToY-markerStartY);
		}
		repaint();
	}

	//highlights data items selected by current marker position
	public void brushAndLink(){

		ArrayList<Integer> arraysIntersected = new ArrayList<>();
		ArrayList<Range> permittedRanges = new ArrayList<>();

		//check which axes are intersected by marker
		for (int i = 0; i < achse_x_rel_position.size(); i++){

			//if intersection between marker and axis...
			Rectangle2D axis = new Rectangle2D.Double(achse_x_rel_position.get(i)*getWidth(), Y_Achse_Padding_top, Achse_Bereite, Achse_Hoehe);
			Rectangle2D intersct = selektor.createIntersection(axis);
			if (intersct.getWidth() > 0){
				//add to list
				arraysIntersected.add(i);

				//create permitted range
				permittedRanges.add(getPermittedRange(i));
			}
		}

		//if no axes are marked, do nothing
		if (arraysIntersected.size() == 0)
			return;

		//for each data item, check whether it crosses relevant access within range of rectangle
		Color HILITE_COL = new Color(0xffff2222);
		ArrayList<Range> ranges = model.getRanges();
		ArrayList<Data> data = model.getList();
		for (Data d : data){
			boolean shouldHighlight = false;
			for (int i = 0; i < arraysIntersected.size(); i++){

				int axis_to_check = arraysIntersected.get(i);
				Range range_to_check_in = permittedRanges.get(i);

				if (range_to_check_in.contains(d.getValues()[axis_to_check])){

					shouldHighlight = true;
					break;
				}
			}
			if (shouldHighlight)
				d.setColor(HILITE_COL);
			else
				d.setColor(new Color(0xff000000));
		}
	}

	//calculates permitted range for data values on an axis
	// with respect to what the marker is currently selecting
	// and whether the axis is inverted
	private Range getPermittedRange (int axis_id){

		//calculate allowable range from marker position
		int axisMin = Y_Achse_Padding_top;
		int axisMax = getHeight() - Y_Achse_Padding_bottom;
		int markerMin = Math.max(axisMin, (int)selektor.getY());
		int markerMax = Math.min(axisMax, (int)(selektor.getY() + selektor.getHeight()));

		//get min and max on 0-1 scale
		double markerMin_d = (markerMin-axisMin) / (double)Achse_Hoehe;
		double markerMax_d = (markerMax-axisMin) / (double)Achse_Hoehe;

		//scale and shift range to allow for offsets on axes
		markerMin_d = (markerMin_d - Achse_offset_end) / (1.0-2*Achse_offset_end);
		markerMax_d = (markerMax_d - Achse_offset_end) / (1.0-2*Achse_offset_end);

		//calculate absolute values WRT data range
		Range wholeRange = model.getRanges().get(axis_id);
		Range permittedRange;
		//invert scale for ascending axes
		if (axis_is_ascending.get(axis_id)){
			double tempMin_d = markerMin_d;
			markerMin_d = 1.0-markerMax_d;
			markerMax_d = 1.0-tempMin_d;

		}
		double range = wholeRange.getMax() - wholeRange.getMin();
		permittedRange = new Range(wholeRange.getMin() + range*markerMin_d,
				wholeRange.getMin() + range*markerMax_d);

		return permittedRange;
	}

	//returns number of axis that point is contained by
	// if none, return -1
	public int pointSelectsAxis(int x, int y){

		//check y pos
		if (y < Y_Achse_Padding_top || y > (getHeight() - Y_Achse_Padding_bottom)){
			//y coord out of range of axes
			return -1;
		}
		//check x pos
		for (int i = 0; i < achse_x_rel_position.size(); i++){
			if (x >= achse_x_rel_position.get(i)*getWidth()
					&& x <= (achse_x_rel_position.get(i)*getWidth() + Achse_Bereite)){
				return i;
			}
		}
		return -1;
	}
	

	//Umkehrung der y-Achse
	public boolean pointInvertsAxis(int x,int y){

		//Überprüfe die Position der Y-Achse
		if (y < Invertierer_button_top || y > Invertierer_button_top+Invertierer_button_groesse)
			return false;

		//Überprüfe die Position der X-Achse
		for (int i = 0; i < achse_x_rel_position.size(); i++){
			int axis_x = (int)(achse_x_rel_position.get(i) * getWidth());

			if (x >= axis_x - Achse_Bereite*2
					&& x <= axis_x - Achse_Bereite*2 + Invertierer_button_groesse){
				axis_is_ascending.set(i, !axis_is_ascending.get(i));
				repaint();
				return true;
			}
		}
		return false;
	}

	//updates x position of given axis
	public void moveAxis(int axis_id, int by_x){
		double rel_change = by_x / (double)getWidth();
		achse_x_rel_position.set(axis_id, achse_x_rel_position.get(axis_id) + rel_change);
		repaint();
	}

	//returns a list of axis ids in order of x location (left to right)
	private ArrayList<Integer> getAxisOrder(ArrayList<Double> x_positions){
		ArrayList<Double> axes_pos = new ArrayList<>(x_positions);
		ArrayList<Integer> axis_order = new ArrayList<>();

		for (int i = 0; i < x_positions.size(); i++){

			double min_val = Collections.min(axes_pos);
			int axis = x_positions.indexOf(min_val);
			axis_order.add(axis);
			axes_pos.set(axis, Double.MAX_VALUE);
		}
		return axis_order;
	}

	@Override
	public void update(Graphics g) {
		paint(g);
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}
	
}
