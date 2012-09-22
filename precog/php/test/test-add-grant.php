<?php

require_once('basetest.php');

class TestAddGrant extends PrecogBaseTest {

    function setupAccount(){
        
        $accountId = createAccount(fakeEmailAddress@precog.com);
         return $accountId;
    }
      
    function addGrantTest()
    {
        $account = $this->setupAccount();
        $grantId = array("grant"=> "write");//??
        $grantAdded = addGrant($account, $grantId);


        $this->assertTrue($describeAccount($account) == $grantAdded);
    }
}
?>
