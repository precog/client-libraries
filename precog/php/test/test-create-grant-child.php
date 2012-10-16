<?php

require_once('basetest.php');

class CreateGrantChildTest extends PrecogBaseTest {
    function testCreateGrantChild() {
        $api = PrecogBaseTest::createApi($this->info);
        $result = $api->createGrant(array("type"=>"read", "path"=>$this->info["path"]."foo/", "ownerAccountId"=> $this->info["accountId"], "expirationDate"=> null));
 		$result = $api->createGrantChild($result["grantId"], array("type"=>"read", "path"=>$this->info["path"]."foo/", "ownerAccountId"=> $this->info["accountId"], "expirationDate"=> null));
 		$this->assertTrue(isset($result["grantId"]));
    }
}
?>