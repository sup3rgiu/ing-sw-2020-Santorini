package it.polimi.ingsw.psp40.view.gui;

import it.polimi.ingsw.psp40.model.Player;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;

;

public class WaitingPopup extends PopupStage {

    private String details;

    WaitingPopup(Stage ownerStage, String details) {
        super(ownerStage);
        this.details = details;
        build();
    }


    private void build() {
        // Create scene
        Text text = createText(details);
        this.vBox.getChildren().add(text);
        ScreenController.addClassToElement(vBox, "waiting-popup");
    }

}