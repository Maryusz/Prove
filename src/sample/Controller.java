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
import java.util.*;

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
    private TextField fieldIterazioni;

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

        gp.setFill(Color.valueOf("#1A3399"));
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

    private void setEndingPoint(double x, double y) {
        endPoint = new Point2D(x, y);
        pw.setColor((int) endPoint.getX(), (int) endPoint.getY(), Color.BLACK );

    }

    private void setStartingPoint(double x, double y) {
        startPoint = new Point2D(x, y);
        pw.setColor((int) startPoint.getX(), (int) startPoint.getY(), Color.BLACK );
        point2DList.add(startPoint);

    }

    private Point2D newPoint(Point2D p){
        int xLoc = random.nextInt(3) - 1;
        int yLoc = random.nextInt(3) - 1;
        return p.add(xLoc, yLoc);
    }


    @FXML
    void updatePb(ActionEvent event) {

        int iterazioni = Integer.parseInt(fieldIterazioni.getText());

        Task<Void> t = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Point2D  np = newPoint(startPoint);
                        pw.setColor((int)np.getX(), (int) np.getY(), colorPicker.getValue());
                        point2DList.add(np);

                        for (int i = 0; i < iterazioni; i++){

                            np = newPoint(np);
                            if (checkSovrapposizione.isSelected()) {

                                np = newPoint(np);

                            } else {
                                while (point2DList.contains(np)){
                                np = newPoint(np);
                                 }
                            }
                            point2DList.add(np);
                            updateProgress(i, iterazioni);

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


}

