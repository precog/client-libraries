<?php

require_once('basetest.php');

class ListGrantChildrenTest extends PrecogBaseTest {
    function testListGrantChildren() {
        $api = PrecogBaseTest::createApi($this->info);
        $result = $api->createGrant(array("type"=>"read", "path"=>$this->info["path"]."foo/", "ownerAccountIds"=> array($this->info["accountId"]), "expirationDate"=> null));
 		$parentGrant = $result["grantId"];
 		$result = $api->createGrantChild($result["grantId"], array("type"=>"read", "path"=>$this->info["path"]."foo/", "ownerAccountIds"=> array($this->info["accountId"]), "expirationDate"=> null));
 		var_dump($result);
 		$result = $api->listGrantChildren($parentGrant);
 		var_dump($result);
 		$this->assertTrue(!empty($result) && count($result) === 1);
    }
}
?>