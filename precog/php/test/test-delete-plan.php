<?php

require_once('basetest.php');

class deletePlanCaseTest extends PrecogBaseTest {

     function setupAccount(){
        $email = json_encode(array("email"=>"fakeEmailAddress@precog.com"));
        $accountId = $this->api->createAccount($email);
         return $accountId;
    }
      
    function testDeletePlanCase()
    {
        $account = $this->api->setupAccount();
        $this->api->deletePlan($account);
       
        $this->assertTrue($this->api->$describeAccount($account) == array("plan")=>"starter");
    }
}
?>
