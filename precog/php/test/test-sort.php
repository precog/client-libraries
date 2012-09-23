<?php

require_once('basetest.php');

class TestSortCase extends PrecogBaseTest {
    function setupPath()
    {
        $path = "/unit_test/beta/test/php/query/TEST" . str_replace(".", "", uniqid(rand(), true));
        $this->rg->store($path, array('foo' => 1));
        $this->rg->store($path, array('foo' => 2));
        return $path;
    }

    function testSort()
    {
        $path = $this->setupPath();
        $options1 = array("limit" => 1, "sortOn" => "foo", "sortOrder" => "asc");
        $options2 = array("limit" => 1, "sortOn" => "foo", "sortOrder" => "desc");
        sleep(5);

        $value1 = $this->rg->query("/$path", $options1);
        $value2 = $this->rg->query("/$path", $options2);

        $this->assertTrue($value1[0]["foo"] === 1);
        $this->assertTrue($value2[0]["foo"] === 2);
    }
}
?>