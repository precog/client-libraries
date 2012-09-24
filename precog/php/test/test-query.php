<?php

require_once('basetest.php');

class QueryTest extends PrecogBaseTest {
    function setupPath()
    {
        $path = "/unit_test/beta/test/php/query/TEST" . str_replace(".", "", uniqid(rand(), true));
        $this->api->store($path, array('foo' => 42));
        return $path;
    }

    function testQueries()
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

    function testChildren()
    {
        $path = queryCaseTest::setupPath();

        sleep(5);

        $value = $this->api->listChildren("/unit_test/beta");

        $this->assertIsA($value, "Array");
        $this->assertTrue(in_array("/test/", $value));
    }


}
?>
