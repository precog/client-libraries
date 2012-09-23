<?php

require_once('basetest.php');

class TestDeletePlan extends PrecogBaseTest {

    function setupAccount(){
        
        $accountId = createAccount(fakeEmailAddress@precog.com);
         return $accountId;
    }
      
    function deletePlanTest()
    {
        $account = $this->setupAccount();
        deletePlan($account);
       
        $this->assertTrue($describeAccount($account) == array("plan")=>"starter");
    }
}
?>
