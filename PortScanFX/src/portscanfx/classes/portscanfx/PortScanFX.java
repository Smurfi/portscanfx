package portscanfx;

import insidefx.undecorator.UndecoratorScene;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import portscanfx.controller.PortScanFxMainController;

/**
 * PortScanFX main class
 *
 * @author mkroll
 * @version 20170907
 */
public class PortScanFX extends Application {

    Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        primaryStage.setTitle("PortScanFX");

        URL location = getClass().getResource("/portscanfx/ui/PortScanFxMain.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        Region root = (Region) fxmlLoader.load(location.openStream());
        
        // Give stage to controller
        PortScanFxMainController controller = (PortScanFxMainController) fxmlLoader.getController();
        controller.setStage(primaryStage); // The Undecorator as a Scene

        final UndecoratorScene undecoratorScene = new UndecoratorScene(primaryStage, root);
        URL stylesheet = PortScanFX.class.getResource("/portscanfx/css/portscanfxmain.css");
        undecoratorScene.addStylesheet(stylesheet.toString());
        
        primaryStage.setScene(undecoratorScene);
        primaryStage.toFront();
        primaryStage.getIcons().add(new Image(PortScanFX.class.getResourceAsStream("gfx/network.png")));
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
