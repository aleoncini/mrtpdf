package model;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;

public class Page {

    private final static String REPORT_DIR = "/reports/";

    private PDDocument document;
    private PDPageContentStream contentStream;
    private PDImageXObject bannerImage;
    private String fileName = REPORT_DIR + "template.pdf";

    private float borderWidth = 40;
    private float contentWidth;
    private float xLeft, xRight, xMiddle;
    private float imageY, topY;

    public Page(){
        init();
        drawPageForm();
    }

    private void drawPageForm() {
        try {
            drawBanner();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init(){
        document = new PDDocument();
        document.addPage(new PDPage());
        PDPage page = document.getPage(0);
        try {
            contentStream = new PDPageContentStream(document, page);
            URL resource = this.getClass().getResource("RHBannerNew.png");

            //Path path = Paths.get(new ReportDirectory().getResourcesDirectoryPath().toString() + "RHBanner.png");
            //String path = new ReportDirectory().getResourcesDirectoryPath().toString() + "/RHBannerNew.png";
            //logger.info("[PageFormatter] loading banner from: " + path);

            bannerImage = PDImageXObject.createFromFile(resource.getPath(), document);
        } catch (IOException e) {
            e.printStackTrace();
        }

        contentWidth = page.getBleedBox().getWidth() - (borderWidth * 2);
        xLeft = borderWidth;
        xRight = xLeft + contentWidth;
        xMiddle = xLeft + (contentWidth / 2);
        float imageHeight = (contentWidth / bannerImage.getWidth()) * bannerImage.getHeight();
        topY = page.getBleedBox().getHeight() - borderWidth;
        imageY = topY - imageHeight;
    }

    public void format(Report report){
        fileName = REPORT_DIR + report.getFileName();
    }

    public boolean save(){
        try {
            document.save(fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void drawBanner() throws IOException, URISyntaxException {
        float imageHeight = topY -imageY;
        contentStream.drawImage(bannerImage, xLeft, imageY, contentWidth, imageHeight);

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
        contentStream.setNonStrokingColor(Color.white);
        contentStream.newLineAtOffset(65, imageY + 36);
        contentStream.showText("Business Mileage Form");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.COURIER, 10);
        contentStream.setNonStrokingColor(Color.white);
        contentStream.newLineAtOffset(65, imageY + 16);
        contentStream.showText("Produced by MRTool");
        contentStream.endText();
    }

}
