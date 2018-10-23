package edu.gmu.csiss.earthcube.cyberconnector.search;

public class GranulesRequest {
    String collection_url, collection_name, collection_desc, time_start, time_end;

    double west = -180, east = 180, south = -90, north = 90;

    int recordsperpage = 5;

    public GranulesRequest() {}

    public int getRecordsperpage() {
        return recordsperpage;
    }

    public void setRecordsperpage(int recordsperpage) {
        this.recordsperpage = recordsperpage;
    }

    public String getCollection_url() {
        return collection_url;
    }

    public void setCollection_url(String collection_url) {
        this.collection_url = collection_url;
    }

    public String getCollection_name() {
        return collection_name;
    }

    public void setCollection_name(String collection_name) {
        this.collection_name = collection_name;
    }

    public String getCollection_desc() {
        return collection_desc;
    }

    public void setCollection_desc(String collection_desc) {
        this.collection_desc = collection_desc;
    }

    public String getTime_start() {
        return time_start;
    }

    public void setTime_start(String time_start) {
        this.time_start = time_start;
    }

    public String getTime_end() {
        return time_end;
    }

    public void setTime_end(String time_end) {
        this.time_end = time_end;
    }

    public double getWest() {
        return west;
    }

    public void setWest(double west) {
        this.west = west;
    }

    public double getEast() {
        return east;
    }

    public void setEast(double east) {
        this.east = east;
    }

    public double getSouth() {
        return south;
    }

    public void setSouth(double south) {
        this.south = south;
    }

    public double getNorth() {
        return north;
    }

    public void setNorth(double north) {
        this.north = north;
    }
}
