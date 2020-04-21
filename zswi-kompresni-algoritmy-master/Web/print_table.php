<?php
header('Content-type: application/json');
$sql = "SELECT id, delta, rate FROM results";
$result = $conn->query($sql);

if ($result->num_rows > 0) {
    // output data of each row
    while($row = $result->fetch_assoc()) {
        //echo "id: " . $row["id"]. " - delta: " . $row["delta"]. " - rate " . $row["rate"]. "<br>";
    }
} else {
    echo "0 results";
}
$tblhead = array("id", "delta", "rate");
$conn->close();
?>

<?php if ($result->num_rows > 0): ?>
<table>
  <thead>
    <tr>
      <th><?php echo($tblhead); ?></th>
    </tr>
  </thead>
  <tbody>
<?php foreach ($result as $row): array_map('htmlentities', $row); ?>
    <tr>
      <td><?php echo($row); ?></td>
    </tr>
<?php endforeach; ?>
  </tbody>
</table>
<?php endif; ?>