<?php

require_once('basetest.php');

class RetrieveMetadataTest extends PrecogBaseTest {
    var $api;
    function setupPath()
    {
        $this->api = PrecogBaseTest::createApi($this->info);
        $path = "test/php/metadata/T" . str_replace(".", "", uniqid(rand(), true));
        $r = $this->api->store($path, array('foo' => 42));
        return $path;
    }

    function testRetrieveMetadataCase()
    {
        $path = $this->setupPath();
        $childPath = $path."/childPath";
        $this->api->store($childPath, array('foo'=> 43));

        sleep(5);
        $result = $this->api->retrieveMetadata($path, "children");

        $this->assertTrue(in_array("/childPath/", $result["children"]));
    }

    function testChildren()
    {
        $path = $this->setupPath();
        $childPath = $path."/childPath";
        $this->api->store($childPath, array('foo'=> 43));

        sleep(5);

        $value = $this->api->listChildren($path);

        $this->assertIsA($value, "Array");
        $this->assertTrue(in_array("/childPath/", $value));
    }
}
?>
