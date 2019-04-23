package infovis.diagram;

import infovis.debug.Debug;
import infovis.diagram.elements.DrawingEdge;
import infovis.diagram.elements.Edge;
import infovis.diagram.elements.Element;
import infovis.diagram.elements.GroupingRectangle;
import infovis.diagram.elements.None;
import infovis.diagram.elements.Vertex;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class MouseController implements MouseListener,MouseMotionListener {

	 private Model model;
	 private View view;
	 private Element selectedElements = new None();
	 private double mouseOffsetX;
	 private double mouseOffsetY;
	 private boolean drawEdgesMode = false;
	 private DrawingEdge drawingEdge = null;
	 private GroupingRectangle groupRectangle;
	 private boolean movingSelector = false;
     private boolean movingOverview = false;


	// Getter And Setter Methods
	 public Element getSelectedElements(){
		 return selectedElements;
	 }
    public Model getModel() {
		return model;
	}
	public void setModel(Model diagramModel) {
		this.model = diagramModel;
	}
	public View getView() {
		return view;
	}
	public void setView(View diagramView) {
		this.view = diagramView;
	}

	//Mouse Listener
	public void mouseClicked(MouseEvent event) {
		int x = event.getX();
		int y = event.getY();
		double scale = view.getScale();
		
		
		
		if (event.getButton() == MouseEvent.BUTTON3){
			// add element to the model
			Vertex groupVertex = (Vertex)getElementContainingPosition(x/scale,y/scale);
			for (@SuppressWarnings("unchecked")
			Iterator<Vertex> iter = groupVertex.getGroupedElements().iteratorVertices();iter.hasNext();){
				model.addVertex(iter.next());
			}
			for (@SuppressWarnings("unchecked")
			Iterator<Edge> iter = groupVertex.getGroupedElements().iteratorEdges();iter.hasNext();){
				model.addEdge(iter.next());
			}
			// remove element from the model
			List<Edge> edgesToRemove = new ArrayList<Edge>();
			for (@SuppressWarnings("unchecked")
			Iterator<Edge> iter = model.iteratorEdges(); iter.hasNext();){
				Edge edge = iter.next();
				if (edge.getSource() == groupVertex || edge.getTarget() == groupVertex){
					edgesToRemove.add(edge);
				}
			}
			//Remove the old Edge to draw it again with new Position
			model.removeEdges(edgesToRemove);
			model.removeElement(groupVertex);
			
			
		}
	}

	public void mouseEntered(MouseEvent arg0) {
		//no need now
	}

	public void mouseExited(MouseEvent arg0) {
		//no need now
	}
	public void mousePressed(MouseEvent event) {
		//need for move the selector
		//get position of mouse and give it to view to update paint and reposition of selector
		int x = event.getX();
		int y = event.getY();
		double scale = view.getScale();

		//Moving Selector
		if (view.selectorContains(x,y)){
		    movingSelector = true;
		    mouseOffsetX = x;
		    mouseOffsetY = y;
        }
		
		//Moving Zoom Frame
        else if (view.overviewContains(x,y)){
		    movingOverview = true;
            mouseOffsetX = x;
            mouseOffsetY = y;
        }
	   
		//Copied from Vertex
        else if (drawEdgesMode){
            drawingEdge = new DrawingEdge((Vertex)getElementContainingPosition(x/scale,y/scale));
            model.addElement(drawingEdge);
        }
        else {

            selectedElements = getElementContainingPosition(x/scale,y/scale);
            //set offset for Mouse for more presission
            mouseOffsetX = x - selectedElements.getX() * scale ;
            mouseOffsetY = y - selectedElements.getY() * scale ;
        }
		
	}
	public void mouseReleased(MouseEvent arg0){
		//save the position of selector
		int x = arg0.getX();
		int y = arg0.getY();

		if (movingSelector){
		    movingSelector = false;
            view.updatePosition();
        }
        else if (movingOverview){
		    movingOverview = false;
        }
        else {

            if (drawingEdge != null){
                Element to = getElementContainingPosition(x, y);
                model.addEdge(new Edge(drawingEdge.getFrom(),(Vertex)to));
                model.removeElement(drawingEdge);
                drawingEdge = null;
            }
            if (groupRectangle != null){
                Model groupedElements = new Model();
                for (@SuppressWarnings("unchecked")
				Iterator<Vertex> iter = model.iteratorVertices(); iter.hasNext();) {
                    Vertex vertex = iter.next();
                    if (groupRectangle.contains(vertex.getShape().getBounds2D())){
                        groupedElements.addVertex(vertex);
                    }
                }
                if (!groupedElements.isEmpty()){
                    model.removeVertices(groupedElements.getVertices());

                    Vertex groupVertex = new Vertex(groupRectangle.getCenterX(),groupRectangle.getCenterX());
                    groupVertex.setGroupedElements(groupedElements);
                    model.addVertex(groupVertex);

                    List<Edge> newEdges = new ArrayList<Edge>();
                    for (@SuppressWarnings("unchecked")
					Iterator<Edge> iter = model.iteratorEdges(); iter.hasNext();) {
                        Edge edge =  iter.next();
                        if (groupRectangle.contains(edge.getSource().getShape().getBounds2D())
                                && groupRectangle.contains(edge.getTarget().getShape().getBounds2D())){
                            groupVertex.getGroupedElements().addEdge(edge);
                            Debug.p("add Edge to groupedElements");
                        } else if (groupRectangle.contains(edge.getSource().getShape().getBounds2D())){
                            groupVertex.getGroupedElements().addEdge(edge);
                            newEdges.add(new Edge(groupVertex,edge.getTarget()));
                        } else if (groupRectangle.contains(edge.getTarget().getShape().getBounds2D())){
                            groupVertex.getGroupedElements().addEdge(edge);
                            newEdges.add(new Edge(edge.getSource(),groupVertex));
                        }
                    }
                    model.addEdges(newEdges);
                    model.removeEdges(groupedElements.getEdges());
                }
                model.removeElement(groupRectangle);
                groupRectangle = null;
            }
        }
	}
	
	public void mouseDragged(MouseEvent event) {
		int x = event.getX();
		int y = event.getY();
		double scale = view.getScale();

		if (movingSelector){
		    //update position of Selector
            view.updateMarker(x - (int)mouseOffsetX + (int)view.getSelector().getX(),
                    y - (int)mouseOffsetY + (int)view.getSelector().getY());
            mouseOffsetX = x;
            mouseOffsetY = y;
        }
        else if (movingOverview){
		    //update position Zoom Frame
            view.updateSelectorPosition(x - (int)mouseOffsetX , y - (int)mouseOffsetY);
            mouseOffsetX = x;
            mouseOffsetY = y;

        }
		else if (drawEdgesMode){
			drawingEdge.setX(event.getX());
			drawingEdge.setY(event.getY());
		}
		else if(selectedElements != null){
			selectedElements.updatePosition((event.getX()-mouseOffsetX)/scale, (event.getY()-mouseOffsetY) /scale);
		}
		view.repaint();
	}
	public void mouseMoved(MouseEvent event) {
		//no need now
	}
	public boolean isDrawingEdgess() {
		return drawEdgesMode;
	}
	public void setDrawingEdges(boolean drawingEdges) {
		this.drawEdgesMode = drawingEdges;
	}
	
	
	/*
	 * private Methods
	 */
	private Element getElementContainingPosition(double x,double y){
		Element currentElement = new None();
		Iterator<Element> iter = getModel().iterator();
		while (iter.hasNext()) {
		  Element element =  iter.next();
		  if (element.contains(x, y)) currentElement = element;  
		}
		return currentElement;
	}
	
    
}

