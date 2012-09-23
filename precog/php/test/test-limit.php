<?php

require_once('basetest.php');

class limitCaseTest extends PrecogBaseTest {
    function setupPath()
    {
        $path = "/unit_test/beta/test/php/query/TEST" . str_replace(".", "", uniqid(rand(), true));
        $this->rg->store($path, array('foo' => 42));
        $this->rg->store($path, array('foo' => 42));
        return $path;
    }

    function testLimitCase()
    {
        $path = $this->setupPath();
        $options = array("limit"=>1);

        sleep(5);

        $value = $this->rg->query("/$path", $options);
        var_dump($value);
        $this->assertTrue(count($value), 1);
       // $result = $this->rg->limit($path);
      //  var_dump($this->rg->errorMessage);
      //  sleep(10);
      //  $this->assertTrue($value[0] === 0);
    }
}
?>