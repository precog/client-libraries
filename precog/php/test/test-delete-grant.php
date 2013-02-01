<?php

require_once('basetest.php');

class DeleteGrantTest extends PrecogBaseTest {
    function testDeleteGrant() {
        $api = PrecogBaseTest::createApi($this->info);

		$grant = array(
                    "parentIds"=> array(),
                    "expirationDate"=> null,
                    "permissions"=>array(
                        array("accessType"=>"read", "path"=>$this->info["path"]."foo/","ownerAccountIds" => array($this->info["accountId"]))
                  	)
                );

        $result = $api->createGrant($grant);
 		$grantId = $result["grantId"];
 		$result = $api->deleteGrant($grantId);
 		$this->assertTrue($result);
 		$result = $api->describeGrant($grantId);
 		$this->assertFalse($result);
    }
}
?>