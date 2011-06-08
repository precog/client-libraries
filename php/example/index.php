<?php
include_once('../src/ReportGrid.php');

$report_grid = new ReportGridAPI('3D833B80-D09C-429B-9114-958DD4963230');

echo "<br/>--------newToken--------<br/>";
$new_token = $report_grid->newToken("ddimarco/transaction", "9223372036854775807", true, true, true, 2, 2, 2);
echo "<P>NEW TOKEN: $new_token";

echo "<br/>--------tokenInfo--------<br/>";
$token_info = $report_grid->tokenInfo($new_token);
echo "<P>TOKEN INFO: " . print_r($new_token, true);

/*-------BEGIN: THIS IS BUSTED!!!!!-------

echo "<br/>--------updateToken--------<br/>";
$token_info = $report_grid->updateToken($new_token, "ddimarco/transaction", "9223372030000000000", false, false, false, 1, 1, 1);
echo "<P>TOKEN INFO: " . print_r($new_token, true);

-------END: THIS IS BUSTED!!!!!-------*/

/*
echo "<br/>--------UPDATED tokenInfo--------<br/>";
$token_info = $report_grid->tokenInfo($new_token);
echo "<P>UPDATED TOKEN INFO: " . print_r($new_token, true);
*/

/*
echo "<br/>--------getTokens--------<br/>";
$tokens = $report_grid->getTokens();
if (count($tokens) > 0) {
echo "<P>TOKENS: " . print_r($tokens, true);
} else {
echo "<P>NO tokens fool!";
}
*/
/*
echo "<br/>--------deleteToken--------<br/>";
$result = $report_grid->deleteToken($new_token);
if ($result == "1") {
echo "<P>DELETED TOKEN";
} else {
echo "<P>FAILED DELETING TOKEN";
}
*/
/*
echo "<br/>--------recordEvent--------<br/>";
$events = array();
$interaction = array();
$event['type'] = "withdrawl";
$event['location'] = "15th & Arapahoe";
$event['amount'] = "100.00";
$events['interaction'] = $event;
$params['events'] = $events;
if ($report_grid->recordEvent("ddimarco/transaction", $params)) {
echo "<br/>Event recorded";
} else {
echo "<br/>Event record FAIL";
}
*/
/*
echo "<br/>--------retrieveEvent (count)--------<br/>";
$events = $report_grid->retrieveEvent("ddimarco/transaction", ".interaction.type", "withdrawl", "count");
echo "<br/>EVENT COUNT: " . $events[0];
*/
/*
echo "<br/>--------retrieveEvent (minute)--------<br/>";
$events = $report_grid->retrieveEvent("ddimarco/transaction", ".interaction.type", "withdrawl", "minute");
if (isset($events['minute'])) {
foreach($events['minute'] as $index=>$event_data) {
echo "<br/>minute-->" . $event_data[0] . " :: " . $event_data[1];
}
} else {
echo "<br/>NO events found (minute)";
}

echo "<br/>--------retrieveEvent (hour)--------<br/>";
$events = $report_grid->retrieveEvent("ddimarco/transaction", ".interaction.type", "withdrawl", "hour");
if (isset($events['hour'])) {
foreach($events['hour'] as $index=>$event_data) {
echo "<br/>hour-->" . $event_data[0] . " :: " . $event_data[1];
}
} else {
echo "<br/>NO events found (hour)";
}

echo "<br/>--------retrieveEvent (day)--------<br/>";
$events = $report_grid->retrieveEvent("ddimarco/transaction", ".interaction.type", "withdrawl", "day");
if (isset($events['day'])) {
foreach($events['day'] as $index=>$event_data) {
echo "<br/>day-->" . $event_data[0] . " :: " . $event_data[1];
}
} else {
echo "<br/>NO events found (day)";
}

echo "<br/>--------retrieveEvent (week)--------<br/>";
$events = $report_grid->retrieveEvent("ddimarco/transaction", ".interaction.type", "withdrawl", "week");
if (isset($events['week'])) {
foreach($events['week'] as $index=>$event_data) {
echo "<br/>week-->" . $event_data[0] . " :: " . $event_data[1];
}
} else {
echo "<br/>NO events found (week)";
}

echo "<br/>--------retrieveEvent (month)--------<br/>";
$events = $report_grid->retrieveEvent("ddimarco/transaction", ".interaction.type", "withdrawl", "month");
if (isset($events['month'])) {
foreach($events['month'] as $index=>$event_data) {
echo "<br/>month-->" . $event_data[0] . " :: " . $event_data[1];
}
} else {
echo "<br/>NO events found (month)";
}

echo "<br/>--------retrieveEvent (year)--------<br/>";
$events = $report_grid->retrieveEvent("ddimarco/transaction", ".interaction.type", "withdrawl", "year");
if (isset($events['year'])) {
foreach($events['year'] as $index=>$event_data) {
echo "<br/>year-->" . $event_data[0] . " :: " . $event_data[1];
}
} else {
echo "<br/>NO events found (year)";
}

echo "<br/>--------retrieveEvent (eternity)--------<br/>";
$events = $report_grid->retrieveEvent("ddimarco/transaction", ".interaction.type", "withdrawl", "eternity");
if (isset($events['eternity'])) {
foreach($events['eternity'] as $index=>$event_data) {
echo "<br/>eternity-->" . $event_data[0] . " :: " . $event_data[1];
}
} else {
echo "<br/>NO events found (eternity)";
}
*/

/*
echo "<br/>--------simple query--------<br/>";
$where = array();
$where['.interaction.type'] = "withdrawl";
$events = $report_grid->search("series/month", "ddimarco/transaction", $where);
echo "<br/>SIMPLE QUERY RESULTS: " . print_r($events, true);

echo "<br/>--------intersection query--------<br/>";
$where = array();
$where['.interaction.type'] = "withdrawl";
$where['.interaction.amount'] = "100.00";
$events = $report_grid->search("series/month", "ddimarco/transaction", $where);
echo "<br/>INTERSECTION QUERY RESULTS: " . print_r($events, true);
*/
/*
echo "<br/>--------deleteEvent(s)--------<br/>";
$events = array();
$interaction = array();
$event['type'] = "withdrawl";
$event['location'] = "15th & Arapahoe";
$event['amount'] = "100.00";
$events['interaction'] = $event;
$params['events'] = $events;
$params['count'] = -1;//delete!
if ($report_grid->recordEvent("ddimarco/transaction", $params)) {
echo "<br/>Event deleted";
} else {
echo "<br/>Event delete FAIL";
}
echo "<br/>--------retrieveEvent (count)--------<br/>";
$events = $report_grid->retrieveEvent("ddimarco/transaction", ".interaction.type", "withdrawl", "count");
echo "<br/>EVENT COUNT: " . $events[0];
*/
?>
