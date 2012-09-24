<?php

require_once('basetest.php');

class changePlanCaseTest extends PrecogBaseTest {

<<<<<<< HEAD
    function setupAccount()
    {
        $email = json_encode(array("email"=>"fakeEmailAddress@precog.com"));
=======
    function setupAccount(){
        $email = array("email"=>"fakeEmailAddress@precog.com");
>>>>>>> updated some tests and added outlines of some security api tests
        $accountId = $this->api->createAccount($email);
         return $accountId;
    }

    function testChangePlanCase()
    {
        $account = $this->setupAccount();
        $plan = array("plan"=>"bronze");//??
        $changedPlan = $this->api->changePlan($account, $plan);

<<<<<<< HEAD
        $this->assertTrue($describeAccount($account) == $grantAdded);
=======
        sleep(5);
        $this->assertTrue($this->api->$describeAccountPlan($account)["type"] === "bronze");
        
>>>>>>> updated some tests and added outlines of some security api tests
    }
}
?>
