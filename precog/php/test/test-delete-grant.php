<?php

require_once('basetest.php');

class DeleteGrantTest extends PrecogBaseTest {
    function testDeleteGrant() {
        $api = PrecogBaseTest::createApi($this->info);
        $result = $api->createGrant(array("type"=>"read", "path"=>$this->info["path"]."foo/", "ownerAccountId"=> $this->info["accountId"], "expirationDate"=> null));
 		$grantId = $result["grantId"];
 		$result = $api->deleteGrant($grantId);
 		$this->assertTrue($result);
 		$result = $api->describeGrant($grantId);
 		$this->assertFalse($result);
    }
}
?>