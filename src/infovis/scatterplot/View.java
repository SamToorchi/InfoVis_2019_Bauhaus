package infovis.scatterplot;


import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.JPanel;

public class View extends JPanel {
	     private Model model = null;

	     // Selektor inizialisiern und verstecken
	     private Rectangle2D selektor = new Rectangle2D.Double(0,0,0,0);
         private Rectangle2D matrixRechtecke = new Rectangle2D.Double(0,0,0,0);
	     private boolean selektorSichtbar = false;

        private int X_Label_Abstand;
        private int Y_Label_Abstand;
        private int PLOT_Groesse;
        //Offset Faktor der Punkte
        private final double PLOT_Offset_Faktor = 0.2;

        //Schwarz - normale Punkte
        private Color Source_Point_Color = new Color(0xff000000);
        
        //Rot - selektierte Punkte
        private Color selektiert_Point_Color = new Color(0xffff0000);
        
        //Grau - Hintergrundfarbe
        private Color Hintergrund_Farbe = new Color(0xFFFFFFFF);


        //model inizialisieren
        public void setModel(Model model) {
        this.model = model;
        }
        //Funktionsaufruf für MouseController
        public Rectangle2D getSelektor() {
                return selektor;
        }
        
        //Sichtbarkeit der Selektor von false ändern
        public void setSelektorSichtbarkeit(boolean isVisible){
        	selektorSichtbar = isVisible;
        }

        public void setSelektor(Rectangle2D r) {
        	//erlauben den Selektor nach links und rechts zu zeichnen:
        	//Bereiche kleiner als null: Start mit Bereite + X-Position, 
        	//Weite der Selektor - Weite der Matrix, nur Höhe der Matrix 
        	//weil nur die Zeichnung auf die horizontale Achse ist 
            if (r.getWidth() < 0){
                r = new Rectangle2D.Double(r.getX() + r.getWidth(), r.getY(),
                        selektor.getWidth()-r.getWidth(), r.getHeight());
            }
            //erlauben den Selektor nach oben und unten zu zeichnen:
            //nur Position auf die Horizontale, Y-Position + Hohe der Matrix, Weite der Matrix, Höhe des
            //Selektors - Höhe der Matrix
            if (r.getHeight() < 0){
                r = new Rectangle2D.Double(r.getX(), r.getY() + r.getHeight(),
                        r.getWidth(), selektor.getHeight()-r.getHeight());
            }

            //Wenn Selektor sich in dem Matrix befindet:
            if (!matrixRechtecke.contains(r))
                return;

            //Selektor darf die Matrix nicht verlassen
            int[] Zelle1 = Zelle_Mit_Punkte(r.getX(), r.getY());
            int[] Zelle2 = Zelle_Mit_Punkte(r.getX() + r.getWidth(), r.getY() + r.getHeight());
            if (Zelle1[0] != Zelle2[0] || Zelle1[1] != Zelle2[1]){
                return;
            }

            selektor = r;

            //Punkte Markieren
            brushUndLink(selektor, Zelle1);

        }
		 
