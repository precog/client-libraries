<?php

require_once('basetest.php');

class deletePathCaseTest extends PrecogBaseTest {
    function setupPath()
    {
        $path = "/unit_test/beta/test/php/query/TEST" . str_replace(".", "", uniqid(rand(), true));
        $this->api->store($path, array('foo' => 42));
        return $path;
    }

    function testDeletePathCase()
    {
        $path = $this->setupPath();

        sleep(5);

        $value = $this->api->query("count(/$path)");
        $this->assertTrue($value[0] > 0);
        $result = $this->api->delete($path);

        sleep(5);
        $value = $this->api->query("count(/$path)");
        $this->assertTrue($value[0] === 0);
    }
}
?>