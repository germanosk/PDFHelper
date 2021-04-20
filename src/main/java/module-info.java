module me.germanosk.PDFHelper {
    requires javafx.controls;
    requires javafx.fxml;
	requires javafx.graphics;
	requires javafx.swing;
	requires org.apache.pdfbox;
	requires java.desktop;
	requires javafx.base;

    opens me.germanosk.PDFHelper to javafx.fxml;
    exports me.germanosk.PDFHelper;
}