        //Methoden von paint überschreiben
		@Override
		public void paint(Graphics g) {

			//Anzahl der Matrizen sind Anzahl der Labels
            final int Anzahl_Attr = model.getLabels().size();
            //Radius der Punkte
            final int Radius_Punkt = 2;
            //Aufteilung der Label auf die X-Achse in Abhängigkeit von der Fenstergröße (aller 12%)
            X_Label_Abstand = (int)(getWidth() * 0.12);
            //Aufteilung der Label auf die Y-Achse in Abhängigkeit von der Fenstergröße (aller 10%)
            Y_Label_Abstand = (int)(getHeight() * 0.1);
            //Größe des gesamten Plots
            PLOT_Groesse = Math.min((getWidth() - X_Label_Abstand) / Anzahl_Attr , (getHeight() - Y_Label_Abstand + 2) / Anzahl_Attr);
            
            //Matrix - Größe
            matrixRechtecke = new Rectangle2D.Double(X_Label_Abstand,Y_Label_Abstand,PLOT_Groesse * Anzahl_Attr,PLOT_Groesse * Anzahl_Attr);

            //Zeichnen
            Graphics2D g2D = (Graphics2D) g;
            //Anti-Aliasing aktivieren
            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

            //Beschriftung jedes Labels
            Font schriftart = new Font("sansserif", Font.BOLD, 12);
            g2D.setFont(schriftart);
            //Labels aus der Model lesen
            ArrayList<String> labels = model.getLabels();
            //Labels erzeugen
            for (int i = 0; i < Anzahl_Attr; i++){
                String label = labels.get(i);
                //x label
                g2D.drawString(label, X_Label_Abstand + (i*PLOT_Groesse), (int)(Y_Label_Abstand * 0.9));

                //y label
                g2D.drawString(label, (int)(X_Label_Abstand * 0.1), (int)(Y_Label_Abstand + ((i+0.5)*PLOT_Groesse)));

            }

            /*
             * Überschrift
            font = new Font("Serif", Font.PLAIN, (int)(Y_Label_Abstand/2));
            g2D.setFont(font);
            g2D.drawString("Scatter Plot Matrix", (int)(getWidth() * 0.3), (int)(getHeight()* 0.06));
             */

            for (int x = 0; x < Anzahl_Attr; x++){
                for (int y = 0; y < Anzahl_Attr; y++) {

                        g2D.setColor(Hintergrund_Farbe);

                        //Matrizen mit der Hintergrund und festgelegte Größe zeichnen
                    g2D.fillRect(X_Label_Abstand + (x*PLOT_Groesse), Y_Label_Abstand + (y*PLOT_Groesse),
                            PLOT_Groesse, PLOT_Groesse);
                    g2D.setColor(new Color(0xff000000));
                    g2D.drawRect(X_Label_Abstand + (x*PLOT_Groesse), Y_Label_Abstand + (y*PLOT_Groesse),
                            PLOT_Groesse, PLOT_Groesse);
                }
            }

            //jeden Punkt zeichnen
            for (Data d : model.getList()){

                g2D.setColor(d.getColor());

                //für jedes Label auf der x- und y- Achse
                for (int y = 0; y < Anzahl_Attr; y++) {
                	for (int x = 0; x < Anzahl_Attr; x++) {


                        //Offsets berechnen
                        int plot_offset_y = Y_Label_Abstand + ((y+1) * PLOT_Groesse);//von unten
                        int plot_offset_x = X_Label_Abstand + (x * PLOT_Groesse);//von links

                        //Werte auf den Daten ablesen
                        double y_value = d.getValues()[y];
                        double x_value = d.getValues()[x];

                        
                        //positionen berechnen
                        double y_zeichnen = plot_offset_y - (getPointOffset(y_value,  model.getRanges().get(y)) * PLOT_Groesse);
                        double x_zeichnen = plot_offset_x + (getPointOffset(x_value,  model.getRanges().get(x)) * PLOT_Groesse);
                        
                        
                        //Zeichnen
                        g2D.fill(new Ellipse2D.Double(x_zeichnen - Radius_Punkt, y_zeichnen - Radius_Punkt,
                                2*Radius_Punkt, 2*Radius_Punkt));

                    }
                }

            }

            //Farbe der Marker definieren, wenn visibility auf visible ist           
            if (selektorSichtbar){
                g2D.setColor(new Color(0xFF008000));
                g2D.draw(selektor);
            } 
		}

