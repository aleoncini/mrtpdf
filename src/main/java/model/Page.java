package model;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;

public class Page {
    private static final Logger logger = LoggerFactory.getLogger("it.redhat.mrtool");
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
        logger.info("[Page] initializing page...");
        init();
        logger.info("[Page] initialization complete. Now drawing template objects.");
        drawPageForm();
        logger.info("[Page] Template ready.");
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
            bannerImage = PDImageXObject.createFromFile(REPORT_DIR + "RHBannerNew.png", document);
            contentStream = new PDPageContentStream(document, page);
        } catch (Throwable t) {
            StringWriter trace = new StringWriter();
            t.printStackTrace(new PrintWriter(trace, true));
            logger.warn(trace.toString());
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
        logger.info("[Page] saving " + fileName);
        try {
            if (contentStream != null){
                contentStream.close();
                contentStream = null;
            }
            document.save(fileName);
        } catch (Throwable t) {
            StringWriter trace = new StringWriter();
            t.printStackTrace(new PrintWriter(trace, true));
            logger.warn(trace.toString());
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
