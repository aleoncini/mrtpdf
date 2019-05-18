package model;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

public class PageFormatter {
    private static final Logger logger = LoggerFactory.getLogger("it.redhat.mrtool");

    private PDDocument document;
    private PDPageContentStream contentStream;
    private PDImageXObject bannerImage;

    private Color darkBlue= new Color(0, 65, 85);

    private float borderWidth = 40;
    private float width;
    private float xLeft, xRight, xMiddle;
    private float imageY, imageHeight;

    public void close() throws IOException {
        contentStream.close();
    }

    public PageFormatter init() throws IOException {
        document = new PDDocument();
        document.addPage(new PDPage());
        PDPage page = document.getPage(0);
        contentStream = new PDPageContentStream(document, page);

        //Path path = Paths.get(new ReportDirectory().getResourcesDirectoryPath().toString() + "RHBanner.png");
        //String path = new ReportDirectory().getResourcesDirectoryPath().toString() + "/RHBannerNew.png";
        //logger.info("[PageFormatter] loading banner from: " + path);

        //bannerImage = PDImageXObject.createFromFile(path, document);

        width = page.getBleedBox().getWidth() - (borderWidth * 2);
        xLeft = borderWidth;
        xRight = xLeft + width;
        xMiddle = xLeft + (width / 2);
        imageHeight = (width / bannerImage.getWidth()) * bannerImage.getHeight();
        imageY = page.getBleedBox().getHeight() - 40 - imageHeight;

        logger.info("[PageFormatter] formatter inited.");
        return this;
    }

    public void formatHeaderInfo(String period, int monthDistance, int yearDistance) throws IOException {
        //writeCellValue(contentStream, 0, 0, associate.getName());
        //writeCellValue(contentStream, 0, 1, associate.getCostCenter());
        //writeCellValue(contentStream, 0, 2, associate.getRedhatId());
        writeCellValue(contentStream, 1, 0, period);
        //writeCellValue(contentStream, 1, 1, associate.getCar().getRegistryNumber());
        //writeCellValue(contentStream, 1, 2, "" + associate.getCar().getMileageRate());
        int prevDistance = yearDistance - monthDistance;
        writeCellValue(contentStream, 2, 0, "" + monthDistance);
        writeCellValue(contentStream, 2, 1, "" + prevDistance);
        writeCellValue(contentStream, 2, 2, "" + yearDistance);
        //drawFooterValues(monthDistance, associate.getCar().getMileageRate());
    }

    public void formatFooter() throws IOException {
        drawFooterLines();
        drawFooterLabels();
    }

