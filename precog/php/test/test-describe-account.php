<?php

require_once('basetest.php');

class describeAccountsCaseTest extends PrecogBaseTest {

     function setupAccount(){
        $email = json_encode(array("email"=>"fakeEmailAddress@precog.com"));
        $accountId = createAccount($email);
         return $accountId;
    }

      
    function testDescribeAccountsCase()
    {
        $account = $this->setupAccount();
        $description = describeAccount($account);

        $this->assertTrue($description != null);
    }
}
?>
