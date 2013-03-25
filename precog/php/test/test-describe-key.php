<?php

require_once('basetest.php');

class DescribeAPIkeyTest extends PrecogBaseTest {
        function testDescribeAPIkey() {
            $api = PrecogBaseTest::createApi($this->info);

            $grant = array("name"=>"php-test","description"=>"",
		    	"grants" => array(
                    array(
		    			"parentIds"=> array(),
		    			"expirationDate"=> null,
		    			"permissions"=>array(array("accessType"=>"read", "path"=>$this->info["path"]."foo/","ownerAccountIds"=> array($this->info["accountId"])))
		    		)
                )
		    );

            $result = $api->createKey($grant);
            $apiKey = $result["apiKey"];
     		$result = $api->describeKey($apiKey);
     		$this->assertEqual($apiKey, $result["apiKey"]);
     		$this->assertTrue(isset($result["grants"]));
     		$this->assertTrue(count($result["grants"]) > 0);
        }
}
?>