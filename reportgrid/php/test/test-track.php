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
		$rollupPath = $this->path . "/rollup";
		$this->rg->track($rollupPath . "/child", array('rollup' => array('value' => 1)), array('rollup' => 'true')); // roll up the limit of the token, although in this cases true === 2
        sleep(30); // wait for stage propagation
        $this->assertEqual($this->rg->count($rollupPath, 'rollup'), $this->rg->count($rollupPath . "/child", 'rollup'));
	}
}