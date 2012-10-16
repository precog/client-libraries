<?php

require_once('basetest.php');

class DescribeAccountsTest extends PrecogBaseTest {
    function testDescribeAccountsCase()
    {
        $pg = PrecogBaseTest::ensureAccount($this->info);
        $result = PrecogAPI::describeAccount($pg['user'], $pg['password'], $pg['accountId'], $pg['baseUrl'], $pg['version']);

        $this->assertTrue(isset($result['data']['apiKey']));
        $this->assertTrue(isset($result['data']['rootPath']));
    }
}
?>
