<?php

require_once('basetest.php');

class TestCreateAccount extends PrecogBaseTest {

    function setupAccount(){
        
        $accountId = createAccount("fakeEmailAddress@precog.com");
         return $accountId;
    }

      
    function createAccountsTest()
    {
        $account = $this->setupAccount();

        $this->assertTrue($account != null);
    }
}
?>
