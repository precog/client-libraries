<?php

require_once('basetest.php');

class TestDescribeAccounts extends PrecogBaseTest {

    function setupAccount(){
        
        $accountId = createAccount(fakeEmailAddress@precog.com);
         return $accountId;
    }

      
    function describeAccountsTest()
    {
        $account = $this->setupAccount();
        $description = describeAccount($account);

        $this->assertTrue($description != null);
    }
}
?>
