<?php

require_once('basetest.php');

class TestSkip extends PrecogBaseTest {
    function setupPath() 
    {
        $path = "/unit_test/beta/test/php/query/TEST" . str_replace(".", "", uniqid(rand(), true));
        $this->rg->store($path, array('foo' => "first"));
        $this->rg->store($path, array('foo' => "second"));
        return $path;
    }

    function skipTest()
    {
        $path = $this->setupPath(); 
        $options1 = array("limit"=>1, "skip"=>false);
        $options2 = array("limit"=>1), "skip"=>true);

        sleep(10);

        $value1 = $this->rg->query("count(/$path)", $options1;
        $value2 = $this->rg->query("count(/$path)", $options2);


        $this->assertTrue($value1[0] != $value2[0]);
    }
}
?>