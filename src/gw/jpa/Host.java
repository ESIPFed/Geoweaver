package gw.jpa;

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
		
		String[] cc = new String[3];
		
		if(url!=null) {
			
			String[] ss = url.split(":");
			
			String current_scheme = ss[0];
			
			String current_ip = ss[1].substring(2);
			
//			int current_port = Integer.parseInt(ss[2].replaceAll("\\D", ""));
			
			cc[0] = current_scheme;
			
			cc[1] = current_ip;
			
			cc[2] = ss[2].replaceAll("\\D", "");
			
		}
		
		return cc;
		
	}
	
}
