<?php

require_once('basetest.php');

class listAPIkeysCaseTest extends PrecogBaseTest {
    $authorizingAPIkey = "CE1DE42A-D9EA-4494-8240-680230067C7C";
         function setupAPIkey(){
            
            $grants = array("grants"=>array("type"=>"read", "path"=>"/foo/", "ownerAccountId"=> NEED SAMPLE ID HERE, "expirationDate"=>0); //might need one more array to encase this array to support multiple grants.

            $apiKey = $this->api->createKey($authorizingAPIkey, $grants);
            return $apiKey;
        }

        function testListAPIkeysCase()
        {
            $key = $this->setupAPIkey();
            $result = $this->api->listKeys($authorizingAPIkey);
            sleep(5);
          
            $this->assertTrue($result["apiKey"] === $key["apiKey"]); //bad test, not certain that this will be the first api key returned. Maybe use foreach to iterate through
        }
}
?>