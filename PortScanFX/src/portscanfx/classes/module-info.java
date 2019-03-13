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
module portscanfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires UndecoratorBis;
    requires jdom;
    requires javafx.swt;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.media;
    requires javafx.swing;
    requires javafx.web;
    
    opens portscanfx to javafx.fxml;
    opens portscanfx.controller to javafx.fxml;
    
    exports portscanfx; 
    exports portscanfx.controller;
}
