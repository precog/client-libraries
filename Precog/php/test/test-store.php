<?php

require_once('basetest.php');

class TestStoring extends PrecogBaseTest {
	var $path = "/precog/beta/test/php/track/";
	function testStore()
	{
		$this->assertTrue($this->rg->store($this->path, array('impression' => array( 'browser' => 'Chrome' ))));
	}
}
?>
