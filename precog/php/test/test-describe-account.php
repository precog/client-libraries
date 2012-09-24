<?php

require_once('basetest.php');

class DescribeAccountsTest extends PrecogBaseTest {
    function testDescribeAccountsCase()
    {
        $account = $this->setupAccount();
        $description = $this->api->describeAccount($account["accountId"]);

        $this->assertTrue($description["accountId"] > 1);
        $this->assertTrue($description["email"] ==="fakeEmailAddress@precog.com");
        $this->assertTrue($description["accountCreationDate"] != null);
        $this->assertTrue($description["apiKey"] != null);
        $this->assertTrue($description["rootPath"] === "/".$description["accountId"]."/");
        $this->assertTrue($description["plan"]["type"] =! null);
    }
}
?>
