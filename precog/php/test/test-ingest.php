<?php

require_once('basetest.php');

class IngestSyncTest extends PrecogBaseTest {
    function testIngestSync()
    {
        $api = PrecogBaseTest::createApi($this->info);
        $path = "/test/php/ingestsync/T" . str_replace(".", "", uniqid(rand(), true));
        $r = $api->ingest($path, file_get_contents("testData1.json"), "json");

        sleep(15);

        $result = $api->query("count(/$path)");
        $this->assertTrue($result[0] === 6);

    }
  //async no longer returns results but returns a job id, support for this functionality needs to be added  
 /*
    function testIngestAsync()
    {
        $api = PrecogBaseTest::createApi($this->info);
        $path = "/test/php/ingestasync/T" . str_replace(".", "", uniqid(rand(), true));
        $r = $api->ingest($path, file_get_contents("testData1.json"), "json", array("async"=> true));

        sleep(5);

        $result = $api->query("count(/$path)");
        $this->assertTrue($result[0] === 6);

    }
    */
}

?>

