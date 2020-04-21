<?php 

function OpenCon()
{
$dbhost = "students.kiv.zcu.cz";
$dbuser = "db1_vyuka";
$dbpass = "db1_vyuka";
$db = "db1_vyuka";

$conn = new mysqli($dbhost, $dbuser, $dbpass,$db) or die("Connect failed: %s\n". $conn -> error);
return $conn;
}
function CloseConn($conn)
{
$conn -> close();
}
?>