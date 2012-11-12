using System;
using System.Collections.Generic;

namespace Precog.Client
{
	public enum ContentType
	{
        XZIP, ZIP, JSON, CSV
    }

	public class Request
	{
		public Dictionary<string, string> Parameters { get; private set;}
	    public Dictionary<string, string> Header { get; set; }
	    public string Body { get; set; }
	    public ContentType ContentType { get; set; }

	    public Request() {
			this.Body = "";
			this.ContentType = ContentType.JSON;
	        this.Parameters = new Dictionary<string, string>();
	        this.Header = new Dictionary<string, string>();
	    }

	}
}

