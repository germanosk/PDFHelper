package me.germanosk.PDFHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceEntry;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 * This class is an adaptation from ExtractImagesFromPDF class from
 * https://github.com/RPTools/TokenTool project.
 * 
 * @author germanosk
 *
 */
public class ImageHelper {

	private PDDocument document;
	private final Set<COSStream> imageTracker = new HashSet<COSStream>();

	private int currentPageIndex;
	private String pdfName;
	private String path;

	private boolean isRunning;
	private boolean interrupt;

	public void extractImagesAtPage(PDDocument document, int pageNumber, String pdfName, String path) {
		this.document = document;
		currentPageIndex = pageNumber - 1;
		this.pdfName = pdfName;
		this.path = path;
		isRunning = true;
		interrupt = false;
		System.out.println("PDF name: " + pdfName);
		System.out.println("Path: " + path);
		try {
			extractAnnotationImages(document.getPage(currentPageIndex));

			getImagesFromResources(document.getPage(currentPageIndex).getResources());
		} catch (IOException e) {
			e.printStackTrace();
		}
		isRunning = false;
		interrupt = false;
	}

	private void getImagesFromResources(PDResources resources) throws IOException {
		ArrayList<COSName> xObjectNamesReversed = new ArrayList<>();

		for (COSName xObjectName : resources.getXObjectNames()) {
			xObjectNamesReversed.add(xObjectName);
		}

		Collections.reverse(xObjectNamesReversed);

		for (COSName xObjectName : xObjectNamesReversed) {
			if (interrupt)
				return;

			PDXObject xObject = resources.getXObject(xObjectName);

			if (xObject instanceof PDFormXObject) {
				getImagesFromResources(((PDFormXObject) xObject).getResources());
			} else if (xObject instanceof PDImageXObject) {
				if (!imageTracker.contains(xObject.getCOSObject())) {
					imageTracker.add(xObject.getCOSObject());
					String name = pdfName + " - pg " + (currentPageIndex + 1) + " - " + xObjectName.getName();
					System.out.println("Extracting image... " + name);

					SaveImage(SwingFXUtils.toFXImage(((PDImageXObject) xObject).getImage(), null), name);
				}
			}
		}
	}

	private void extractAnnotationImages(PDPage page) throws IOException {
		for (PDAnnotation annotation : page.getAnnotations()) {
			extractAnnotationImages(annotation);
		}
	}

	private void extractAnnotationImages(PDAnnotation annotation) throws IOException {
		PDAppearanceDictionary appearance = annotation.getAppearance();

		if (appearance == null)
			return;

		extractAnnotationImages(appearance.getDownAppearance());
		extractAnnotationImages(appearance.getNormalAppearance());
		extractAnnotationImages(appearance.getRolloverAppearance());
	}

	private void extractAnnotationImages(PDAppearanceEntry appearance) throws IOException {
		if (interrupt)
			return;

		PDResources resources = appearance.getAppearanceStream().getResources();
		if (resources == null)
			return;

		for (COSName cosname : resources.getXObjectNames()) {
			PDXObject xObject = resources.getXObject(cosname);

			if (xObject instanceof PDFormXObject)
				extractAnnotationImages((PDFormXObject) xObject);
			else if (xObject instanceof PDImageXObject)
				extractAnnotationImages((PDImageXObject) xObject);
		}
	}

	private void extractAnnotationImages(PDFormXObject form) throws IOException {
		PDResources resources = form.getResources();
		if (resources == null)
			return;

		for (COSName cosname : resources.getXObjectNames()) {
			PDXObject xObject = resources.getXObject(cosname);

			if (xObject instanceof PDFormXObject)
				extractAnnotationImages((PDFormXObject) xObject);
			else if (xObject instanceof PDImageXObject)
				extractAnnotationImages((PDImageXObject) xObject);
		}
	}

	private void extractAnnotationImages(PDImageXObject xObject) throws IOException {
		if (!imageTracker.contains(xObject.getCOSObject())) {

			String name = pdfName + " - pg " + (currentPageIndex + 1) + " - img " + imageTracker.size();

			System.out.println("Extracting Annotation, eg button image... " + name);

			imageTracker.add(xObject.getCOSObject());
			SaveImage(SwingFXUtils.toFXImage(xObject.getImage(), null), name);

		}
	}

	private void SaveImage(Image buttonImage, String imageName) {

		try {
			File tempImageFile;
			String filename = generateFilePathAndName(imageName);
			tempImageFile = new File(filename);
			System.out.println("Image: " + tempImageFile.getAbsolutePath());

			ImageIO.write(SwingFXUtils.fromFXImage(buttonImage, null), "png", tempImageFile);
		} catch (IOException e) {
			System.err.println("Unable to write token to file: " + imageName);
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	private String generateFilePathAndName(String imageName) {
		int number = 1;
		String filePathAndName = path + "/" + imageName + ".png";
		while (new File(filePathAndName).exists()) {
			filePathAndName = path + "/" + imageName + number + ".png";
			number++;
		}
		return filePathAndName;
	}

}
