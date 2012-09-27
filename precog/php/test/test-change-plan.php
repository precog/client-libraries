<?php

require_once('basetest.php');

class changePlanCaseTest extends PrecogBaseTest {

    function setupAccount()
    {
        $email = json_encode(array("email"=>"fakeEmailAddress@precog.com"));
        $accountId = $this->api->createAccount($email);
         return $accountId;
    }

    function testChangePlanCase()
    {
        $account = $this->setupAccount();
        $plan = array("plan"=>"bronze");//??
        $changedPlan = $this->api->changePlan($account, $plan);

        $this->assertTrue($describeAccount($account) == $grantAdded);
    }
}
?>