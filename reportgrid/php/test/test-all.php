<?php

require_once('simpletest/autorun.php');

class AllTests extends TestSuite {
    function AllTests() {
        $this->TestSuite('All tests');
        # All of the following tests are v1 tests
#        $this->addFile('test-token.php');
#        $this->addFile('test-track.php');
#        $this->addFile('test-gif-track.php');
#        $this->addFile('test-query.php');
    }
}
