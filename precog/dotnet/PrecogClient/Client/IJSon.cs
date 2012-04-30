using System;

namespace Precog.Client
{
	public interface IJson
	{
		string Encode<T>(T record);
		T Decode<T>(string record);
	}
}

