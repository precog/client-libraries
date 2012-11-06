package com.precog.api;

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
		this.path = ("/" + path).replaceAll("/+", "/");
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
		return new Path(path + "/" + that.path);
	}

  /**
   * Return the prefix of this path. May return null if the path is only
   * one element long.
   */
  public Path getPrefix() {
    String[] components = path.split("/");
    if (components.length > 1) {
      StringBuilder prefix = new StringBuilder();
      for (int i = 0; i < components.length - 1; i++) prefix.append(components[i]).append("/");
      return new Path(prefix.toString());
    } else {
      return null;
    }
  }

	/**
	 * Convert the path to a relative path.
	 * @return 
	 */
	public String relativize() {
		return path.replaceFirst("/", "");
	}

	public String toString() {
		return path;
	}
}
