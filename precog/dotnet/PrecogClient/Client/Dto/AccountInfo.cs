using System;
using System.Collections.Generic;

namespace Precog.Client.Dto
{
	public class AccountInfo
	{
		public string accountId { get; set;}
	    public string email { get; set;}
	    public DateTime AccountCreationDate { get; set;}
	    public string ApiKey { get; set;}
	    public string rootPath { get; set;}
	    public Dictionary<string, string> plan { get; set;}

		public AccountInfo ()
		{
		}
	}
}

