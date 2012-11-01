using System;

namespace Precog.Client.Dto
{
	public class IngestResult
	{
		public bool Completed { get; private set; }
		public int Total { get; set;}
    	public int Ingested { get; set;}
    	public int Failed { get; set;}
    	public int Skipped { get; set;}
    	public string[] Errors { get; set;}

		public IngestResult (bool Completed= true)
		{
			this.Completed = Completed;
		}
		
	}
}

