<?php

require_once('basetest.php');

class AddGrantTest extends PrecogBaseTest {

    function setupAccount(){
        $email = json_encode(array("email"=>"fakeEmailAddress@precog.com"));
        $accountId = $this->api->createAccount($email);
        return $accountId;
    }

    function testAddGrantCase()
    {
        $account = $this->setupAccount();
        $grantId = array("grant"=> "write");//??
        $grantAdded = $this->api->addGrantToAccount($account, $grantId);
        $this->assertTrue($this->api->describeAccount($account) === $grantAdded);
    }
}
?>
