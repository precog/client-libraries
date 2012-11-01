using System;
using System.Collections.Generic;

namespace Precog.Client.Options
{
	public class CSVIngestOptions : IngestOptions
	{

	    public static string QUOTE = "quote";
	    public static string ESCAPE = "escape";
	    public static string DELIMITER = "delimiter";

	    public string delimiter {get; set;}
	    public string quote {get; set;}
	    public string escape {get; set;}

	    public CSVIngestOptions(): base(ContentType.CSV)
		{
			
	    }

	    public override Dictionary<string, string> asMap()
		{
	        Dictionary<string, string> map = base.asMap();
	        if (quote != null) {
	            map.Add(QUOTE, quote);
	        }
	        if (delimiter != null) {
	            map.Add(DELIMITER, delimiter);
	        }
	        if (escape != null) {
	            map.Add(ESCAPE, escape);
	        }
	        return map;
	    }

	}
}

