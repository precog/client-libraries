<?php

require_once('basetest.php');

class CreateAPIkeyTest extends PrecogBaseTest {
        function testCreateAPIkey() {
            $api = PrecogBaseTest::createApi($this->info);

            $grant= array("name"=>"php-test","description"=>"",
		    	"grants"=>array(array(
		    			"parentIds"=> array(), 
		    			"expirationDate"=> null,
		    			"permissions"=>array(array("accessType"=>"read", "path"=>$this->info["path"]."foo/","ownerAccountId"=> $this->info["accountId"]))
		    		))
		    	);
print(json_encode($grant));
            $result = $api->createKey($grant);
            $this->assertTrue(isset($result) && strlen($result["apiKey"]) == 36);
        }
}
?>