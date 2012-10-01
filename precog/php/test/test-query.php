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

        sleep(5);

        $value = $this->api->query("
            num := count(/$path)
            a := 4
            a + num");

        $this->assertIsA($value, "Array");
        $this->assertTrue($value[0] == 5, "should be 5 but is ".$value[0]);
    }

}
?>
