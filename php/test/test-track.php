<?php

require_once('basetest.php');

class TestTracking extends BaseTest {
	var $path = "/test/php/track";
	function testTrack()
	{
		$this->assertTrue($this->rg->track($this->path, array('impression' => array( 'browser' => 'Chrome' ))));
	}

	function testRollup()
	{
		$paths = array($this->path, dirname($this->path));
		$counts = array();
		foreach($paths as $path) {
			$counts[] = $this->rg->count($path, 'rollup');
		}

		$this->rg->track($this->path, array('rollup' => array('value' => 1)), array('rollup' => 1));
		sleep(15);
		for($i = 0; $i < count($path); $i++) {
			$this->assertTrue($counts[$i] < $this->rg->count($paths[$i], 'rollup'));
		}
	}
}