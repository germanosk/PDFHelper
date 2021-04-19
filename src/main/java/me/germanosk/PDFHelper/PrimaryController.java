package me.germanosk.PDFHelper;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.multipdf.PageExtractor;
import org.apache.pdfbox.pdmodel.PDDocument;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;

public class PrimaryController {

	@FXML
	TextField pageTextField;
	@FXML
	Label pageNumberLabel;
	
	@FXML
	VBox source;
	
	File pdfFile;
	
    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
    
    @FXML
    public void OnDragOver(DragEvent event) {
    	
		 System.out.println("Drag Over");
		 event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
		 
		 Dragboard db = event.getDragboard();
         if (db.hasFiles()) {
    		 System.out.println("File Over");
         }
         event.consume();
    }
    
    @FXML
    public void OnDragDropped(DragEvent event) {
    	
		System.err.println("Drag Dropped");
    	
		Dragboard db = event.getDragboard();
		boolean success = false;
		if (db.hasFiles()) {
		    System.out.println("Dropped: " + db.getFiles().get(0).getAbsolutePath());
		    success = true;
		    try {
		    	PDDocument doc = PDDocument.load(db.getFiles().get(0));
			    System.err.println(" it is a PDF" );
			    pageNumberLabel.setText(" / "+doc.getNumberOfPages());
                pageTextField.setText(""+doc.getNumberOfPages());
                doc.close();
                pdfFile = db.getFiles().get(0);
		    	
		    }catch (Exception e) {
			    System.err.println(" NOT a PDF" );
		    	
			}
		    
		}
		event.setDropCompleted(success);
		event.consume();
    }
    
    @FXML
    public void OnDragDetected(MouseEvent event) {
    	
		System.err.println("Drag Detected");
        /* drag was detected, start a drag-and-drop gesture*/
        /* allow any transfer mode */
        Dragboard db = source.startDragAndDrop(TransferMode.ANY);
        
        /* Put a string on a dragboard */
        ClipboardContent content = new ClipboardContent();
       // content.putString(source.getText());
        db.setContent(content);
        
        event.consume();
    }

    @FXML
    public void OnDragDone(DragEvent event) {
		System.err.println("Drag Done");
    	
    }
    
    @FXML
    public void extract() {
    	if(pdfFile != null) {
    		try{
    			int pageToBeRemoved = Integer.parseInt(pageTextField.getText());
                convertPDF(pageToBeRemoved, pdfFile);
            }catch(IOException e){
                e.printStackTrace();
            }
    	}
    }
    
    private void convertPDF(int pageNumber, File file) throws IOException{
        String outputFile = file.getParentFile().getAbsolutePath()+"/COPY.pdf";
       
        PDDocument doc = PDDocument.load(file);      
        PageExtractor pageExtractor = new PageExtractor(doc, pageNumber, pageNumber);
        PDDocument outuptDoc = pageExtractor.extract();
        outuptDoc.save(outputFile);
        outuptDoc.close();
        doc.close();
    }
}
