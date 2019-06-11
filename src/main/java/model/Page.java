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
    private Color darkBlue= new Color(0, 65, 85);

    public Page(){
        logger.info("[Page] initializing page...");
        init();
        logger.info("[Page] initialization complete. Now drawing template objects.");
        drawPageForm();
        logger.info("[Page] report ready.");
    }

    private void drawPageForm() {
        try {
            drawBanner();
            drawHeaderLines();
            drawHeaderLabels();
            drawFooterLines();
            drawFooterLabels();
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

    public Page setReport(Report report){
        fileName = REPORT_DIR + report.getFileName();
        drawReportData(report);
        return this;
    }

    private void drawReportData(Report report) {
        try {
            drawHeaderValues(report.getPeriod(),
                    report.getMonthlyDistance(),
                    report.getTotalYearDistance(),
                    report.getAssociateName(),
                    report.getCostCenter(),
                    report.getRhid(),
                    report.getCarRegistryNumber(),
                    report.getCarMileageRate());
            drawFooterValues(report.getMonthlyDistance(), report.getCarMileageRate());
            prepareTable(report.getTripLogs().size());
            writeLogs(report.getTripLogs());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeLogs(java.util.List<String[]> trips) throws IOException {
        float H = 10;
        float baseY = imageY - 125 - (H * 2);
        for (Object trip: trips) {
            baseY -= 10;
            writeLog(baseY, (String[]) trip);
        }
    }

    private void writeLog(float baseY, String[] trip) throws IOException {
        contentStream.setNonStrokingColor(0, 65, 83);
        contentStream.setFont(PDType1Font.HELVETICA, 8);

        float x1 = xLeft + 30;
        float x2 = 110;
        float x3 = 110 + (contentWidth * 0.35f);
        float x4 = xLeft + contentWidth - 20;
        float y = baseY + 2;

        contentStream.beginText();
        String text = trip[0];
        float text_width = (PDType1Font.HELVETICA.getStringWidth("" + text) / 1000.0f) * 8;
        contentStream.newLineAtOffset(x1 - text_width, y);
        contentStream.showText(text);
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(x2, y);
        contentStream.showText(trip[1]);
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(x3, y);
        contentStream.showText(trip[2]);
        contentStream.endText();

        contentStream.beginText();
        text_width = (PDType1Font.HELVETICA.getStringWidth("" + trip[3]) / 1000.0f) * 8;
        contentStream.newLineAtOffset(x4 - text_width, y);
        contentStream.showText(trip[3]);
        contentStream.endText();
    }

    private void prepareTable(int rows) throws IOException {
        float x1 = 40;
        float x2 = 100;
        float W = contentWidth * 0.35f;
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
        contentStream.addRect(x, startY -H, x1 + contentWidth - x, H);
        contentStream.fill();

        contentStream.setNonStrokingColor(Color.WHITE);

        float middleX1 = x1 + 30;
        float middleX2 = (x1 + contentWidth) - (((x1 + contentWidth) - (100 + (W * 2))) / 2);

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
            contentStream.lineTo(x1 + contentWidth, baseY);
            contentStream.stroke();

            if (isOdd(i)){
                contentStream.setNonStrokingColor(215, 245, 215);
                contentStream.addRect(x1, baseY, contentWidth, H);
                contentStream.fill();
            }
        }
    }

    private boolean isOdd(int number) {
        return number % 2 > 0;
    }

    private void drawHeaderValues(String period, int monthDistance, int yearDistance, String name, String ccenter, String rhid, String rnumber, double rate) throws IOException {
        writeCellValue(0, 0, name);
        writeCellValue(0, 1, ccenter);
        writeCellValue(0, 2, rhid);
        writeCellValue(1, 0, period);
        writeCellValue(1, 1, rnumber);
        writeCellValue(1, 2, "" + Double.toString(rate));
        int prevDistance = yearDistance - monthDistance;
        writeCellValue(2, 0, "" + monthDistance);
        writeCellValue(2, 1, "" + prevDistance);
        writeCellValue(2, 2, "" + yearDistance);
    }

    private void writeCellValue(int col, int row, String value) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
        contentStream.setNonStrokingColor(darkBlue);

        float text_width = (PDType1Font.HELVETICA_BOLD.getStringWidth(value) / 1000.0f) * 10;
        float cellWidth = contentWidth / 3;
        float x = xLeft + (cellWidth * col) + (cellWidth / 2) - (text_width / 2);
        float y = imageY - (40 * row) - 30;

        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(value);
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
        logger.info("[Page] report saved: " + fileName);
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

    private void drawHeaderLines() throws IOException {
        contentStream.setNonStrokingColor(darkBlue);
        contentStream.setLineWidth(3);

        contentStream.moveTo(xLeft, imageY - 1);
        contentStream.lineTo(xRight, imageY - 1);
        contentStream.stroke();
        contentStream.moveTo(xLeft, imageY - 123);
        contentStream.lineTo(xRight, imageY - 123);
        contentStream.stroke();


        contentStream.setLineWidth(.5f);
        contentStream.setLineDashPattern(new float[]{3,1}, 0);

        contentStream.moveTo(50, imageY - 42);
        contentStream.lineTo(xRight - 10, imageY - 42);
        contentStream.stroke();
        contentStream.moveTo(50, imageY - 83);
        contentStream.lineTo(xRight - 10, imageY - 83);
        contentStream.stroke();


        float cellWidth = (contentWidth - 20) / 3;
        contentStream.moveTo(50 + cellWidth, imageY - 115);
        contentStream.lineTo(50 + cellWidth, imageY - 10);
        contentStream.stroke();
        contentStream.moveTo(50 + (cellWidth*2), imageY - 115);
        contentStream.lineTo(50 + (cellWidth*2), imageY - 10);
        contentStream.stroke();
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

    private void writeCellLabel(PDPageContentStream contentStream, int col, int row, String header) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA, 6);
        contentStream.setNonStrokingColor(darkBlue);

        float cellWidth = contentWidth / 3;
        float text_width = (PDType1Font.HELVETICA.getStringWidth(header) / 1000.0f) * 6;
        float x = xLeft + (cellWidth * col) + (cellWidth / 2) - (text_width / 2);
        float y = imageY - (40 * row) - 18;

        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(header);
        contentStream.endText();
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

}
