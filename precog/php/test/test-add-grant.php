<?php

require_once('basetest.php');

class addGrantCaseTest extends PrecogBaseTest {

     function setupAccount(){
        $email = json_encode(array("email"=>"fakeEmailAddress@precog.com"));
        $accountId = $this->api->createAccount($email);
         return $accountId;
    }

    function testAddGrantCase()
    {
        $account = $this->setupAccount();
        $grantId = array("grant"=> "write");//??
        $grantAdded = $this->api->addGrant($account, $grantId);


        $this->assertTrue($describeAccount($account) == $grantAdded);
    }
}
?>
