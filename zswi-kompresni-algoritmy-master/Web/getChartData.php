<?php
$argument1 = $_GET['arg1'];
header('Content-type: application/json');
include 'connection.php';
$conn = OpenCon();
$sql = sprintf("SELECT distortion as y, rate as x FROM results WHERE combo_id = %d ORDER BY `results`.`rate` ASC", $argument1);
$result = $conn->query($sql);

$data = array();
foreach ($result as $row) {
	$data[] = $row;
}
$result->close();
$conn->close();
print json_encode($data);
?>

