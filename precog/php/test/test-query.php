<?php

require_once('basetest.php');

class QueryTest extends PrecogBaseTest {
    var $api;
    function setupPath()
    {
        $this->api = PrecogBaseTest::createApi($this->info);
        $path = $this->info['path']."test/php/query/T" . str_replace(".", "", uniqid(rand(), true));
        $r = $this->api->store($path, array('foo' => 42));
        return $path;
    }

    function testQuery()
    {
        $path = $this->setupPath();

        sleep(15);

        $value = $this->api->query("count(/$path)");

        $this->assertIsA($value, "Array");
        $this->assertTrue($value[0] == 1, "should be 1 but is ".$value[0]);
    }

}
?>
