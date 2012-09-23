<?php

require_once('basetest.php');

class TestChangePlan extends PrecogBaseTest {

    function setupAccount(){
        
        $accountId = createAccount("fakeEmailAddress@precog.com");
         return $accountId;
    }
      
    function changePlanTest()
    {
        $account = $this->setupAccount();
        $plan = array("plan"=>"bronze");//??
        $changedPlan = $this->changePlan($account, $plan);


        $this->assertTrue($describeAccount($account) == $grantAdded);
    }
}
?>
