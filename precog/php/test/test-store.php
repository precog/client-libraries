<?php

require_once('basetest.php');

class StoreTest extends PrecogBaseTest {
    function testStoreCase()
    {
    	$api = PrecogBaseTest::createApi($this->info);
    	$this->assertTrue($api->store("/test/php/store", array('impression' => array( 'browser' => 'Chrome' ))));
    }
}
?>
