<?php

require_once('basetest.php');

class ChangePlanTest extends PrecogBaseTest {
    function testChangePlan()
    {
        $pg = PrecogBaseTest::ensureAccount($this->info);
        $result = PrecogAPI::describePlan($pg['user'], $pg['password'], $pg['accountId'], $pg['baseUrl'], $pg['version']);
        $oldPlan = $result['data']['type'];
        var_dump($oldPlan);
        $newPlan = $oldPlan == "bronze"? "Free":"bronze";
        var_dump($newPlan);
        $changePlan =PrecogAPI::changePlan($pg['user'], $pg['password'], $pg['accountId'],$newPlan, $pg['baseUrl'], $pg['version']);
        var_dump($changePlan);
        $result = PrecogAPI::describePlan($pg['user'], $pg['password'], $pg['accountId'], $pg['baseUrl'], $pg['version']);
        var_dump($result);
        $this->assertTrue($result['data']['type'] == $newPlan);
    }
}
?>