package edu.gmu.csiss.earthcube.cyberconnector.search;

import java.util.ArrayList;
import java.util.List;

import edu.gmu.csiss.earthcube.cyberconnector.products.Product;

/**
*Class SearchResponse.java
*@author Ziheng Sun
*@time Feb 3, 2017 10:43:29 AM
*Original aim is to support CyberConnector.
*/
public class SearchResponse {
	
	int product_total_number;
	
	int startposition;
	
	int recordsperpage;
	
	/**
	 * The following three variables are added for JQuery DataTable
	 * Refer to https://datatables.net/manual/server-side
	 */
	
	int draw;
	
	int recordsTotal;
	
	int recordsFiltered;
	
	List<Product> products;
	
	public int getRecordsTotal() {
		return recordsTotal;
	}

	public void setRecordsTotal(int recordsTotal) {
		this.recordsTotal = recordsTotal;
	}

	public int getRecordsFiltered() {
		return recordsFiltered;
	}

	public void setRecordsFiltered(int recordsFiltered) {
		this.recordsFiltered = recordsFiltered;
	}

	public int getDraw() {
		return draw;
	}

	public void setDraw(int draw) {
		this.draw = draw;
	}

	public int getStartposition() {
		return startposition;
	}

	public void setStartposition(int startposition) {
		this.startposition = startposition;
	}

	public int getRecordsperpage() {
		return recordsperpage;
	}

	public void setRecordsperpage(int recordsperpage) {
		this.recordsperpage = recordsperpage;
	}

	public int getProduct_total_number() {
		return product_total_number;
	}

	public void setProduct_total_number(int product_total_number) {
		this.product_total_number = product_total_number;
	}

	public List<Product> getProducts() {
		return products;
	}

	public void setProducts(List<Product> products) {
		this.products = products;
	}
	
}
