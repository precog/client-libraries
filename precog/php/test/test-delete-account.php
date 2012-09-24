<?php

require_once('basetest.php');

class DeleteAccountTest extends PrecogBaseTest {

<<<<<<< HEAD
    function setupAccount(){
        $email = json_encode(array("email"=>"fakeEmailAddress@precog.com"));
=======
     function setupAccount(){
        $email = array("email"=>"fakeEmailAddress@precog.com");
>>>>>>> updated some tests and added outlines of some security api tests
        $accountId = $this->api->createAccount($email);
        return $accountId;
    }

    function testDeleteAccount()
    {
        $account = $this->setupAccount();
        $this->api->deleteAccount($account);
<<<<<<< HEAD
        sleep(3);
        $this->assertTrue($this->api->$listAccounts($account) === null);
=======
       
        sleep(5);

        $this->assertTrue($this->api->$listAccounts($account["accountId"]) === null);
>>>>>>> updated some tests and added outlines of some security api tests
    }
}
?>
