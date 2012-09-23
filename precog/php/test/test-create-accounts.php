<?php

require_once('basetest.php');

class TestCreateAccount extends PrecogBaseTest {

     function setupAccount(){
        $email = json_encode(array("email"=>"fakeEmailAddress@precog.com"));
        $accountId = createAccount($email);
         return $accountId;
    }

      
    function createAccountsTest()
    {
        $account = $this->setupAccount();
        var_dump($account);
        sleep(5);

        $this->assertTrue($account != null);
    }
}
?>
