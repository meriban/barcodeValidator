module com.meriban.barcodevalidator {
    requires  transitive javafx.controls;
    requires transitive javafx.fxml;

    opens com.meriban.barcodevalidator to javafx.fxml, com.meriban.utilitiesroot;
    opens com.meriban.barcodevalidator.controllers to javafx.fxml, com.meriban.utilitiesroot;
    exports com.meriban.barcodevalidator to javafx.fxml, javafx.graphics, com.meriban.utilitiesroot;
    exports com.meriban.barcodevalidator.controllers to javafx.fxml, javafx.graphics, com.meriban.utilitiesroot;
}