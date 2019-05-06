package infovis.scatterplot;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;

public class MouseController implements MouseListener, MouseMotionListener {

	private Model model = null;
	private View view = null;

	public void mouseClicked(MouseEvent arg0) {
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void mousePressed(MouseEvent arg0) {

		//Eingaben: x-Position und y-Position
		int x = arg0.getX();
		int y = arg0.getY();

		//Selektor Aktivieren
		view.setSelektorSichtbarkeit(true);
		view.getSelektor().setRect(x,y,0,0);
		view.repaint();
	}

	public void mouseReleased(MouseEvent arg0) {

		//wenn maus losgelassen wird, kann die Sichtbarkeit des Selektors zurückgesetzt werden
		view.setSelektorSichtbarkeit(false);
		view.repaint();
	}

	public void mouseDragged(MouseEvent arg0) {
		int x = arg0.getX();
		int y = arg0.getY();

		//die Größe des Selektors ändern
		Rectangle2D mrkr = view.getSelektor();
		view.setSelektor(new Rectangle2D.Double(mrkr.getX(), mrkr.getY(),x - mrkr.getX(), y - mrkr.getY()));

		view.repaint();
	}

	public void mouseMoved(MouseEvent arg0) {
	}

	public void setModel(Model model) {
		this.model  = model;	
	}

	public void setView(View view) {
		this.view  = view;
	}

}
