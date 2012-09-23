<?php

require_once('basetest.php');

class deleteAccountCaseTest extends PrecogBaseTest {

     function setupAccount(){
        $email = json_encode(array("email"=>"fakeEmailAddress@precog.com"));
        $accountId = $this->api->createAccount($email);
         return $accountId;
    }
      
    function testDeleteAccount()
    {
        $account = $this->setupAccount();
        $this->api->deleteAccount($account);
       
        $this->assertTrue($this->api->$listAccounts($account) === null);
    }
}
?>
