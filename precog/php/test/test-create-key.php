<?php

require_once('basetest.php');

class CreateAPIkeyTest extends PrecogBaseTest {
        function testCreateAPIkey() {
            $api = PrecogBaseTest::createApi($this->info);
            $result = $api->createKey(array("grants"=>array(array("type"=>"read", "path"=>$this->info["path"]."foo/", "ownerAccountId"=> $this->info["accountId"], "expirationDate"=> null),array("type"=>"read", "path"=>$this->info["path"]."foo2/", "ownerAccountId"=> $this->info["accountId"], "expirationDate"=> null))));
            $this->assertTrue(isset($result) && strlen($result["apiKey"]) == 36);
        }
}
?>