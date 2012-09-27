<?php

require_once('basetest.php');

class createAccountCaseTest extends PrecogBaseTest {
    function testCreateAccountCase()
    {
        $api = PrecogBaseTest::createApi();
        $result = PrecogAPI::createAccount(PrecogBaseTest::$email, PrecogBaseTest::$password, $api->baseUrl, $api->version);
//        var_dump($result);
        $this->assertTrue(isset($result['ok']));
        $this->assertTrue($result['data']['accountId'] !== null);
    }
}