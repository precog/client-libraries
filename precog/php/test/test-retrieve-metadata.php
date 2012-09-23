<?php

require_once('basetest.php');

class TestRetrieveMetadata extends PrecogBaseTest {

     function setupPath() 
    {
        $path = "/unit_test/beta/test/php/query/TEST" . str_replace(".", "", uniqid(rand(), true));
        $this->rg->store($path, array('foo' => 42));
        var_dump($path);
        return $path;

    }
      
    function retrieveMetadataTest()
    {
        $path = $this->setupPath();
        $childPath = $path."/childPath";
        $this->rg->store($childPath, array('foo'=> 43));

        sleep(5);
        $result = $this->retrieveMetadata($path, "children");
        var_dump($result);

        $this->assertTrue($result  === (array("children"=>"childPath"))); //????;
    }
}
?>
