<?php

require_once('basetest.php');

class DescribeGrantTest extends PrecogBaseTest {
    function testDescribeGrant() {
        $api = PrecogBaseTest::createApi($this->info);
        $result = $api->createGrant(array("type"=>"read", "path"=>$this->info["path"]."foo/", "ownerAccountIds"=> array($this->info["accountId"]), "expirationDate"=> null));
 		$grantId = $result["grantId"];
 		$result = $api->describeGrant($grantId);
 		$this->assertTrue($result["grantId"]===$grantId);
    }
}
?>