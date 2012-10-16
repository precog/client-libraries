<?php

require_once('basetest.php');

class LimitTest extends PrecogBaseTest {
    var $api;
    function setupPath()
    {
        $this->api = PrecogBaseTest::createApi($this->info);
        $path = $this->info['path']."test/php/query/T" . str_replace(".", "", uniqid(rand(), true));
        $r = $this->api->store($path, array('foo' => 42));
        return $path;
    }

    function testLimitCase()
    {
        $path = $this->setupPath();
        $options = array("limit"=>1);

        sleep(5);

        $value = $this->api->query("/$path", $options);
        $this->assertTrue(count($value), 1);
    }
}
?>