<?php

require_once('basetest.php');

class TestQuery extends PrecogBaseTest {
        var $path = "/precog/beta/test/php/query/";

	function testQueries()
	{
                $this->rg->store($this->path, array('foo' => 42));
                $value = $this->rg->query("
                    num := count(load(//precog/beta/test/php/query)) 
                    a := 4 
                    a + num");
		$this->assertIsA($value, "Array");
                $this->assertTrue($value[0] > 0);
        }
}
?>
