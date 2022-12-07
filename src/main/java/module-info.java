module com.meriban.barcodevalidator {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires org.xerial.sqlitejdbc;
    requires transitive java.sql;
    requires java.desktop;
    requires org.jetbrains.annotations;

    opens com.meriban.barcodevalidator to javafx.fxml, com.meriban.utilitiesroot, org.xerial.sqlitejdbc, java.sql;
    opens com.meriban.barcodevalidator.controllers to javafx.fxml, com.meriban.utilitiesroot;
    exports com.meriban.barcodevalidator to javafx.fxml, javafx.graphics, com.meriban.utilitiesroot;
    exports com.meriban.barcodevalidator.controllers to javafx.fxml, javafx.graphics, com.meriban.utilitiesroot;
    exports com.meriban.barcodevalidator.navigators to com.meriban.utilitiesroot, javafx.fxml, javafx.graphics;
    opens com.meriban.barcodevalidator.navigators to com.meriban.utilitiesroot, javafx.fxml;
    exports com.meriban.barcodevalidator.managers to com.meriban.utilitiesroot, javafx.fxml, javafx.graphics;
    opens com.meriban.barcodevalidator.managers to com.meriban.utilitiesroot, java.sql, javafx.fxml, org.xerial.sqlitejdbc;
}