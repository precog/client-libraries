<?php

require_once('basetest.php');

class storingCaseTest extends PrecogBaseTest {
    var $path = "/unit_test/beta/test/php/store";
    function testStoreCase()
    {
    	$this->assertTrue($this->rg->store($this->path, array('impression' => array( 'browser' => 'Chrome' ))));
    }
}
?>
