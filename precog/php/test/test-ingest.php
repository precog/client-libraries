<?php

require_once('basetest.php');

class IngestSyncTest extends PrecogBaseTest {
    var $api;
    var $testPath;
    function setupPath()
    {
        $this->api = PrecogBaseTest::createApi($this->info);
        $path = $this->info['path']."test/php/ingestsync/T" . str_replace(".", "", uniqid(rand(), true));
        $contents = file_get_contents("testData1.json");
      //  var_dump($contents);
        $r = $this->api->ingestSync($path, $contents, "JSON");
      //  $r = $this->api->store($path, "b");
       // var_dump($r);
        return $path;
    }

    function testIngestSync()
    {
        $path = $this->setupPath();
        var_dump($path);

        sleep(5);
        
        $result = $this->api->query("count(/$path)");
        var_dump($result[0]);
        $this->assertTrue($result[0] > 4);
      
    }
}
?>

