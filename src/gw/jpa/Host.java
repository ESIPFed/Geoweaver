package gw.jpa;

import java.net.MalformedURLException;
import java.net.URL;

public class Host {

	private String id, name, ip, port, username, owner, type, url;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String[] parseJupyterURL() {
		
		String[] cc = new String[4];
		
		try {
			
			URL aURL = new URL(url);

			System.out.println("protocol = " + aURL.getProtocol());
			System.out.println("authority = " + aURL.getAuthority());
			System.out.println("host = " + aURL.getHost());
			System.out.println("port = " + aURL.getPort());
			System.out.println("path = " + aURL.getPath());
			System.out.println("query = " + aURL.getQuery());
			System.out.println("filename = " + aURL.getFile());
			System.out.println("ref = " + aURL.getRef());
			
			cc[0] = aURL.getProtocol();
			cc[1] = aURL.getHost();
			
			if(aURL.getPort()!=-1) {

				cc[2] = String.valueOf(aURL.getPort());
				
			}else {
				
				cc[2] = "80";
				
			}
			
			cc[3] = aURL.getPath();
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
		
//		if(url!=null) {
//			
//			String[] ss = url.split(":");
//			
//			String current_scheme = ss[0];
//			
//			String current_ip = ss[1].substring(2);
//			
////			int current_port = Integer.parseInt(ss[2].replaceAll("\\D", ""));
//			
//			cc[0] = current_scheme;
//			
//			cc[1] = current_ip;
//			
//			if(ss.length<2) {
//				
//				cc[2] = "80";
//				
//			}else {
//				
//				System.out.println(ss[2]);
//				
//				cc[2] = ss[2].replaceAll("\\D", "");
//				
//			}
//			
//		}
		
		return cc;
		
	}
	
}