    private void drawFooterLabels() throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA, 6);
        contentStream.setNonStrokingColor(darkBlue);

        String text = "Total mileage for month/period (Km)";
        float text_width = (PDType1Font.HELVETICA.getStringWidth(text) / 1000.0f) * 6;

        contentStream.beginText();
        contentStream.newLineAtOffset(xRight - 60 - text_width, 87);
        contentStream.showText(text);
        contentStream.endText();

        text = "Total cost for month/period (Euro)";
        text_width = (PDType1Font.HELVETICA.getStringWidth(text) / 1000.0f) * 6;

        contentStream.beginText();
        contentStream.newLineAtOffset(xRight - 60 - text_width, 67);
        contentStream.showText(text);
        contentStream.endText();

    }

    private void drawFooterValues(int distance, double rate) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
        contentStream.setNonStrokingColor(darkBlue);

        String text = distance + ".0";
        float text_width = (PDType1Font.HELVETICA.getStringWidth(text) / 1000.0f) * 10;

        contentStream.beginText();
        contentStream.newLineAtOffset(xRight - 10 - text_width, 87);
        contentStream.showText(text);
        contentStream.endText();

        double cost = distance * rate;
        text = "" + cost;
        text_width = (PDType1Font.HELVETICA.getStringWidth(text) / 1000.0f) * 10;

        contentStream.beginText();
        contentStream.newLineAtOffset(xRight - 10 - text_width, 67);
        contentStream.showText(text);
        contentStream.endText();
    }

    public void formatHeader() throws IOException {
        drawHeaderLines();
        drawHeaderLabels();
    }

    private void drawHeaderLabels() throws IOException {
        writeCellLabel(contentStream, 0, 0, "associate");
        writeCellLabel(contentStream, 0, 1, "cost center");
        writeCellLabel(contentStream, 0, 2, "employee number");
        writeCellLabel(contentStream, 1, 0, "period");
        writeCellLabel(contentStream, 1, 1, "car registry number");
        writeCellLabel(contentStream, 1, 2, "mileage cost rate");
        writeCellLabel(contentStream, 2, 0, "this month total (km)");
        writeCellLabel(contentStream, 2, 1, "total from previous report (Km)");
        writeCellLabel(contentStream, 2, 2, "this year total including this month (Km)");
    }

    private void drawHeaderLines() throws IOException {
        contentStream.setNonStrokingColor(darkBlue);
        contentStream.setLineWidth(3);

        contentStream.drawLine(40, imageY - 1, xRight, imageY - 1);
        contentStream.drawLine(40, imageY - 123, xRight, imageY - 123);


        contentStream.setLineWidth(.5f);
        contentStream.setLineDashPattern(new float[]{3,1}, 0);

        contentStream.drawLine(50, imageY - 42, xRight - 10, imageY - 42);
        contentStream.drawLine(50, imageY - 83, xRight - 10, imageY - 83);


        float cellWidth = (width - 20) / 3;
        contentStream.drawLine(50 + cellWidth, imageY - 115, 50 + cellWidth, imageY - 10);
        contentStream.drawLine(50 + (cellWidth*2), imageY - 115, 50 + (cellWidth*2), imageY - 10);
    }

    private void drawFooterLines() throws IOException {
        contentStream.setNonStrokingColor(darkBlue);
        contentStream.setLineDashPattern(new float[]{}, 0);
        contentStream.setLineWidth(1);

        contentStream.moveTo(xLeft, 60);
        contentStream.lineTo(xRight, 60);
        contentStream.stroke();
        contentStream.moveTo(xLeft, 100);
        contentStream.lineTo(xRight, 100);
        contentStream.stroke();

        contentStream.setLineWidth(.5f);
        contentStream.setLineDashPattern(new float[]{3,1}, 0);

        contentStream.moveTo(xMiddle, 80);
        contentStream.lineTo(xRight, 80);
        contentStream.stroke();
    }

    public void formatBanner() throws IOException, URISyntaxException {
        contentStream.drawImage(bannerImage, xLeft, imageY, width, imageHeight);

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

    private void writeCellLabel(PDPageContentStream contentStream, int col, int row, String header) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA, 6);
        contentStream.setNonStrokingColor(darkBlue);

        float cellWidth = width / 3;
        float text_width = (PDType1Font.HELVETICA.getStringWidth(header) / 1000.0f) * 6;
        float x = xLeft + (cellWidth * col) + (cellWidth / 2) - (text_width / 2);
        float y = imageY - (40 * row) - 18;

        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(header);
        contentStream.endText();
    }

    private void writeCellValue(PDPageContentStream contentStream, int col, int row, String value) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
        contentStream.setNonStrokingColor(darkBlue);

        float text_width = (PDType1Font.HELVETICA_BOLD.getStringWidth(value) / 1000.0f) * 10;
        float cellWidth = width / 3;
        float x = xLeft + (cellWidth * col) + (cellWidth / 2) - (text_width / 2);
        float y = imageY - (40 * row) - 30;

        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(value);
        contentStream.endText();
    }

    public void formatTripsTable(java.util.List trips) throws IOException {
        if ((trips == null) || (trips.size() == 0)){
            return;
        }
        prepareTable(trips.size());
        writeLogs(trips);
    }

    private void writeLogs(java.util.List trips) throws IOException {
        float H = 10;
        float baseY = imageY - 125 - (H * 2);
        for (Object trip: trips) {
            baseY -= 10;
            //writeLog(baseY, trip);
        }
    }

    private void writeLog(float baseY) throws IOException {
        contentStream.setNonStrokingColor(0, 65, 83);
        contentStream.setFont(PDType1Font.HELVETICA, 8);

        float x1 = xLeft + 30;
        float x2 = 110;
        float x3 = 110 + (width * 0.35f);
        float x4 = xLeft + width - 20;
        float y = baseY + 2;

        contentStream.beginText();
        String text = "";// + trip.getDate().getDay();
        float text_width = (PDType1Font.HELVETICA.getStringWidth("" + text) / 1000.0f) * 8;
        contentStream.newLineAtOffset(x1 - text_width, y);
        contentStream.showText(text);
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(x2, y);
        //contentStream.showText(trip.getLocation().getDestination());
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(x3, y);
        //contentStream.showText(trip.getPurpose());
        contentStream.endText();

        contentStream.beginText();
        //text = trip.getLocation().getDistance() + ".0";
        text_width = (PDType1Font.HELVETICA.getStringWidth("" + text) / 1000.0f) * 8;
        contentStream.newLineAtOffset(x4 - text_width, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    private void prepareTable(int rows) throws IOException {
        float x1 = 40;
        float x2 = 100;
        float W = width * 0.35f;
        float H = 10;
        float startY = imageY - 125 - H;

        contentStream.setNonStrokingColor(Color.DARK_GRAY);

        contentStream.addRect(x1, startY -H, 59, H);
        contentStream.fill();

        contentStream.addRect(x2, startY -H, W, H);
        contentStream.fill();

        contentStream.addRect(x2 + W + 1, startY -H, W, H);
        contentStream.fill();

        float x = x2 + 2 * (W + 1);
        contentStream.addRect(x, startY -H, x1 + width - x, H);
        contentStream.fill();

        contentStream.setNonStrokingColor(Color.WHITE);

        float middleX1 = x1 + 30;
        float middleX2 = (x1 + width) - (((x1 + width) - (100 + (W * 2))) / 2);

        String text = "DAY";
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 8);
        float text_width = (PDType1Font.HELVETICA_BOLD.getStringWidth(text) / 1000.0f) * 8;
        contentStream.newLineAtOffset(middleX1 - (text_width / 2), startY - 8);
        contentStream.showText(text);
        contentStream.endText();

        text = "From office to location";
        contentStream.beginText();
        text_width = (PDType1Font.HELVETICA_BOLD.getStringWidth(text) / 1000.0f) * 8;
        contentStream.newLineAtOffset(x2 + (W / 2) - (text_width / 2), startY - 8);
        contentStream.showText(text);
        contentStream.endText();

        text = "Purpose and/or Customer";
        contentStream.beginText();
        text_width = (PDType1Font.HELVETICA_BOLD.getStringWidth(text) / 1000.0f) * 8;
        contentStream.newLineAtOffset(x2 + (W * 1.5f) - (text_width / 2), startY - 8);
        contentStream.showText(text);
        contentStream.endText();

        text = "Mileage (km)";
        contentStream.beginText();
        text_width = (PDType1Font.HELVETICA_BOLD.getStringWidth(text) / 1000.0f) * 8;
        contentStream.newLineAtOffset(middleX2 - (text_width / 2), startY - 8);
        contentStream.showText(text);
        contentStream.endText();

        contentStream.setNonStrokingColor(5, 71, 5);
        contentStream.setLineDashPattern(new float[]{3,1}, 0);

        for (int i = 0; i < rows; i++){
            float baseY = startY - (H * 2) - (H * i);
            contentStream.moveTo(x1, baseY);
            contentStream.lineTo(x1 + width, baseY);
            contentStream.stroke();

            if (isOdd(i)){
                contentStream.setNonStrokingColor(215, 245, 215);
                contentStream.addRect(x1, baseY, width, H);
                contentStream.fill();
            }
        }
    }

    private boolean isOdd(int number) {
        return number % 2 > 0;
    }

    public PDDocument getDocument() {
        return document;
    }
}