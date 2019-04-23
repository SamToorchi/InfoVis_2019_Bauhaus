package infovis.diagram;

import infovis.diagram.elements.Label;
import infovis.diagram.elements.Vertex;

import java.awt.Color;


public class MenuController {
	private View view = null;
	private Model model = null;
	private MouseController mouseControllerAddedToView = null;
	
	private static MenuController menuController = null;
	private MenuController(){
	}
	
	public static MenuController getMenuController(){
		if (menuController == null){
			menuController = new MenuController();
		}
		return menuController;  
	}
	
	public static MenuController getInstance(){
		return getMenuController();    
	}

	//getter and setter for MouseEvents
	public MouseController getMouseControllerAddedToView() {
		return mouseControllerAddedToView;
	}

	public void setMouseControllerAddedToView(
			MouseController mouseControllerAddedToView) {
		this.mouseControllerAddedToView = mouseControllerAddedToView;
	}
	
	//singleTone Patern SWT
	public Model getModel() {
		return model;
	}
	public void setModel(Model modell) {
		this.model = modell;
	}
	public View getView() {
		return view;
	}
	public void setView(View view) {
		this.view = view;
	}
	public void newVertex(){
		//Debug.print("MenuController.newVertex invoked");
		Vertex v = new Vertex(0,0,60,20);
		getModel().addVertex(v);
		view.repaint();
	}
	public void newLabel(){
		Label l = new Label();
		getModel().addLabel(l);
		view.repaint();
	}

	public void setScale(double scale){
		view.setScale(scale);
		view.repaint();
	}

	public void startEdgeDrawingMode() {
		mouseControllerAddedToView.setDrawingEdges(true);
	}

	public void stopEdgeDrawingMode() {
		mouseControllerAddedToView.setDrawingEdges(false);
	}
}