<?php

require_once('basetest.php');

class TestQuery extends PrecogBaseTest {
    function setupPath() 
    {
        $path = "/unit_test/beta/test/php/query/TEST" . str_replace(".", "", uniqid(rand(), true));
        $this->rg->store($path, array('foo' => 42));
        return $path;
    }

    function testQueries()
    {
        $path = TestQuery::setupPath();    

        sleep(10);

        $value = $this->rg->query("
            num := count(/$path) 
            a := 4 
            a + num");

        $this->assertIsA($value, "Array");
        $this->assertTrue($value[0] == 5);
    }

    function testChildren()
    {
        $path = TestQuery::setupPath();    

        sleep(10);

        $value = $this->rg->listChildren("/unit_test/beta");

        $this->assertIsA($value, "Array");
        $this->assertTrue(in_array("/test/", $value));
    }


}
?>
