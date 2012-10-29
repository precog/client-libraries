<?php

require_once('basetest.php');

class createAccountCaseTest extends PrecogBaseTest {
    function testCreateAccountCase()
    {
        $api = PrecogBaseTest::createApi($this->info);
        $randomemail = "testphp.".rand(0, 100000000)."@precog.com";
        $result = PrecogAPI::createAccount($randomemail, PrecogBaseTest::$password, $api->getBaseURL(), $api->getVersion());
        $this->assertTrue(isset($result['ok']));
        $this->assertTrue($result['data']['accountId'] !== null);
    }
}
