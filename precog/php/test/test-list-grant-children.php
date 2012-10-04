<?php

require_once('basetest.php');

class ListGrantChildrenTest extends PrecogBaseTest {
    function testListGrantChildren() {
        $api = PrecogBaseTest::createApi($this->info);
        $result = $api->createGrant(array("type"=>"read", "path"=>$this->info["path"]."foo/", "ownerAccountId"=> $this->info["accountId"], "expirationDate"=> null));
 		$parentGrant = $result["grantId"];
 		$result = $api->createGrantChild($result["grantId"], array("type"=>"read", "path"=>$this->info["path"]."foo/", "ownerAccountId"=> $this->info["accountId"], "expirationDate"=> null));	
 		$result = $api->listGrantChildren($parentGrant);
 		$this->assertTrue(count($result) ===1);
    }
}
?>