<?php

require_once('basetest.php');

class TestDeleteAccount extends PrecogBaseTest {

    function setupAccount(){
        
        $accountId = createAccount("fakeEmailAddress@precog.com");
         return $accountId;
    }
      
    function deleteAccountTest()
    {
        $account = $this->setupAccount();
        deleteAccount($account);
       
        $this->assertTrue($listAccounts($account) === null);
    }
}
?>
