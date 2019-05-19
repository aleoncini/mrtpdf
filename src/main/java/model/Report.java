package model;

import it.redhat.mrtool.pdf.rest.ServiceClient;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bson.Document;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public class Report {
    private static final Logger logger = LoggerFactory.getLogger("it.redhat.mrtool");

    private boolean error = false;
    private String errorMessage;
    private String rhid;
    private String associateName;
    private String costCenter;
    private String carRegistryNUmber;
    private double carMileageRate;

    private int totalYearDistance;
    private int totalMonthDistance;
    private int year;
    private int month;
    private File reportFile;

    public Report(String redhatId, int year, int month){
        this.rhid = redhatId;
        this.year = year;
        this.month = month;
        getData();
    }

    private void getData() {
        logger.info("[Report] loading associate data...");
        getAssociateInfo();
        if (error){
            return;
        }
    }

    public boolean isValid(){
        return ! error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    private Report make(){
        //associate = new AssociateHelper().get(associateId);
        //logger.info("[Report] loaded " + associate.getName());

        //TripHelper helper = new TripHelper();

        logger.info("[Report] loading trips...");
        //trips = helper.getTrips(associateId, year, month);
        //totalMonthDistance = helper.distance(trips);
        //logger.info("[Report] loaded " + trips.size() + " trips");

        logger.info("[Report] loading trips...");
        //totalYearDistance = helper.getTotalYearDistance(associateId, year);
        //logger.info("[Report] loaded " + trips.size() + " trips");

        logger.info("[Report] formatting pdf report...");
        PageFormatter formatter = null;
        try {
            formatter = new PageFormatter().init();
            formatter.formatBanner();
            formatter.formatHeader();
            formatter.formatFooter();
            //formatter.formatHeaderInfo(associate, getPeriod(), totalMonthDistance, totalYearDistance);
            //formatter.formatTripsTable(trips);
            formatter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.info("[Report] saving file...");
        save(formatter.getDocument());

        logger.info("[Report] formatting complete.");
        return this;
    }

    private void getAssociateInfo() {
        String jsonString = new ServiceClient().invoke("/rs/associates/" + rhid);
        if (jsonString == null){
            error = true;
            errorMessage = "Unable to load Associate data";
            return;
        }
        Document document = Document.parse(jsonString);
        this.associateName = document.getString("name");
        this.costCenter = document.getString("costCenter");
        Document car = (Document) document.get("car");
        this.carRegistryNUmber = car.getString("registryNumber");
        this.carMileageRate = car.getDouble("mileageRate");
    }

    private String getPeriod() {
        return Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + year;
    }

    public String getFileName(){
        return rhid + "_" + year + "_" + month + ".pdf";
    }

    public File getReportFile(){
        return reportFile;
    }

    private void save(PDDocument document){
        String errorMessage = "";
        try {
            String path = getFileName();
            logger.info("[Report] saving " + path);
            document.save(path);
            reportFile = new File(path);
        } catch (Throwable t) {
            StringWriter trace = new StringWriter();
            t.printStackTrace(new PrintWriter(trace, true));
            logger.info(trace.toString());
        }
        if (errorMessage.length() > 0){
            System.out.println(errorMessage);
        }
    }

}
