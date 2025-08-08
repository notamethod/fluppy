package com.notamethod.ebox.gui;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainController {

    @FXML
    private void openProductEditor() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ProductEditor.fxml"));
            Scene scene = new Scene(loader.load());

            //GameEditorController controller = loader.getController();
            //controller.setProduct(new Product(1, "Produit test", 19.99));

            Stage stage = new Stage();
            stage.setTitle("Édition du produit");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
