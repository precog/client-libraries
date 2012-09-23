<?php

require_once('basetest.php');

class TestListAccounts extends PrecogBaseTest {

    function setupAccount(){
        
        $accountId = createAccount("fakeEmailAddress@precog.com");
         return $accountId;
    }

      
    function listAccountsTest()
    {
        $account = $this->setupAccount();
        $list = listAccounts($account);

        $this->assertTrue($account === $list);
    }
}
?>
