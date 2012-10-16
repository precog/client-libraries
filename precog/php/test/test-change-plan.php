<?php

require_once('basetest.php');

class ChangePlanTest extends PrecogBaseTest {
    function testChangePlan()
    {
        $pg = PrecogBaseTest::ensureAccount($this->info);
        $result = PrecogAPI::describePlan($pg['user'], $pg['password'], $pg['accountId'], $pg['baseUrl'], $pg['version']);
        $oldPlan = $result['data']['type'];
        $newPlan = $oldPlan == "bronze"? "Free":"bronze";
        $changePlan =PrecogAPI::changePlan($pg['user'], $pg['password'], $pg['accountId'],$newPlan, $pg['baseUrl'], $pg['version']);
        $result = PrecogAPI::describePlan($pg['user'], $pg['password'], $pg['accountId'], $pg['baseUrl'], $pg['version']);
        $this->assertTrue($result['data']['type'] == $newPlan);
    }
}
?>