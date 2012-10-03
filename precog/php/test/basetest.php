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
	static $email  = "testphp@precog.com";
	static $password  = "test123";
	public static function serviceInfo()
	{
		$HOST  = "nebula.precog.com";
		$PORT  = null;
		$VERSION  = 1;

        $options = getopt("", array("host:", "port:", "version:"));

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
			}
		}

		$URL = "http://$HOST" . ($PORT ? ":$PORT" : "");
		return array("baseUrl"=>$URL, "version"=>$VERSION);
	}

	public static function ensureAccount($info) {
        $result = PrecogAPI::createAccount(PrecogBaseTest::$email, PrecogBaseTest::$password, $info["baseUrl"], $info["version"]);
        return array(
        	'accountId' => $result['data']['accountId'],
        	'user'      => PrecogBaseTest::$email,
        	'password'  => PrecogBaseTest::$password,
        	'baseUrl'   => $info["baseUrl"],
        	'version'   => $info["version"]
        );
	}

	public static function createApi(&$info) {
        $result = PrecogAPI::createAccount(PrecogBaseTest::$email, PrecogBaseTest::$password, $info["baseUrl"], $info["version"]);
        $info['accountId'] = $result["data"]["accountId"];
        $description = PrecogAPI::describeAccount(PrecogBaseTest::$email, PrecogBaseTest::$password, $result["data"]["accountId"], $info["baseUrl"], $info["version"]);
        $info['path'] = $description["data"]["rootPath"];
        return new PrecogAPI($description["data"]["apiKey"], $info["baseUrl"], $info["version"]);
	}
	var $info;
	var $path;
	function setUp()
	{
		$this->info = PrecogBaseTest::serviceInfo();
	}
}
