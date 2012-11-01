using System;

namespace Precog.Client.Dto
{
	public class IngestResult
	{
		public bool Completed { get; private set; }
		public float Total { get; set;}
    	public float Ingested { get; set;}
    	public float Failed { get; set;}
    	public float Skipped { get; set;}
    	public string[] Errors { get; set;}

		public IngestResult(bool completed= true)
		{
			this.Completed = completed;
		}
		
	}
}

