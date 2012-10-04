<?php

require_once('basetest.php');

class IngestSyncTest extends PrecogBaseTest {
    function testIngestSync()
    {
        $api = PrecogBaseTest::createApi($this->info);
        $path = $this->info['path']."test/php/ingestsync/T" . str_replace(".", "", uniqid(rand(), true));
        $r = $api->ingestSync($path, file_get_contents("testData1.json"), "json");
       
        sleep(5);
        
        $result = $api->query("count(/$path)");
        var_dump($result);
        $this->assertTrue($result[0] === 6);
      
    }

    function testIngestAsync()
    {
        $api = PrecogBaseTest::createApi($this->info);
        $path = $this->info['path']."test/php/ingestasync/T" . str_replace(".", "", uniqid(rand(), true));
        $r = $api->ingestAsync($path, file_get_contents("testData1.json"), "json");

        sleep(5);
        
        $result = $api->query("count(/$path)");
        var_dump($result);
        $this->assertTrue($result[0] === 6);
      
    }
}
?>

