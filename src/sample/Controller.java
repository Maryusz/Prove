package sample;

import com.gluonhq.charm.glisten.control.ProgressBar;
import com.gluonhq.charm.glisten.control.TextField;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.HashSet;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Set;



public class Controller implements Initializable{

    @FXML
    private ProgressBar pb;

    @FXML
    private Label lb;

    @FXML
    private Canvas canvas;

    @FXML
    private ColorPicker colorPicker;

    @FXML
    private TextField fieldIterazioni, lineComplexity;

    @FXML
    private CheckBox checkSovrapposizione;


    private Point2D startPoint, endPoint;
    private EventHandler<MouseEvent> mouseEventEventHandler;
    private GraphicsContext gp;
    private PixelWriter pw;
    private Set<Point2D> point2DList;
    private Random random;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        point2DList = new HashSet<>();
        random = new Random();

        gp = canvas.getGraphicsContext2D();

        gp.setFill(Color.valueOf("#FFFFFF"));
        gp.fillRect(0,0, canvas.getWidth(), canvas.getHeight());
        gp.stroke();
        pw = gp.getPixelWriter();

        fieldIterazioni.setMaxLength(6);
        canvas.setCache(true);


        mouseEventEventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (MouseEvent.MOUSE_PRESSED == event.getEventType()){
                    setStartingPoint(event.getX(), event.getY());
                } else if (MouseEvent.MOUSE_RELEASED == event.getEventType()){
                    setEndingPoint(event.getX(), event.getY());

                    //Draw the line n times specified in the text field

                    for (int i = 0; i < Integer.parseInt(lineComplexity.getText()); i++) {
                        drawRandomLine();
                    }

                }else if (MouseEvent.MOUSE_DRAGGED == event.getEventType()){
                    drawLine(event.getX(), event.getY());
                }
                else {
                   // System.out.println(event.getEventType());
                }
            }
        };

        canvas.setOnMousePressed(mouseEventEventHandler);
        canvas.setOnMouseReleased(mouseEventEventHandler);


    }

    private void drawLine(double x, double y) {
        pw.setColor((int) x, (int) y, colorPicker.getValue());
    }

    /**
     * Sets the end point of the random line taking coordinates from the mouse event (release)
     *
     * @param x
     * @param y
     */
    private void setEndingPoint(double x, double y) {
        endPoint = new Point2D(x, y);
        pw.setColor((int) endPoint.getX(), (int) endPoint.getY(), Color.BLACK );

    }

    /**
     * Sets the starting point taking coordinates from the mouse event (pressure)
     * @param x
     * @param y
     */
    private void setStartingPoint(double x, double y) {
        startPoint = new Point2D(x, y);
        pw.setColor((int) startPoint.getX(), (int) startPoint.getY(), Color.BLACK );
        point2DList.add(startPoint);

    }

    /**
     * Generate a new Point2D taking the start point as reference.
     * To create a random line it search the closest path to end point.
     * @param p
     * @return
     */
    private Point2D newPoint(Point2D p){

        double distance = p.distance(endPoint);
        Point2D nPoint;

        int xLoc = 0;
        int yLoc = 0;
        //Effettua 2 prove per vedere la distanza minore all'endpoint
        for (int i = 0; i < 2; i++) {
            xLoc = random.nextInt(3) - 1;
            yLoc = random.nextInt(3) - 1;

            nPoint = p.add(xLoc, yLoc);
            if (nPoint.distance(endPoint) < distance) {
                return nPoint;
            }
        }
        return p.add(xLoc, yLoc);

    }


    @FXML
    void updatePb(ActionEvent event) {
        //TODO: Must generate a shape for automatic construction
    }


    /**
     * Returns the discance from a given point to the end point
     * @param point2D
     * @return
     */
    private double checkDistance(Point2D point2D) {
        return point2D.distance(endPoint);

    }

    private void drawRandomLine() {
        int iterazioni = Integer.parseInt(fieldIterazioni.getText());

        Task<Void> t = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Point2D  np = newPoint(startPoint);
                        pw.setColor((int)np.getX(), (int) np.getY(), colorPicker.getValue());

                        for (int i = 0; i < iterazioni; i++){
                            np = newPoint(np);
                            if (checkDistance(np) < 2.0) {
                                break;
                            }

                            if (checkSovrapposizione.isSelected()) {
                                np = newPoint(np);
                            } else {
                                int count = 0;

                                while (point2DList.contains(np)){
                                    np = newPoint(np);

                                    count++;
                                    // If theres no other way it breakes the loop.
                                    if (count > 14) {
                                        break;
                                    }
                                }
                            }
                            point2DList.add(np);

                            updateProgress(i, iterazioni);
                            antialiasPoint(np, colorPicker.getValue());
                            pw.setColor((int)np.getX(), (int) np.getY(), colorPicker.getValue() );
                        }
                    }
                });

                return null;
            }
        };

        Thread th = new Thread(t);
        th.setDaemon(true);
        th.start();

        pb.progressProperty().bind(t.progressProperty());
        lb.setText(String.format("%d pixel scritti", point2DList.size()));
    }

    /**
     * This method draw a more transparent pixels around the passed Point 0.4 of the passed color.
     * It's a primitive try to antialias the line.
     *
     * @param point2D
     * @param color
     */
    private void antialiasPoint(Point2D point2D, Color color) {

        // Create the opacized color from the passed color.
        Color opacizedColor = Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.4);

        //Gets the original x and y positions from the passed point
        int xPosition = (int) point2D.getX();
        int yPosition = (int) point2D.getY();

        // Create the new points where to draw opacized pixels
        Point2D lessXPoint2D = new Point2D(xPosition - 1, yPosition);
        Point2D moreXPoint2D = new Point2D(xPosition + 1, yPosition);
        Point2D lessYPoint2D = new Point2D(xPosition, yPosition - 1);
        Point2D moreYPoint2D = new Point2D(xPosition, yPosition + 1);


        // If there no already a point there it draw the new point with opacized color.
        if (!point2DList.contains(lessXPoint2D)) {
            pw.setColor((int) lessXPoint2D.getX(), (int) lessXPoint2D.getY(), opacizedColor);
        }
        if (!point2DList.contains(moreXPoint2D)) {
            pw.setColor((int) moreXPoint2D.getX(), (int) moreXPoint2D.getY(), opacizedColor);
        }
        if (!point2DList.contains(lessYPoint2D)) {
            pw.setColor((int) lessYPoint2D.getX(), (int) lessYPoint2D.getY(), opacizedColor);
        }
        if (!point2DList.contains(moreYPoint2D)) {
            pw.setColor((int) moreYPoint2D.getX(), (int) moreYPoint2D.getY(), opacizedColor);
        }
        
    }


}

