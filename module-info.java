module tfgirls.project.javarts {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires org.slf4j;


    opens tfgirls.project.javarts to javafx.fxml;
    opens tfgirls.project.javarts.View to com.fasterxml.jackson.databind;
    exports tfgirls.project.javarts;
}
