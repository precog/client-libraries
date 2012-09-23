<?php

require_once('basetest.php');

class describeAccountsCaseTest extends PrecogBaseTest {

     function setupAccount(){
        $email = json_encode(array("email"=>"fakeEmailAddress@precog.com"));
        $accountId = $this->api->createAccount($email);
         return $accountId;
    }

      
    function testDescribeAccountsCase()
    {
        $account = $this->setupAccount();
        $description = $this->api->describeAccount($account);

        $this->assertTrue($this->api->$description != null);
    }
}
?>
