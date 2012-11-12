using System;
using System.Collections.Generic;

namespace Precog.Client.Options
{
	public class IngestOptions
	{

		public static string OWNER_ACCOUNT_ID = "ownerAccountId";

	    public ContentType DataType { get; set;}
	    public string OwnerAccountId { get; set;}
	    public bool Async { get; set;}

	    public IngestOptions(ContentType dataType) {
	        this.DataType = dataType;
	    }

	    public virtual Dictionary<string, string> asMap() {
	        Dictionary<string, string> map = new Dictionary<string, string>();
	        if (OwnerAccountId != null) {
	            map.Add(OWNER_ACCOUNT_ID, OwnerAccountId);
	        }
	        return map;
	    }
	}
}

