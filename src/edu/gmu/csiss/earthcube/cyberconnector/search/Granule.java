package edu.gmu.csiss.earthcube.cyberconnector.search;

import edu.gmu.csiss.earthcube.cyberconnector.products.Product;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Granule {

    private String name, iso_url, access_url, time_start, time_end;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIso_url() {
        return iso_url;
    }

    public void setIso_url(String iso_url) {
        this.iso_url = iso_url;
    }

    public String getAccess_url() {
        return access_url;
    }

    public void setAccess_url(String access_url) {
        this.access_url = access_url;
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

    public Granule() {
    }

    public Product toProduct(GranulesRequest gRequest) {
        Product p = new Product();

        String id = this.name.replaceAll("/", "_");
        p.setName(id);
        p.setId(id);
        p.setTitle(this.name);
        p.setDesc("From collection: " + gRequest.collection_desc);

        p.setBegintime(this.time_start);
        p.setEndtime(this.time_end);
        p.setWest(gRequest.west);
        p.setEast(gRequest.east);
        p.setNorth(gRequest.north);
        p.setSouth(gRequest.south);
        p.setAccessurl(this.access_url);
        p.setIfvirtual("0");
        p.setIsspatial("1");
        p.setIscollection("0");

        return p;
    }
}
