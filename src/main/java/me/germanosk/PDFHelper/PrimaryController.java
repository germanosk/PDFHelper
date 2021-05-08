package me.germanosk.PDFHelper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.multipdf.PageExtractor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javafx.beans.property.StringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;

public class PrimaryController {

	@FXML
	ToggleGroup imageExtractionGroup;
	@FXML
	RadioButton imageExtractPage;
	@FXML
	RadioButton imageExtractAllPages;
	
	@FXML
	TextField imagePageTextField;
	@FXML
	ImageView imagePageMiniatureImageView;

	@FXML
	TextField pageTextField;
	@FXML
	Label pageNumberLabel;
	@FXML
	ImageView pageMiniatureImageView;

	@FXML
	VBox source;

	int pageCount = 0;
	int pdfScale = 1;
	File pdfFile;

	Boolean extractingImage = false;
	
	@FXML
	Button extractImageButton;
	@FXML
	Button extractPageButton; 

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
				pageCount = doc.getNumberOfPages();
				System.err.println(" it is a PDF");
				doc.close();
				pdfFile = db.getFiles().get(0);

				pageNumberLabel.setText(" / " + pageCount);
				pageTextField.setText("" + pageCount);
				imagePageTextField.setText("1");

			} catch (Exception e) {
				e.printStackTrace();
				System.err.println(" NOT a PDF");
			}
		}
		event.setDropCompleted(success);
		event.consume();
	}

	@FXML
	public void OnDragDetected(MouseEvent event) {

		System.err.println("Drag Detected");
		/* drag was detected, start a drag-and-drop gesture */
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
	public void OnPageInputChanged(StringProperty property, String oldValue, String newValue) {
		OnMiniatureInputChanged(property, oldValue, newValue, pageMiniatureImageView);
	}
	
	@FXML
	public void OnImageInputChanged(StringProperty property, String oldValue, String newValue) {
		OnMiniatureInputChanged(property, oldValue, newValue, imagePageMiniatureImageView);
	}
	
	private void OnMiniatureInputChanged(StringProperty property, String oldValue, String newValue, ImageView imageView) {
		int pageNumber = Integer.parseInt(newValue);
		if (pageNumber <= pageCount && pageNumber >= 0) {
			try {
				setPageMiniature(imageView, pageNumber, pdfFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			property.set(oldValue);
		}
	}

	@FXML
	public void extractPage() {
		extractPageButton.setDisable(true);
		if (pdfFile != null) {
			try {
				int pageToBeRemoved = Integer.parseInt(pageTextField.getText());
				convertPDF(pageToBeRemoved, pdfFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		extractPageButton.setDisable(false);
	}


	@FXML
	public void imageSelectOnePage() {
		imagePageTextField.setDisable(false);
	}
	
	@FXML
	public void imageSelectAllPages() {
		imagePageTextField.setDisable(true);
	}
	

	@FXML
	public void extractImages() {
		int pageNumber = Integer.parseInt(imagePageTextField.getText());
		if (extractingImage || pageNumber > pageCount) {
			return;
		}
		extractingImage = true;
		extractImageButton.setDisable(true);


		ImageHelper helper = new ImageHelper();
		PDDocument doc;

		
		try {

			doc = PDDocument.load(pdfFile);
			if(imageExtractAllPages.isSelected()) {
				System.out.println("Extracting image from all pages...");
				for(int page = 1; page <= pageCount; page++) {
					helper.extractImagesAtPage(doc, page, pdfFile.getName(), pdfFile.getParentFile().getAbsolutePath());
				}
			}
			else {
				System.out.println("Extracting one page...");
				helper.extractImagesAtPage(doc, pageNumber, pdfFile.getName(), pdfFile.getParentFile().getAbsolutePath());
			}
			doc.close();

		} catch (IOException e) {

			e.printStackTrace();

		}
		extractingImage = false;
		extractImageButton.setDisable(false);
	}

	private void convertPDF(int pageNumber, File file) throws IOException {
		String filename = file.getName();
		int pos = filename.lastIndexOf(".");
		String filenameWithoutExtension = filename.substring(0, pos);
		String outputFile = file.getParentFile().getAbsolutePath() + "/" + filenameWithoutExtension + " - page "
				+ pageNumber + ".pdf";

		PDDocument doc = PDDocument.load(file);
		PageExtractor pageExtractor = new PageExtractor(doc, pageNumber, pageNumber);
		PDDocument outuptDoc = pageExtractor.extract();
		outuptDoc.save(outputFile);
		outuptDoc.close();
		doc.close();
	}

	private void setPageMiniature(ImageView imageView, int pageNumber, File file) throws IOException {
		PDDocument doc = PDDocument.load(file);

		int pageIndex = pageNumber - 1;
		PDFRenderer renderer = new PDFRenderer(doc);
		BufferedImage img = renderer.renderImage(pageIndex, pdfScale);

		WritableImage fxImage = SwingFXUtils.toFXImage(img, null);
		imageView.setImage((Image) fxImage);
		doc.close();
	}
}
