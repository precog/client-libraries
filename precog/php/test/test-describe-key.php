<?php

require_once('basetest.php');

class DescribeAPIkeyTest extends PrecogBaseTest {
        function testDescribeAPIkey() {
            $api = PrecogBaseTest::createApi($this->info);
            $result = $api->createKey(array("grants"=>array(array("type"=>"read", "path"=>$this->info["path"]."foo/", "ownerAccountId"=> $this->info["accountId"], "expirationDate"=> null))));
            $apiKey = $result["apiKey"];
     		$result = $api->describeKey($apiKey);
     		$this->assertEqual($apiKey, $result["apiKey"]);
     		$this->assertTrue(isset($result["grants"]));
     		$this->assertTrue(count($result["grants"]) > 0);
        }
}
?>