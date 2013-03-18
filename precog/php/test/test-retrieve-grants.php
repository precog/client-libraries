<?php

require_once('basetest.php');

class RetrieveGrantsTest extends PrecogBaseTest {
    function testRetrieveGrants() {
        $api = PrecogBaseTest::createApi($this->info);

		$grant = array("name"=>"php-test","description"=>"",
	    	"grants"=>array(array(
	    			"parentIds"=> array(),
	    			"expirationDate"=> null,
	    			"permissions"=>array(array("accessType"=>"read", "path"=>$this->info["path"]."foo/","ownerAccountIds"=> array($this->info["accountId"]))
	    		))
	    	)
	    );

        $result = $api->createKey($grant);
 		$apiKey = $result["apiKey"];
 		$result = $api->retrieveGrants($apiKey);
 		$this->assertTrue(is_array($result));
 		$this->assertTrue(count($result)===1);
 		$this->assertTrue(isset($result[0]["grantId"]));
    }
}
?>