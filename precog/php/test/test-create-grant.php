<?php

require_once('basetest.php');

class CreateGrantTest extends PrecogBaseTest {
    function testCreateGrant() {
        $api = PrecogBaseTest::createApi($this->info);

        $grant= array(
    			"parentIds"=> array(), 
    			"expirationDate"=> null,
    			"permissions"=>array(array("accessType"=>"read", "path"=>$this->info["path"]."foo/","ownerAccountId"=> $this->info["accountId"]))
    	);

        $result = $api->createGrant($grant);
 		$this->assertTrue(isset($result["grantId"]));
    }
}
?>