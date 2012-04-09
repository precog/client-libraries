<?php

require_once('basetest.php');

class TestQuery extends PrecogBaseTest {
        static function path() 
        {
            return "/unit_test/beta/test/php/query/TEST" . uniqid(rand(), true);
        }

        function testQueries()
	{
            $path = TestQuery::path();    
            echo $path;
            $this->rg->store($path, array('foo' => 42));
            $value = $this->rg->query("
                num := count(load(//precog/beta/test/php/query/TEST$path)) 
                a := 4 
                a + num");
	    $this->assertIsA($value, "Array");
            $this->assertTrue($value[0] > 0);
        }
}
?>
