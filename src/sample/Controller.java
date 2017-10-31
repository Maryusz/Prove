package sample;

import com.gluonhq.charm.glisten.control.ProgressBar;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

public class Controller implements Initializable{

    @FXML
    private ProgressBar pb;

    @FXML
    private Label lb;

    @FXML
    private Canvas canvas;

    private Point2D startPoint, endPoint;
    private EventHandler<MouseEvent> mouseEventEventHandler;
    private GraphicsContext gp;
    private PixelWriter pw;
    private List<Point2D> point2DList;
    private Random random;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        point2DList = new ArrayList<>();
        random = new Random();

        gp = canvas.getGraphicsContext2D();
        pw = gp.getPixelWriter();


        mouseEventEventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (MouseEvent.MOUSE_PRESSED == event.getEventType()){
                    setStartingPoint(event.getX(), event.getY());
                } else if (MouseEvent.MOUSE_RELEASED == event.getEventType()){
                    setEndingPoint(event.getX(), event.getY());
                } else {
                   // System.out.println(event.getEventType());
                }
            }
        };

        canvas.setOnMouseClicked(mouseEventEventHandler);
        canvas.setOnMouseDragged(mouseEventEventHandler);
        canvas.setOnMouseEntered(mouseEventEventHandler);
        canvas.setOnMouseExited(mouseEventEventHandler);
        canvas.setOnMouseMoved(mouseEventEventHandler);
        canvas.setOnMousePressed(mouseEventEventHandler);
        canvas.setOnMouseReleased(mouseEventEventHandler);


    }

    private void setEndingPoint(double x, double y) {
        endPoint = new Point2D(x, y);
        System.out.println(startPoint.add(endPoint));
        pw.setColor((int) startPoint.getX(), (int) startPoint.getY(), Color.BLACK );
        pw.setColor((int) endPoint.getX(), (int) endPoint.getY(), Color.BLACK );
    }

    private void setStartingPoint(double x, double y) {
        startPoint = new Point2D(x, y);
        //System.out.println(startPoint);
    }


    @FXML
    void updatePb(ActionEvent event) {
        Task<Void> t = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int timesToRun = 100000;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < timesToRun; i++ ){
                            pw.setColor(random.nextInt(600), random.nextInt(600),
                                    Color.color(random.nextDouble(), random.nextDouble(),random.nextDouble()));

                            updateProgress(i, timesToRun);
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

    }


}

