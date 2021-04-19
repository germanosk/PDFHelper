module me.germanosk.PDFHelper {
    requires javafx.controls;
    requires javafx.fxml;
	requires javafx.graphics;
	requires org.apache.pdfbox;

    opens me.germanosk.PDFHelper to javafx.fxml;
    exports me.germanosk.PDFHelper;
}