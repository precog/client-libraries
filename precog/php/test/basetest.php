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
	static $DEFAULT_API_KEY = 'CE1DE42A-D9EA-4494-8240-680230067C7C';
	static $email  = "testphp@precog.com";
	static $password  = "test123";
	public static function createApi()
	{
		$HOST  = "devapi.precog.com";
		$PORT  = null;
		$VERSION  = 1;
		$APIKEY = PrecogBaseTest::$DEFAULT_API_KEY;

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

		$URL = "http://$HOST" . ($PORT ? ":$PORT" : "");
		echo "Starting test against $URL\n";

		return new PrecogAPI($APIKEY, $URL, $VERSION);
	}

	public static function ensureAccount() {
		$api = PrecogBaseTest::createApi();
        $result = PrecogAPI::createAccount(PrecogBaseTest::$email, PrecogBaseTest::$password, $api->baseUrl, $api->version);
        return array(
        	'accountId' => $result['data']['accountId'],
        	'user'      => PrecogBaseTest::$email,
        	'password'  => PrecogBaseTest::$password,
        	'baseUrl'   => $api->baseUrl,
        	'version'   => $api->version
        );
	}

	var $api;
	function setUp()
	{
		$this->api = PrecogBaseTest::createApi();
	}
}
