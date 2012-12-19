<?php

require_once('basetest.php');

class AddGrantToKeyTest extends PrecogBaseTest {
    function testAddGrantToKey() {
        $api = PrecogBaseTest::createApi($this->info);

		$grant= array("name"=>"php-test","description"=>"",
    	"grants"=>array(array(
    			"parentIds"=> array(), 
    			"expirationDate"=> null,
    			"permissions"=>array(array("accessType"=>"read", "path"=>$this->info["path"]."foo/","ownerAccountId"=> $this->info["accountId"]))
    		))
    	);

        $result = $api->createKey($grant);
 		$apiKey1 = $result["apiKey"];
        $result = $api->createKey($grant);
 	    $apiKey2 = $result["apiKey"];
    	$result = $api->describeKey($apiKey2);

 		$grantId = $result["grants"][0]["grantId"];
 		$api->addGrantToKey($apiKey1, $grantId);
        $result = $api->describeKey($apiKey1);
        $this->assertEqual(2, count($result["grants"]));
    }
}
?>