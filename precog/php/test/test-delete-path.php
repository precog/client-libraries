<?php

require_once('basetest.php');

class DeletePathTest extends PrecogBaseTest {
    var $api;
    function setupPath()
    {
        $this->api = PrecogBaseTest::createApi($this->info);
        $path = "/test/php/query/T" . str_replace(".", "", uniqid(rand(), true));
        $r = $this->api->store($path, array('foo' => 42));
        return $path;
    }

    function testDeletePathCase()
    {
        $path = $this->setupPath();

        sleep(10);

        $value = $this->api->query("count(/$path)");
    
        $this->assertTrue($value[0] > 0);
        $result = $this->api->delete($path);

        sleep(10);
        $value = $this->api->query("count(/$path)");
        $this->assertTrue($value[0] === 0);
    }
}
?>