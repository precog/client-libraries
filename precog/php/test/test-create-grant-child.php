<?php

require_once('basetest.php');

class CreateGrantChildTest extends PrecogBaseTest {
    function testCreateGrantChild() {

    	$api = PrecogBaseTest::createApi($this->info);

    	$grant= array(
    			"parentIds"=> array(), 
    			"expirationDate"=> null,
    			"permissions"=>array(array("accessType"=>"read", "path"=>$this->info["path"]."foo/","ownerAccountId"=> $this->info["accountId"]))
    	);
        
        $result = $api->createGrant($grant);

		$child_grant= array(
				"name"=>"childGrant1",
				"description" => "a child grant to test",
    			"parentIds"=> array($result["grantId"]), 
    			"expirationDate"=> null,
    			"permissions"=>array(array("accessType"=>"read", "path"=>$this->info["path"]."foo/","ownerAccountId"=> $this->info["accountId"]))
    	);
        
 		$result = $api->createGrantChild($result["grantId"], $child_grant);
 		$this->assertTrue(isset($result["grantId"]));
    }
}
?>