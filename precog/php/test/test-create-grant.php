<?php

require_once('basetest.php');

class CreateGrantTest extends PrecogBaseTest {
    function testCreateGrant() {
        $api = PrecogBaseTest::createApi($this->info);
        $result = $api->createGrant(array("type"=>"read", "path"=>$this->info["path"]."foo/", "ownerAccountId"=> $this->info["accountId"], "expirationDate"=> null));
 		$this->assertTrue(isset($result["grantId"]));
    }
}
?>