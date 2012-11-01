using System;

namespace Precog.Client.Dto
{
	public class IngestResult
	{
		public bool Completed { get; private set; }
		public decimal Total { get; set;}
    	public decimal Ingested { get; set;}
    	public decimal Failed { get; set;}
    	public decimal Skipped { get; set;}
    	public string[] Errors { get; set;}

		public IngestResult(bool completed= true)
		{
			this.Completed = completed;
		}
		
	}
}

