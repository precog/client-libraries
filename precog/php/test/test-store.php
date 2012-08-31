<?php

require_once('basetest.php');

class TestStoring extends PrecogBaseTest {
    var $path = "/unit_test/beta/test/php/store";
    function testStore()
    {
    	$this->assertTrue($this->rg->store($this->path, array('impression' => array( 'browser' => 'Chrome' ))));
    }
}
?>
