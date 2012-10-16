<?php

require_once('basetest.php');

class DeletePlanTest extends PrecogBaseTest {
    function testDeletePlan()
    {
        $pg = PrecogBaseTest::ensureAccount($this->info);
        $result = PrecogAPI::describePlan($pg['user'], $pg['password'], $pg['accountId'], $pg['baseUrl'], $pg['version']);
        $oldPlan = $result['data']['type'];
        $newPlan = "bronze";
        if($oldPlan == "Free") {
            $changePlan =PrecogAPI::changePlan($pg['user'], $pg['password'], $pg['accountId'],$newPlan, $pg['baseUrl'], $pg['version']);
        }
        PrecogAPI::deletePlan($pg['user'], $pg['password'], $pg['accountId'], $pg['baseUrl'], $pg['version']);
        $result = PrecogAPI::describePlan($pg['user'], $pg['password'], $pg['accountId'], $pg['baseUrl'], $pg['version']);
        $this->assertTrue($result['data']['type'] == "Free");
    }
}
?>