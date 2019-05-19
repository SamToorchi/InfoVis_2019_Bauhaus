package infovis.diagram;

import infovis.diagram.elements.Element;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;



public class View extends JPanel{
	private Model model = null;
	private Color color = Color.BLUE;
	//Default Scale
	private double scale = 1;
	private double overviewDiagramScale;
	// 30% of Window for Zoom Frame
	private double overviewBoxScale = 0.3;
	//Border Stroke of Zoom Frame
	private int selectorBoxStroke = 3;
	private double translateX=0;
	private double translateY=0;
	//Position of Zoom Frame
	private double selectorTranslateX=0;
	private double selectorTranslateY=0;
	private Rectangle2D selector = new Rectangle2D.Double(selectorTranslateX,selectorTranslateY,0,0);
	private Rectangle2D overviewRect = new Rectangle2D.Double();
	private boolean fisheye;



	public void setFisheye(boolean shouldSetFisheye){
		fisheye = shouldSetFisheye;
	}
	public Model getModel() {
		return model;
	}
	public void setModel(Model model) {
		this.model = model;
	}
	public Color getColor() {
		return color;
	}
	public void setColor(Color color) {
		this.color = color;
	}

	
	public void paint(Graphics g) {
		
		Graphics2D g2D = (Graphics2D) g;
		// Anti-Aliasing for Border of Circles
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.clearRect(0, 0, getWidth(), getHeight());
		
		if (fisheye){

			paintDiagram(g2D);
		}
		else{

			//main diagram
			g2D.translate(translateX,translateY);
			g2D.scale(scale,scale);
			paintDiagram(g2D);
			//reset translation and scale for Diagram
			g2D.scale(1/scale,1/scale);
			g2D.translate(-translateX,-translateY);
			//reset translation and scale for Zoom Frame
			//g2D.translate(selectorTranslateX,selectorTranslateY);


			
			//set dimensions of Zoom Frame  for Default Scale
			overviewRect = new Rectangle2D.Double(selectorTranslateX,selectorTranslateY,getWidth()*overviewBoxScale, getHeight()*overviewBoxScale);
			//complementary colors for Background
			g2D.setColor(new Color(0xbcb88f));
			g2D.fill(overviewRect);
			//add Border Color of Zoom Frame
			g2D.setColor(new Color(0xff3e9172));
			//set Border Stroke of ZoomFrame
			g2D.setStroke(new BasicStroke(3));
			g2D.draw(overviewRect);

			
			// Zoom Frame set Scale  
			double[] limt = getDiagramlimt();
			g2D.translate(selectorTranslateX, selectorTranslateY);
			overviewDiagramScale = Math.min((overviewRect.getWidth())/ limt[0],
					(overviewRect.getHeight())/ limt[1]);
			g2D.scale(overviewDiagramScale, overviewDiagramScale);
			paintDiagram(g2D);

			//set in Zoom Frame the visible Area
			g2D.scale(1/ overviewDiagramScale,1/ overviewDiagramScale);
			g2D.translate(-selectorTranslateX, -selectorTranslateY);

			//create rectangle to show the visible Area in Zoom Frame
			selector = new Rectangle2D.Double(selector.getX(),selector.getY(),getWidth()/scale* overviewDiagramScale,getHeight()/scale* overviewDiagramScale);

			//Place the rectangle in Zoom Frame in the right position -- rectangle must be in Zoom Frame			
			if (!overviewRect.contains(selector)){
				Rectangle2D tempR = overviewRect.createIntersection(selector);
				if (tempR.getHeight() == overviewRect.getHeight() &&
						tempR.getWidth() == overviewRect.getWidth())
					selector.setRect(tempR);
				else {
					double xChanges = selector.getWidth() - tempR.getWidth();
					double yChanges = selector.getHeight() - tempR.getHeight();
					selector.setRect(selector.getX() - xChanges, selector.getY() - yChanges, selector.getWidth(), selector.getHeight());
					updatePosition();
					//update the paint with new place of selector Rectangle
					paint(g2D);
				}
			}
			
			else {
				g2D.setStroke(new BasicStroke(selectorBoxStroke));
				g2D.setColor(new Color(0xffff0000));
				//resize the rectangle
				g2D.draw(new Rectangle2D.Double(selector.getX() - selectorBoxStroke /2, selector.getY() - selectorBoxStroke /2,
						selector.getWidth() + selectorBoxStroke, selector.getHeight() + selectorBoxStroke));

			}
			
		}
			
		}

		
	private void paintDiagram(Graphics2D g2D){
		for (Element element: model.getElements()){
			element.paint(g2D);
		}
	}

	//Zoom Frame cannot leave the diagram
	private double[] getDiagramlimt(){
		double xLimit = 50;
		double yLimit = 50;
		for (Element element : model.getElements()){
			if (element.getX() > xLimit)
				xLimit = element.getX();
			if (element.getY() > yLimit)
				yLimit = element.getY();
		}

		//size of area of zoom frame
		double offset = 200;
		xLimit += offset;
		yLimit += offset;

		double[] limt = {xLimit, yLimit};
		return limt;
		
	}
	
	public void setScale(double scale) {
		this.scale = scale;
		updatePosition();
	}
	public double getScale(){
		return scale;
	}
	public double getTranslateX() {
		return translateX;
	}
	public void setTranslateX(double translateX) {
		this.translateX = translateX;
	}
	public double getTranslateY() {
		return translateY;
	}
	public void setTranslateY(double tansslateY) {
		this.translateY = tansslateY;
	}

	public void updatePosition(){
		//set the relative position of Selector
		setTranslateX(-(selector.getX()-selectorTranslateX) / overviewDiagramScale * scale);
		setTranslateY(-(selector.getY()-selectorTranslateY) / overviewDiagramScale * scale);
	}
	public void updateMarker(int x, int y){


		//check position is valid
		if (overviewRect.contains(x, y, selector.getWidth(), selector.getHeight())){
			selector.setRect(x, y, selector.getWidth(), selector.getHeight());
		}
		
		//Update the position of selector
		updatePosition();
	}
	public void updateSelectorPosition(int x, int y){

		//check validity
		if (new Rectangle2D.Double(0,0,getWidth(),getHeight())
				.contains(selectorTranslateX+x,selectorTranslateY+y,overviewRect.getWidth(),overviewRect.getHeight())){
			selectorTranslateX += x;
			selectorTranslateY += y;

			//update marker as well
			selector.setRect(selector.getX() + x, selector.getY() + y, selector.getWidth(), selector.getHeight());
		}
	}
	public Rectangle2D getSelector(){
		return selector;
	}
	public boolean selectorContains(int x, int y){
		return selector.contains(x, y);
	}
	public boolean overviewContains(int x, int y) {return overviewRect.contains(x,y);}

}
 