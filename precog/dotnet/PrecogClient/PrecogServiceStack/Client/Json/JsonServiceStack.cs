using System;
using ServiceStack.Text;

namespace Precog.Client.Json
{
	public class JsonServiceStack : IJson
	{
		public JsonServiceStack ()
		{
		}

		public string Encode<T>(T record)
		{
			return record.ToJson();
		}
		
		public T Decode<T>(string value)
		{
			return value.FromJson<T>();
		}
	}
}

