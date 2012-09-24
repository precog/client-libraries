<?php

require_once('basetest.php');

class createAPIkeyCaseTest extends PrecogBaseTest {
    $authorizingAPIkey = "CE1DE42A-D9EA-4494-8240-680230067C7C";
         function setupAPIkey(){
            
            $grants = array("grants"=>array("type"=>"read", "path"=>"/foo/", "ownerAccountId"=> NEED SAMPLE ID HERE, "expirationDate"=>0); //might need one more array to encase this array to support multiple grants.

            $apiKey = $this->api->createKey($authorizingAPIkey, $grants);
            return $apiKey;
        }

        function testCreateAPIkeyCase()
        {
            $key = $this->setupAPIkey();
            sleep(5);
          
            $this->assertTrue($key["apiKey"] >1]); 
        }
}
?>