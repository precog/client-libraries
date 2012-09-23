<?php

require_once('basetest.php');

class TestRetrieveMetadata extends PrecogBaseTest {

     function setupPath() 
    {
        $path = "/unit_test/beta/test/php/query/TEST" . str_replace(".", "", uniqid(rand(), true));
        $this->rg->store($path, array('foo' => 42));
        return $path;
    }
      
    function retrieveMetadataTest()
    {
        $path = $this->setupPath();
        $childPath = $path."/childPath";
        $result = $this->retrieveMetadata($path, "children");

        $this->assertTrue($result  === json_encode(array("children"=>"childPath"))); //????;
    }
}
?>
