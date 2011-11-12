<?php
/*
require_once('test-token.php');
require_once('test-track.php');
require_once('test-query.php');
*/

require_once('simpletest/autorun.php');

class AllTests extends TestSuite {
    function AllTests() {
        $this->TestSuite('All tests');
        $this->addFile('test-token.php');
        $this->addFile('test-track.php');
        $this->addFile('test-query.php');
    }
}