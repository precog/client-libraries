<?php

require_once('basetest.php');

class TestTracking extends BaseTest {
	var $path = "/test/php/track";
	function testTrack()
	{
		$this->assertTrue($this->rg->track($this->path, array('impression' => array( 'browser' => 'Chrome' ))));
	}
}