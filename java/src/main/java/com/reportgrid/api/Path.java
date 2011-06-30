package com.reportgrid.api;

/**
 * A simple path representation that conforms to the path syntax
 * of the ReportGrid service API. 
 *
 * @author knuttycombe
 */
public class Path {
	protected final String path;

	/**
	 * Create a new path from the specified string. This path should
	 * consist of valid identifiers (alphanumeric strings) delimited
	 * by the "/" character.
	 * @param path 
	 */
	public Path(String path) {
		this.path = ("/" + path + "/").replaceAll("/+", "/");
	}

	/**
	 * Get the value of path
	 *
	 * @return the value of path
	 */
	public String getPath() {
		return path;
	}

	public Path append(Path that) {
		return new Path(path + that.path);
	}

	public Path append(Property that) {
		if (that == null) {
			return this;
		} else {
			return new Path(path + that.getValue());
		}
	}

	/**
	 * Convert the path to a relative path.
	 * @return 
	 */
	public String relativize() {
		return path.replaceFirst("/", "");
	}
}
