<?php

require_once('basetest.php');

class RetrieveGrantsTest extends PrecogBaseTest {
    function testRetrieveGrants() {
        $api = PrecogBaseTest::createApi($this->info);
        $result = $api->createKey(array("grants"=>array(array("type"=>"read", "path"=>$this->info["path"]."foo/", "ownerAccountId"=> $this->info["accountId"], "expirationDate"=> null))));
 		$apiKey = $result["apiKey"];
 		$result = $api->retrieveGrants($apiKey);
 		$this->assertTrue(is_array($result));
 		$this->assertTrue(count($result)===1);
 		$this->assertTrue(isset($result[0]["grantId"]));
    }
}
?>