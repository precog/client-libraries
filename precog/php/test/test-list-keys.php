<?php

require_once('basetest.php');

class ListAPIkeyTest extends PrecogBaseTest {
        function testListAPIkey() {
            $api = PrecogBaseTest::createApi($this->info);
            $authorizingApiKey = $api->getApiKey();

            $grant= array("name"=>"php-test","description"=>"",
                "grants"=>array(array(
                        "parentIds"=> array(), 
                        "expirationDate"=> null,
                        "permissions"=>array(
                            array("accessType"=>"read", "path"=>$this->info["path"]."foo/","ownerAccountId"=> $this->info["accountId"])
                            )
                    ))
                );
            
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