<?php

require_once('basetest.php');

class SortTest extends PrecogBaseTest {
    var $api;
    function setupPath()
    {
        $this->api = PrecogBaseTest::createApi($this->info);
        $path = "/test/php/query/T" . str_replace(".", "", uniqid(rand(), true));
        $r = $this->api->store($path, array('foo' => 2));
        $r = $this->api->store($path, array('foo' => 1));
        return $path;
    }

    function testSortCase()
    {
        $path = $this->setupPath();
        $options1 = array("limit" => 1, "sortOn" => "foo", "sortOrder" => "asc");
        $options2 = array("limit" => 1, "sortOn" => "foo", "sortOrder" => "desc");
        sleep(5);

        $value1 = $this->api->query("/$path", $options1);
        $value2 = $this->api->query("/$path", $options2);

        $this->assertTrue($value1[0]["foo"] === 1);
        $this->assertTrue($value2[0]["foo"] === 2);
    }
}
?>