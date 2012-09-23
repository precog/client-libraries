<?php

require_once('basetest.php');

class TestAddGrant extends PrecogBaseTest {

     function setupAccount(){
        $email = json_encode(array("email"=>"fakeEmailAddress@precog.com"));
        $accountId = createAccount($email);
         return $accountId;
    }

    function addGrantTest()
    {
        $account = $this->setupAccount();
        $grantId = array("grant"=> "write");//??
        $grantAdded = addGrant($account, $grantId);


        $this->assertTrue($describeAccount($account) == $grantAdded);
    }
}
?>
