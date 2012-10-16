<?php

require_once('basetest.php');

class DescribePlanTest extends PrecogBaseTest {
    function testDescribePlan()
    {
        $pg = PrecogBaseTest::ensureAccount($this->info);
        $result = PrecogAPI::describePlan($pg['user'], $pg['password'], $pg['accountId'], $pg['baseUrl'], $pg['version']);
        
        $this->assertTrue(isset($result['data']['type']));
    }
}
?>