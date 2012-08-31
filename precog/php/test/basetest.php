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
	static $id = '2D36035A-62F6-465E-A64A-0E37BCC5257E';
	public static function createApi()
	{
          $HOST  = "beta2012v1.precog.com";
          $PORT  = 80;
          $PATH  = "/v1/";
          $TOKEN = PrecogBaseTest::$id;

	        $options = getopt("", array("host:", "port:", "path:", "token:"));

                foreach ($options as $option => $value) {
                  switch($option) {
                  case "host":
                    $HOST = $value;
                    break;
                  case "port":
                    $PORT = $value;
                    break;
                  case "path":
                    $PATH = $value;
                    break;
                  case "token":
                    $TOKEN = $value;
                    break;
                  }
                }
		
                 	 $URL = "http://$HOST:$PORT$PATH";
                  echo "Starting test against $URL\n";

		return new PrecogAPI($TOKEN, $URL);	
	}

	var $rg;
	function setUp()
	{
		$this->rg = PrecogBaseTest::createApi();
	}
}
