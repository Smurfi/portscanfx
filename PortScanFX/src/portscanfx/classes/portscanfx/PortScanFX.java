/*
 * Copyright 2019 mkroll.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
