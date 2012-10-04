<?php

require_once('basetest.php');

class ChangePasswordTest extends PrecogBaseTest {
    function testChangePassword()
    {
    	$email = "testphp-".str_replace(".", "", uniqid(rand(), true))."@precog.com";
    	$result = PrecogAPI::createAccount($email, "abc", $this->info["baseUrl"], $this->info["version"]);
    	$accountId = $result["data"]["accountId"];
    	$result = PrecogAPI::changePassword($email, "abc", "123", $accountId, $this->info["baseUrl"], $this->info["version"]);
    	$this->assertTrue($result["ok"]);
        $result = PrecogAPI::describeAccount($email, "123", $accountId, $this->info['baseUrl'], $this->info['version']);
        $this->assertTrue($result['ok']);
    }
}
?>
