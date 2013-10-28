package com.wds.tools.envers.cli.utils;

public class ConnectionUrl {
	public ConnectionUrl(String url) {
		this.url = url;
		this.urlType = resolveUrlType(url);
		this.databaseType = resolveDatabaseType(url);
	}

	private final String url;
	private final String urlType;
	private final String databaseType;

	public String getUrl() {
		return this.url;
	}

	public String getUrlType() {
		return this.urlType;
	}

	public String getDatabaseType() {
		return this.databaseType;
	}

	public boolean isJdbc() {
		return Consts.JDBC.equalsIgnoreCase(getUrlType());
	}

	private String resolveUrlType(String url) {
		String type = null;
		if (url != null) {
			type = url.substring(0, url.indexOf(":"));
		}
		return type;
	}

	private String resolveDatabaseType(String url) {
		String type = null;
		if (url != null) {
			url = url.substring((getUrlType() + ":").length());
			type = url.substring(0, url.indexOf(":"));
		}
		return type;
	}
}
