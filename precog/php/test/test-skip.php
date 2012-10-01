<?php

require_once('basetest.php');

class SkipTest extends PrecogBaseTest {
    var $api;
    var $testPath;
    function setupPath()
    {
        $this->api = PrecogBaseTest::createApi($this->info);
        $path = $this->info['path']."test/php/query/T" . str_replace(".", "", uniqid(rand(), true));
        $r = $this->api->store($path, "a");
        $r = $this->api->store($path, "b");
        return $path;
    }

    function testSkipCase()
    {
        $path = $this->setupPath();
        $options1 = array("limit" => 1, "skip" => 0);
        $options2 = array("limit" => 1, "skip" => 1);

        sleep(5);

        $value1 = $this->api->query("/$path", $options1);
        $value2 = $this->api->query("/$path", $options2);

        $this->assertTrue($value1[0] != $value2[0]);
    }
}
?>