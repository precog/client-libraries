<?php

require_once('basetest.php');

class describeAccountPlanCaseTest extends PrecogBaseTest {

     function setupAccount(){
        $email = array("email"=>"fakeEmailAddress@precog.com");
        $accountId = $this->api->createAccount($email);
         return $accountId;
    }

      
    function testDescribeAccountPlanCase()
    {
        $account = $this->setupAccount();
        $description = $this->api->describeAccountPlan($account["accountId"]);

        $this->assertTrue($description["type"] != null);
   
    }
}
?>
