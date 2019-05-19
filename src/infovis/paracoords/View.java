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

	//private ArrayList<Integer> axis_x_positionen;
	private ArrayList<Double> achse_x_rel_position;
	private ArrayList<Integer> achsen_reihenfolge;
	private ArrayList<Boolean> axis_ascending;

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
		//Achsen Variablen erzeugen
		if (axis_ascending == null){
			//in modell einfügen
			axis_ascending = new ArrayList<>();
			for (int i = 0; i < Anzahl_Achsen; i++)
				axis_ascending.add(true);
		}
		//Aktualisieren Sie die Reihenfolge, in der die parallelen Achsen eingezeichnet werden sollen
		achsen_reihenfolge = getAxisOrder(achse_x_rel_position);
		
		
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

			//Achsenskalenbeschriftungen zeichnen
			g2D.setFont(label_font);
			String min_scale = Double.toString(ranges.get(i).getMin());
			String max_scale = Double.toString(ranges.get(i).getMax());
			int min_width = g.getFontMetrics().stringWidth(min_scale);
			int max_width = g.getFontMetrics().stringWidth(max_scale);
			if (axis_ascending.get(i)){
				//zeichne Minimumwert ganz unten
				g2D.drawString(min_scale, axis_x - (min_width+Achse_Bereite),
						(int)(getHeight() - Y_Achse_Padding_bottom - (Achse_offset_end*Achse_Hoehe*0.7)));
				g2D.drawString(max_scale, axis_x - (max_width+Achse_Bereite),
						(int)(Y_Achse_Padding_top + (Achse_offset_end*Achse_Hoehe*0.9)));
			}
			else {
				g2D.drawString(max_scale, axis_x - (max_width+Achse_Bereite),
						(int)(getHeight() - Y_Achse_Padding_bottom - (Achse_offset_end*Achse_Hoehe*0.7)));
				g2D.drawString(min_scale, axis_x - (min_width+Achse_Bereite),
						(int)(Y_Achse_Padding_top + (Achse_offset_end*Achse_Hoehe*0.9)));
			}

			//Invertieren Button zeichnen
			g2D.setColor(new Color((0xFF7A3131)));
			g2D.fillRoundRect(axis_x - Achse_Bereite*2, Invertierer_button_top, Invertierer_button_groesse, Invertierer_button_groesse,
					(int)(Invertierer_button_groesse*0.3), (int)(Invertierer_button_groesse*0.3));
			String arrow;
			if (axis_ascending.get(i))
				arrow = "|^|";
			else
				arrow = "|v|";
			Font arrow_font = new Font("Sans-Serif", Font.PLAIN, (int)(Y_Achse_Padding_top/4));
			g2D.setFont(arrow_font);
			g2D.setColor(new Color(0xffffff));
			g2D.drawString(arrow, axis_x - (int)(Achse_Bereite*1.7), Invertierer_button_top+Invertierer_button_groesse);



		}




		//Plotlinien für Elemente
		ArrayList<Data> data = model.getList();
		//für jedes Element aus Model:
		for (Data d : data){
			ArrayList<Integer> item_y_punkte = new ArrayList<>();
			double [] values = d.getValues();
			//Berechne die Position, an der die Linie die einzelnen Achsen kreuzt bzw. schneidet
			for (int i = 0; i < Anzahl_Achsen; i++){

				int achse_id = achsen_reihenfolge.get(i);

				item_y_punkte.add(absPointOnRange(values[achse_id], ranges.get(achse_id), Achse_Hoehe, axis_ascending.get(achse_id)));

				//Plotlinien Zeicnen
				if (i > 0){

					int vorgaenger_achse_id = achsen_reihenfolge.get(i-1);
					g2D.setColor(d.getColor());
					g2D.drawLine((int)(achse_x_rel_position.get(vorgaenger_achse_id)*getWidth()) + Achse_Bereite, item_y_punkte.get(i-1)+Y_Achse_Padding_top,
							(int)(achse_x_rel_position.get(achse_id)*getWidth()), item_y_punkte.get(i)+Y_Achse_Padding_top);
				}
			}
		}

		//Zeichne den Selektor
		g2D.setColor(new Color((0xFF008A00)));
		g2D.draw(selektor);

	}

	//gibt einen Int zurück, der beschreibt, wie viele Pixel ein Wert entlang einer Achse hat
	private Integer absPointOnRange(double value, Range range, int Achse_Hoehe, boolean ascending){


		//normalisierten Psitionsvalue innerhalb des range
		double normalisierte_position = (value - range.getMin()) / (range.getMax() - range.getMin());

		//skalieren und verschieben, um einen normalisierten Wert innerhalb des Versatzbereichs zu erhalten
		normalisierte_position = normalisierte_position * (1.0 - Achse_offset_end *2);
		normalisierte_position += Achse_offset_end;

		//convert to absolute value depending on ascending or descending
		if (ascending){
			return (int)((1.0-normalisierte_position) * Achse_Hoehe);
		}
		else {
			return (int)(normalisierte_position*Achse_Hoehe);
		}
	}

	//Funktionsaufruf für Selektor
	public Rectangle2D getSelektor(){
		return selektor;
	}
	public void setSelektor(int markerStartX, int markerStartY, int draggedToX, int draggedToY) {

		//wenn Höhe und Breite die gleichen Werte sind, dann gib 0 zurück
		if (markerStartX == draggedToX){
			selektor = new Rectangle2D.Double(markerStartX, markerStartY,0,0);
		}
		//sonst berechne die relative Änderung
		else {
			selektor = new Rectangle2D.Double(markerStartX,markerStartY,
					draggedToX-markerStartX, draggedToY-markerStartY);
		}
		repaint();
	}

	//Markieren der Daten
	public void brushAndLink(){

		//Modell erzeugen
		ArrayList<Integer> arraysGeschnitten = new ArrayList<>();
		ArrayList<Range> permittedRanges = new ArrayList<>();

		//Überprüfen welche Achsen von der Marker geschnitten werden
		for (int i = 0; i < achse_x_rel_position.size(); i++){

			//if intersection between marker and axis...
			Rectangle2D axis = new Rectangle2D.Double(achse_x_rel_position.get(i)*getWidth(), Y_Achse_Padding_top, Achse_Bereite, Achse_Hoehe);
			Rectangle2D Schnittpunkt = selektor.createIntersection(axis);
			if (Schnittpunkt.getWidth() > 0){
				//in Modell schreiben
				arraysGeschnitten.add(i);

				//erlaubten Bereich anlegen
				permittedRanges.add(getPermittedRange(i));
			}
		}

		//wenn keine Achsen markiert sind, nichts machen
		if (arraysGeschnitten.size() == 0)
			return;

		Color Highlight_COL = new Color(0xffff2222);
		ArrayList<Range> ranges = model.getRanges();
		ArrayList<Data> data = model.getList();
		for (Data d : data){
			boolean shouldHighlight = false;
			for (int i = 0; i < arraysGeschnitten.size(); i++){

				int achsen_zum_ueberpruefen = arraysGeschnitten.get(i);
				Range range_to_check_in = permittedRanges.get(i);

				if (range_to_check_in.contains(d.getValues()[achsen_zum_ueberpruefen])){

					shouldHighlight = true;
					break;
				}
			}
			if (shouldHighlight)
				d.setColor(Highlight_COL);
			else
				d.setColor(new Color(0xff000000));
		}
	}

	//Berechnet den zulässigen Bereich für Datenwerte auf einer Achse in Bezug darauf, was der Marker gerade auswählt und ob die Achse invertiert ist
	private Range getPermittedRange (int achse_id){

		//Berechnet den zulässigen Bereich von der Markerposition
		int achseMin = Y_Achse_Padding_top;
		int achseMax = getHeight() - Y_Achse_Padding_bottom;
		int markerMin = Math.max(achseMin, (int)selektor.getY());
		int markerMax = Math.min(achseMax, (int)(selektor.getY() + selektor.getHeight()));

		//min und max zwischen 0-1 berechnet
		double markerMin_d = (markerMin-achseMin) / (double)Achse_Hoehe;
		double markerMax_d = (markerMax-achseMin) / (double)Achse_Hoehe;

		//Verschiebungen auf Achsen
		markerMin_d = (markerMin_d - Achse_offset_end) / (1.0-2*Achse_offset_end);
		markerMax_d = (markerMax_d - Achse_offset_end) / (1.0-2*Achse_offset_end);


		Range wholeRange = model.getRanges().get(achse_id);
		Range permittedRange;
		//Skala für aufsteigende Achsen invertieren
		if (axis_ascending.get(achse_id)){
			double tempMin_d = markerMin_d;
			markerMin_d = 1.0-markerMax_d;
			markerMax_d = 1.0-tempMin_d;

		}
		double range = wholeRange.getMax() - wholeRange.getMin();
		permittedRange = new Range(wholeRange.getMin() + range*markerMin_d,
				wholeRange.getMin() + range*markerMax_d);

		return permittedRange;
	}

	//Gibt die Nummer der Achse zurück, in der sich der Punkt befindet
	// if none, return -1
	public int pointSelectsAxis(int x, int y){

		
		if (y < Y_Achse_Padding_top || y > (getHeight() - Y_Achse_Padding_bottom)){
			//y Koordinate außerhalb des Achsenbereichs
			return -1;
		}
		
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
				axis_ascending.set(i, !axis_ascending.get(i));
				repaint();
				return true;
			}
		}
		return false;
	}

	//aktuallisiere die X-Position der bewegten Achse
	public void moveAchse(int achse_id, int by_x){
		double rel_aenderung = by_x / (double)getWidth();
		achse_x_rel_position.set(achse_id, achse_x_rel_position.get(achse_id) + rel_aenderung);
		repaint();
	}

	//gibt eine Liste der Achsen-IDs in der Reihenfolge der x-Position zurück (von links nach rechts)
	private ArrayList<Integer> getAxisOrder(ArrayList<Double> x_positionen){
		ArrayList<Double> axes_pos = new ArrayList<>(x_positionen);
		ArrayList<Integer> achsen_reihenfolge = new ArrayList<>();

		for (int i = 0; i < x_positionen.size(); i++){

			double min_val = Collections.min(axes_pos);
			int axis = x_positionen.indexOf(min_val);
			achsen_reihenfolge.add(axis);
			axes_pos.set(axis, Double.MAX_VALUE);
		}
		return achsen_reihenfolge;
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
