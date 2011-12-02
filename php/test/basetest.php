<?php

require_once('../src/ReportGrid.php');
require_once('simpletest/autorun.php');


abstract class BaseTest extends UnitTestCase {
	static $id = 'A3BC1539-E8A9-4207-BB41-3036EC2C6E6D';
	public static function createApi()
	{
		return new ReportGridAPI(BaseTest::$id, "http://devapi.reportgrid.com/services/analytics/v1/");
	}

	var $rg;
	function setUp()
	{
		$this->rg = BaseTest::createApi();
	}
}
