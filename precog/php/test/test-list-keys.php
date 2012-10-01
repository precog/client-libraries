<?php

require_once('basetest.php');

class listAPIkeysCaseTest extends PrecogBaseTest {
    
         function setupAPIkey(){
            $authorizingAPIkey = "A1C62105-691B-4D77-9372-36A693E5D905";
            $grants = array("grants"=>array(array("type"=>"write", "path"=>"/foo/", "ownerAccountId"=> 0000000024, "expirationDate"=>0),array("type"=>"read", "path"=>"/foo/", "ownerAccountId"=> 0000000024, "expirationDate"=>0)); //might need one more array to encase this array to support multiple grants.
                var_dump($grants);
                var_dump($grants["grants"]);
                var_dump($grants["grants"][0]["type"]);
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