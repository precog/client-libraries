using System;
using System.Collections.Generic;

namespace Precog.Client.Dto
{
	public class AccountInfo
	{
		public String AccountId { get; set;}
	    public String Email { get; set;}
	    public DateTime AccountCreationDate { get; set;}
	    public String ApiKey { get; set;}
	    public String RootPath { get; set;}
	    public Dictionary<string, string> Plan { get; set;}

		public AccountInfo ()
		{
		}
	}
}

