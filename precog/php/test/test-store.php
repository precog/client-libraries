<?php

require_once('basetest.php');

class StoreTest extends PrecogBaseTest {
    var $path = "/unit_test/beta/test/php/store";
    function testStoreCase()
    {
    	$this->assertTrue($this->api->store($this->path, array('impression' => array( 'browser' => 'Chrome' ))));
    }
}
?>
