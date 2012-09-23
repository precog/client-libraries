<?php

require_once('basetest.php');

class TestDeleteAccount extends PrecogBaseTest {

     function setupAccount(){
        $email = json_encode(array("email"=>"fakeEmailAddress@precog.com"));
        $accountId = createAccount($email);
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
