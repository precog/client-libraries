<?php

require_once('basetest.php');

class deleteAccountCaseTest extends PrecogBaseTest {

     function setupAccount(){
        $email = json_encode(array("email"=>"fakeEmailAddress@precog.com"));
        $accountId = createAccount($email);
         return $accountId;
    }
      
    function testDeleteAccount()
    {
        $account = $this->setupAccount();
        deleteAccount($account);
       
        $this->assertTrue($listAccounts($account) === null);
    }
}
?>
