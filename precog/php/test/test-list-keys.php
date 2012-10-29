<?php

require_once('basetest.php');

class ListAPIkeyTest extends PrecogBaseTest {
        function testListAPIkey() {
            $api = PrecogBaseTest::createApi($this->info);
            $authorizingApiKey = $api->getApiKey();
            $result = $api->createKey(array("grants"=>array(array("type"=>"read", "path"=>$this->info["path"]."foo/", "ownerAccountId"=> $this->info["accountId"], "expirationDate"=> null))));
           // var_dump($result);
           // var_dump($result["apiKey"]);
            $apiKey = $result["apiKey"];
            $result = $api->listKeys();
            $this->assertTrue(count($result) > 0);
            $found = false;
            foreach ($result as $key => $value) {
                if($value["apiKey"] == $apiKey) {
                    $found = true;
                    break;
                }
            }
            $this->assertTrue($found, "sub apikey is in the result");
        }
}
?>