		//Brush und Link, um die Farbe der zusammenhängenden Daten (Punkte) in rot zu setzen
		private void brushUndLink(Rectangle2D marker, int[] Zelle_Koord){
            //akzeptierte Bereiche für Punkte festlegen
            //double minX,maxX,minY,maxY;
            double[] minPoint = getDataToPoint(marker.getX(),
                    marker.getY() + marker.getHeight(), Zelle_Koord[0], Zelle_Koord[1]);
            double[] maxPoint = getDataToPoint(marker.getX() + marker.getWidth(),
                    marker.getY(), Zelle_Koord[0], Zelle_Koord[1]);

            //Vergleichen alle Daten mit diesen Bereichen
            for (Data d : model.getList()){

            	//Werte in jedem Matrix speichern
                double data_x = d.getValues()[Zelle_Koord[0]];
                double data_y = d.getValues()[Zelle_Koord[1]];

                //x-Werte überprüfen
                if (data_x < minPoint[0] || data_x > maxPoint[0]){
                    //alles was außerhalb des Bereichs ist, wird nicht markiert
                    d.setColor(Source_Point_Color);
                    continue;
                }

              //y-Werte überprüfen
                if (data_y < minPoint[1] || data_y > maxPoint[1]){
                    //alles was außerhalb des Bereichs ist, wird nicht markiert
                    d.setColor(Source_Point_Color);
                    continue;
                }

                //ansonsten die Daten (Punkte markieren)
                d.setColor(selektiert_Point_Color);

            }

        }

		//Die Position der Punkte werden in % berechnet und jedem Matrix positioniert
		private double getPointOffset(double point, Range r){
		    if (point < r.getMin()){
		        System.err.println("punkt ausserhalb von bereich");
		        return 0.0;
            }
            if(point > r.getMax()){
                System.err.println("punkt ausserhalb von bereich");
                return 1.0;
            }

            double range = r.getMax() - r.getMin();
            //ein wenig offset über und unter dem Matrix erzeugen
            range += (range * PLOT_Offset_Faktor * 2.0);
            
            //Divion durch 0 ist nicht erlaubt und gibt immer null zurück
            if (range == 0.0)//protect against division by 0
                return 0.0;

            return (point - r.getMin() + (PLOT_Offset_Faktor*range))/range;

        }
		
		//gibt die Datenwerte in der korrekten Skala für einen Punkt in der Scattermatrix zurück
		//nimmt cell als Argumente an, wie sie bereits in setMarkerRect berechnet wurden
        private double[] getDataToPoint(double x,double y, int cell_x, int cell_y){

		    //relative Position innerhalb der Zelle erhalten
        	//erstmal in Pixel
        	//dann normalisiert
        	
            double rel_y = ((cell_y+1) * PLOT_Groesse  +  Y_Label_Abstand) - y;//in px
            rel_y /= PLOT_Groesse;
            
            
            double rel_x = x - X_Label_Abstand - (cell_x * PLOT_Groesse);
            rel_x /= PLOT_Groesse;



            //für Plotoffset anpassen
            double scale_factor = 1.0 / (1.0 - (PLOT_Offset_Faktor*2));
            rel_x -= PLOT_Offset_Faktor;
            rel_x *= scale_factor;
            rel_y -= PLOT_Offset_Faktor;
            rel_y *= scale_factor;

            ArrayList<Range> ranges = model.getRanges();
            double[] rtn_values = new double[]{0.0,0.0};
            
            //y
            Range y_range = ranges.get(cell_y);
            rtn_values[1] = y_range.getMin() + ((y_range.getMax() - y_range.getMin()) * rel_y);

            //x
            Range x_range = ranges.get(cell_x);
            rtn_values[0] = x_range.getMin() + ((x_range.getMax() - x_range.getMin()) * rel_x);


            return rtn_values;
        }

        //Gibt die x- und y-Koordinaten, in welcher Zelle sich der angegebene Punkt befindet, zurück
        private int[] Zelle_Mit_Punkte(double x, double y){

		    if (!matrixRechtecke.contains(x,y))
		        return new int[]{-1,-1};

		    int[] Zelle_Koord = new int[2];

            Zelle_Koord[0] = (int)((x - X_Label_Abstand) / PLOT_Groesse);
            Zelle_Koord[1] = (int)((y - Y_Label_Abstand) / PLOT_Groesse);

            return Zelle_Koord;
        }


}
