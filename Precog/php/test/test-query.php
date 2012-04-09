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

        $value = $this->rg->query("
            num := count(load(//unit_test/beta/test/php/query/TEST14471312814f8355686cdfc421311681)) 
            a := 4 
            a + num");
        echo "value is: " . $value[0] . "end";
        $this->assertIsA($value, "Array");
        $this->assertTrue($value[0] == 5);
    }
}
?>
