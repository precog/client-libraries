<?php

require_once('basetest.php');

class skipCaseTest extends PrecogBaseTest {
    function setupPath()
    {
        $path = "/unit_test/beta/test/php/query/TEST" . str_replace(".", "", uniqid(rand(), true));
        $this->api->store($path, "first");
        $this->api->store($path, "second");
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