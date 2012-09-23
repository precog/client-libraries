<?php

require_once('../src/Precog.php');
require_once('simpletest/autorun.php');


function skipgeneration()
{
	if(PHP_SAPI === 'cli')
	{
		global $argc;
		global $argv;
		for($i = 0; $i < $argc; $i++) {
			if($argv[$i] == '-skipgeneration')
				return true;
		}
		return false;
	} else
		return isset($_GET['skipgeneration']) && $_GET['skipgeneration'];
}

abstract class PrecogBaseTest extends UnitTestCase {
	static $id = 'CE1DE42A-D9EA-4494-8240-680230067C7C';
	public static function createApi()
	{
		$HOST  = "devapi.precog.com";
		$PORT  = null;
		$VERSION  = 1;
		$APIKEY = PrecogBaseTest::$id;

        $options = getopt("", array("host:", "port:", "version:", "apikey:"));

		foreach ($options as $option => $value) {
			switch($option) {
				case "host":
					$HOST = $value;
					break;
				case "port":
					$PORT = $value;
					break;
				case "version":
					$VERSION = $value;
					break;
				case "apikey":
					$APIKEY = $value;
					break;
			}
		}

		 $URL = "https://$HOST" . ($PORT ? ":$PORT" : "");
		echo "Starting test against $URL\n";

		return new PrecogAPI($APIKEY, $URL, $VERSION);
	}

	var $api;
	function setUp()
	{
		$this->api = PrecogBaseTest::createApi();
	}
}
