<?php

require_once('basetest.php');

class retrieveMetadataCaseTest extends PrecogBaseTest {

     function setupPath() 
    {
        $path = "/unit_test/beta/test/php/query/TEST" . str_replace(".", "", uniqid(rand(), true));
        $this->api->store($path, array('foo' => 42));
        var_dump($path);
        return $path;

    }
      
    function testRetrieveMetadataCase()
    {
        $path = $this->setupPath();
        $childPath = $path."/childPath";
        $this->api->store($childPath, array('foo'=> 43));

        sleep(5);
        $result = $this->api->retrieveMetadata($path, "children");
        var_dump($result);

        $this->assertTrue($result  === json_encode(array("children"=>"childPath"))); //????;
    }
}
?>
