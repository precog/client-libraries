<?php

require_once('basetest.php');

class DeleteAPIkeyTest extends PrecogBaseTest {
        function testDeleteAPIkey() {
            $api = PrecogBaseTest::createApi($this->info);
            $result = $api->createKey(array("grants"=>array(array("type"=>"read", "path"=>$this->info["path"]."foo/", "ownerAccountId"=> $this->info["accountId"], "expirationDate"=> null))));
     		$apiKey = $result["apiKey"];
     		$this->assertTrue($api->deleteKey($apiKey));
     		$this->assertFalse($api->describeKey($apiKey));
        }
}
?>