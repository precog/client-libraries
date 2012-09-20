<?php

require_once('basetest.php');

class TestPathDelete extends PrecogBaseTest {
    function setupPath() 
    {
        $path = "/unit_test/beta/test/php/query/TEST" . str_replace(".", "", uniqid(rand(), true));
        $this->rg->store($path, array('foo' => 42));
        return $path;
    }

    function testDeletePath()
    {
        $path = $this->setupPath();    

        sleep(10);

        $value = $this->rg->query("count(/$path)");
        $this->assertTrue($value[0] > 0);
        $result = $this->rg->delete($path);
        var_dump($this->rg->errorMessage);
        sleep(10);
        $this->assertTrue($value[0] === 0);
    }
}
?>