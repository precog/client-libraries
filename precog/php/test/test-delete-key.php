<?php

require_once('basetest.php');

class DeleteAPIkeyTest extends PrecogBaseTest {
        function testDeleteAPIkey() {
            $api = PrecogBaseTest::createApi($this->info);
			$grant= array("name"=>"php-test","description"=>"",
		    	"grants"=>array(array(
		    			"parentIds"=> array(), 
		    			"expirationDate"=> null,
		    			"permissions"=>array(array("accessType"=>"read", "path"=>$this->info["path"]."foo/","ownerAccountId"=> $this->info["accountId"]))
		    		))
		    	);

            $result = $api->createKey($grant);
     		$apiKey = $result["apiKey"];
     		$this->assertTrue($api->deleteKey($apiKey));
     		$this->assertFalse($api->describeKey($apiKey));
        }
}
?>