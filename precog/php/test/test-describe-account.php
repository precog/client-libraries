<?php

require_once('basetest.php');

class TestDescribeAccounts extends PrecogBaseTest {

     function setupAccount(){
        $email = json_encode(array("email"=>"fakeEmailAddress@precog.com"));
        $accountId = createAccount($email);
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
