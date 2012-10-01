<?php

require_once('basetest.php');

class AddGrantToKeyTest extends PrecogBaseTest {
    function testAddGrantToKey() {
        $api = PrecogBaseTest::createApi($this->info);
        $result = $api->createKey(array("grants"=>array(array("type"=>"read", "path"=>$this->info["path"]."foo/", "ownerAccountId"=> $this->info["accountId"], "expirationDate"=> null))));
 		$apiKey1 = $result["apiKey"];
        $result = $api->createKey(array("grants"=>array(array("type"=>"write", "path"=>$this->info["path"]."foo/", "ownerAccountId"=> $this->info["accountId"], "expirationDate"=> null))));
 	    $apiKey2 = $result["apiKey"];
    	$result = $api->describeKey($apiKey2);
 		$grantId = $result["grants"][0]["grantId"];
        var_dump($grantId);
 		$this->assertTrue($api->addGrantToKey($apiKey1, $grantId));
        $result = $api->describeKey($apiKey1);
        $this->assertEqual(2, count($result["grants"]));
    }
}
?>