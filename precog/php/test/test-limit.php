<?php

require_once('basetest.php');

class LimitTest extends PrecogBaseTest {
    function setupPath()
    {
        $path = "/unit_test/beta/test/php/query/TEST" . str_replace(".", "", uniqid(rand(), true));
        $this->api->store($path, array('foo' => 42));
        $this->api->store($path, array('foo' => 42));
        return $path;
    }

    function testLimitCase()
    {
        $path = $this->setupPath();
        $options = array("limit"=>1);

        sleep(5);

        $value = $this->api->query("/$path", $options);
        var_dump($value);
        $this->assertTrue(count($value), 1);
       // $result = $this->api->limit($path);
      //  var_dump($this->api->errorMessage);
      //  sleep(10);
      //  $this->assertTrue($value[0] === 0);
    }
}
?>