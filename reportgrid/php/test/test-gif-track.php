<?php

require_once('basetest.php');

class TestGifTracking extends BaseTest {
	var $path  = "/test/php/giftrack";
	var $event = array("conversion" => array("browser" => "Chrome"));
	function testUrl()
	{
		$this->assertEqual(
			'http://api.reportgrid.com/services/viz/gif/transparent.gif?tokenId=A3BC1539-E8A9-4207-BB41-3036EC2C6E6D&path=%2Ftest%2Fphp%2Fgiftrack&event=%7B%22conversion%22%3A%7B%22browser%22%3A%22Chrome%22%7D%7D&service=http%3A%2F%2Fdevapi.reportgrid.com%3A80%2Fservices%2Fanalytics%2Fv1%2F&rollup=1',
			$this->rg->gifUrl($this->path, $this->event, array("rollup" => 1))
		);
	}

	function testTrack()
	{
		$count = $this->rg->count($this->path, 'conversion');
		$url = $this->rg->gifUrl($this->path, $this->event, array("rollup" => 1));
		var_dump($url);
		$this->event["conversion"]["#location"] = true;
		$url = $this->rg->gifUrl($this->path, $this->event, array("rollup" => 1));
		var_dump($url);
		file_get_contents($url);
		sleep(20);
		$count = $this->assertTrue($this->rg->count($this->path, 'conversion') > $count);
	}
}