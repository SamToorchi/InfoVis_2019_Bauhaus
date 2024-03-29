package infovis.paracoords;

import infovis.scatterplot.Model;

import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class MouseController implements MouseListener, MouseMotionListener {
	private View view = null;
	private Model model = null;
	Shape currentShape = null;

	private int X_mouse_ref;

	private boolean drawingMarker = false;
	private int movingAxis = -1;
	
	public void mouseClicked(MouseEvent e) {
		
	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {
		//check bounds?
		int x = e.getX();
		int y = e.getY();

		//Überprüfe ob einen Button gedruckt wurde
		if (view.pointInvertsAxis(x,y))
			return;

		//Überprüfe ob der Punkt in der Achse enthalten ist
		movingAxis = view.pointSelectsAxis(x,y);
		if (movingAxis >= 0){
			X_mouse_ref = x;
//			Y_mouse_ref = y;
		}
		else {
			//if not, start marker draw
			drawingMarker = true;
			view.setSelektor(x,y,x,y);
		}

	}

	public void mouseReleased(MouseEvent e) {

		if (movingAxis >= 0){
			movingAxis = -1;
		}
		else if (drawingMarker) {
			//check bounds?
			drawingMarker = false;
			view.brushAndLink();
			view.setSelektor(0,0,0,0);
		}



	}

	public void mouseDragged(MouseEvent e) {

		int x = e.getX();
		int y = e.getY();

		if (movingAxis >= 0) {

			view.moveAchse(movingAxis, x - X_mouse_ref);
			X_mouse_ref = x;
		}
		else if (drawingMarker){

			view.setSelektor((int)(view.getSelektor().getX()), (int)(view.getSelektor().getY()),
					x,y);
		}

	}

	public void mouseMoved(MouseEvent e) {

	}

	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

}
