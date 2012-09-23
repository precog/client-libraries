<?php

require_once('basetest.php');

class TestSkipCase extends PrecogBaseTest {
    function setupPath()
    {
        $path = "/unit_test/beta/test/php/query/TEST" . str_replace(".", "", uniqid(rand(), true));
        $this->rg->store($path, "first");
        $this->rg->store($path, "second");
        return $path;
    }

    function testSkip()
    {
        $path = $this->setupPath();
        $options1 = array("limit" => 1, "skip" => 0);
        $options2 = array("limit" => 1, "skip" => 1);

        sleep(5);

        $value1 = $this->rg->query("/$path", $options1);
        $value2 = $this->rg->query("/$path", $options2);


        $this->assertTrue($value1[0] != $value2[0]);
    }
}
?>