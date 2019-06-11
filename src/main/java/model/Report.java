package model;

import it.redhat.mrtool.pdf.rest.ServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bson.Document;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Report {
    private static final Logger logger = LoggerFactory.getLogger("it.redhat.mrtool");

    private String rhid;
    private String associateName;
    private String costCenter;
    private String carRegistryNumber;
    private double carMileageRate;
    private List<String[]> tripLogs;

    private int totalYearDistance;
    private int totalMonthDistance;
    private int year;
    private int month;

    public Report(String redhatId, int year, int month){
        this.rhid = redhatId;
        this.year = year;
        this.month = month;
        getData();
    }

    private void getData() {
        logger.info("[Report] loading associate data...");
        getAssociateInfo();
        logger.info("[Report] loading trips data...");
        getMonthlyTrips();
        logger.info("[Report] loaded " + tripLogs.size() + " trips for this report.");
    }

    private void getMonthlyTrips() {
        tripLogs = new ArrayList<>();
        String jsonString = new ServiceClient().invoke("/rs/trips/" + rhid + "/" + year + "/" + month);
        if (jsonString == null){
            logger.error("[Report] Unable to load associate trips of the month!");
            return;
        }
        Document document = Document.parse(jsonString);
        totalYearDistance = document.getInteger("totalDistance");
        List<Document> trips = (List<Document>) document.get("trips");
        totalMonthDistance = 0;
        for (Document trip : trips) {
            Document location = (Document) trip.get("location");
            Document date = (Document) trip.get("date");
            String[] values = new String[4];
            values[0] = Integer.toString(date.getInteger("day"));
            values[1] = location.getString("destination");
            values[2] = trip.getString("purpose");
            int dist = location.getInteger("distance");
            totalMonthDistance += dist;
            values[3] = dist + ".0";
            tripLogs.add(values);
        }
    }

    private void getAssociateInfo() {
        String jsonString = new ServiceClient().invoke("/rs/associates/" + rhid);
        if (jsonString == null){
            logger.error("[Report] Unable to load associate info!");
            return;
        }
        Document document = Document.parse(jsonString);
        this.associateName = document.getString("name");
        this.costCenter = document.getString("costCenter");
        Document car = (Document) document.get("car");
        this.carRegistryNumber = car.getString("registryNumber");
        this.carMileageRate = car.getDouble("mileageRate");
    }

    public String getPeriod() {
        return Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + year;
    }

    public int getMonthlyDistance() {
        return totalMonthDistance;
    }

    public int getTotalYearDistance() {
        return totalYearDistance;
    }

    public String getAssociateName() {
        return associateName;
    }

    public String getCostCenter() {
        return costCenter;
    }

    public String getRhid() {
        return rhid;
    }

    public String getCarRegistryNumber() {
        return carRegistryNumber;
    }

    public double getCarMileageRate() {
        return carMileageRate;
    }

    public java.util.List<String[]> getTripLogs(){
        return tripLogs;
    }

    public String getFileName(){
        return rhid + "_" + year + "_" + month + ".pdf";
    }

}
