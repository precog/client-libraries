using System;
using Precog.Client;
using Precog.Client.Json;

namespace Precog.Client
{
	public class ServiceStack
	{
		public static PrecogClient CreatePrecogClient (Uri service, string tokenId)
		{
			return PrecogClient.Create(service, tokenId, new JsonServiceStack());
		}

		public static PrecogClient CreatePrecogClient (string tokenId)
		{
			return PrecogClient.Create(tokenId, new JsonServiceStack());
		}

		public static PrecogClient CreateSecurePrecogClient (string tokenId)
		{
			return PrecogClient.CreateSecure(tokenId, new JsonServiceStack());
		}
	}
}

