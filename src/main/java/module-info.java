module group.bloodcellanalyser {
    requires javafx.controls;
    requires javafx.fxml;


    opens group.bloodcellanalyser to javafx.fxml;
    exports group.bloodcellanalyser;
}