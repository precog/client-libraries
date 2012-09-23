<?php

require_once('basetest.php');

class listAccountsCaseTest extends PrecogBaseTest {

     function setupAccount(){
        $email = json_encode(array("email"=>"fakeEmailAddress@precog.com"));
        $accountId = createAccount($email);
         return $accountId;
    }

      
    function testListAccountsCase()
    {
        $account = $this->setupAccount();
        $list = listAccounts($account);

        $this->assertTrue($account === $list);
    }
}
?>
