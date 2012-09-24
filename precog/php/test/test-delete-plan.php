<?php

require_once('basetest.php');

class DeletePlanTest extends PrecogBaseTest {

    function setupAccount(){
        $email = json_encode(array("email"=>"fakeEmailAddress@precog.com"));
        $accountId = $this->api->createAccount($email);
        return $accountId;
    }

    function testDeletePlanCase()
    {
        $account = $this->api->setupAccount();
        $plan = array("plan"=>"bronze");//??
        $changedPlan = $this->api->changePlan($account, $plan);
        $this->api->deletePlan($account);
<<<<<<< HEAD
        sleep(3);
        $result = $this->api->$describeAccount($account);
        $this->assertTrue($result["plan"] === "starter");
=======

        sleep(5);
       
        $this->assertTrue($this->api->$describeAccount($account["type"]) === "starter");
>>>>>>> updated some tests and added outlines of some security api tests
    }
}
?>
