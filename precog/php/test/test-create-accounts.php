<?php

require_once('basetest.php');

class createAccountCaseTest extends PrecogBaseTest {

     function setupAccount(){
        $email = json_encode(array("email"=>"fakeEmailAddress@precog.com"));
        $accountId = createAccount($email);
         return $accountId;
    }

      
    function testCreateAccountCase()
    {
        $account = $this->setupAccount();
        var_dump($account);
        sleep(5);

        $this->assertTrue($account != null);
    }
}
?>
