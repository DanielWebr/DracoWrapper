<!DOCTYPE html>
<html>
	<head>
		<title>ChartJS - LineGraph</title>
		<link href="../WEB/css/black-dashboard.css?v=1.0.0" rel="stylesheet" />
	</head>
	<body class="">
		<?php
			include 'connection.php';
			$conn = OpenCon();
			$sql = sprintf("SELECT name FROM algorithm ORDER BY `algorithm`.`name` ASC");
			$result1 = $conn->query($sql);
			$sql = sprintf("SELECT name FROM metric ORDER BY `metric`.`name` ASC");
			$result2 = $conn->query($sql);
			$sql = sprintf("SELECT name FROM object ORDER BY `object`.`name` ASC");
			$result3 = $conn->query($sql);
	
			$algData = array();
			while($algData[] = $result1->fetch_object());
			array_pop($algData);
			$metData = array();
			while($metData[] = $result2->fetch_object());
			array_pop($metData);
			$objData = array();
			while($objData[] = $result3->fetch_object());
			array_pop($objData);
			
			$algF = $_POST['alg'];
			$met = $_POST['met'];
			$obj = $_POST['obj'];
		?>
		
		
		<div class="card card-chart">
            <div class="card-header ">
				<div class="row">
					<div class="col-sm-6 text-left">
						<h2 class="card-title">Distortion/Bitrate chart</h2>
					</div>
				</div>
				
			</div>
			<div class="card-body">
			
				<form action="" method=POST>
					<input type="submit" value="Find this combo">
					<label><input type="checkbox" id="myCheck" onclick="displayGraph()">Logarithmic y axis</label>
				<div class="form-row">
					<div class="form-group form-row">
					<select name="alg[]" size="3" multiple class="form-control">
						<?php foreach($algData as $option) : ?>
						<option value="<?php echo $option->name; ?>"<?php if(in_array($option->name,$algF)) {echo "selected=selected"; } ?>><?php echo $option->name; ?></option>
						<?php endforeach; ?>
					</select>		
					
					<select name="met" size="3" class="form-control">
						<?php foreach($metData as $option) : ?>
						<option value="<?php echo $option->name; ?>"<?php if($met == $option->name) {echo "selected=selected"; } ?> ><?php echo $option->name; ?></option>
						<?php endforeach; ?>
					</select>
		
					<select name="obj" size="3" class="form-control">
						<?php foreach($objData as $option) : ?>
						<option value="<?php echo $option->name; ?>"<?php if($obj == $option->name) {echo "selected=selected"; } ?>><?php echo $option->name; ?></option>
						<?php endforeach; ?>
					</select>	
					</div>					
				</form>
					<div class="chart-area" id="chart-container">
						<canvas id="mycanvas"></canvas>
					</div>
		</div>
		<?php 
			
			$resAlgF = array();
			
			?>
			<div align="right">
			<button id="save-btn">Save Chart Image</button>
			</div>
			<h5 class="card-category">Selected object: <?php echo $obj; ?> </br> Selected metric: <?php echo $met; ?></h5>
			<?php
			
			$querIDf = array();
			foreach($algF as $alg) {
				$sql = sprintf("SELECT id FROM combo WHERE metric_name = \"%s\" AND object_name = \"%s\" AND algorithm_name = \"%s\"", $met, $obj, $alg);
			
			$resID = $conn->query($sql);
			if ($resID->num_rows < 1) {
				?><h2 class="card-title">Combo not found in database.</h2><?php
			} else {
				array_push($resAlgF, $alg);
				$querID = $resID->fetch_assoc()["id"];
				$sql = sprintf("SELECT delta, distortion, rate FROM results WHERE combo_id = %d ORDER BY `results`.`delta` ASC", $querID);
				$resTable = $conn->query($sql);
				array_push($querIDf, $querID);
				$tblHead = array("delta", "distortion", "rate");
				
			?>
			
			</div>
		</div>
		
			
			<?php if ($resTable->num_rows > 0): ?>
			<div class="table">
				<table class="table tablesorter">
					<h2 class="card-title">Algorithm: <?php echo $alg ?></h2>
					<thead class="text-primary">
						<tr>
							<th><?php echo implode('</th><th>', $tblHead); ?></th>
						</tr>
					</thead>
					<tbody>
						<?php foreach ($resTable as $row): array_map('htmlentities', $row); ?>
						<tr>
							<td><?php echo implode('</td><td>', $row); ?></td>
						</tr>
						<?php endforeach; ?>
					</tbody>
				</table>
			</div>
			<?php endif; }}?>
		<?php	
			$result1->close();
			$result2->close();
			$result3->close();
			$conn->close();
		?>
		<!-- scripts -->
		<script type="text/javascript" src="js/jquery.min.js"></script>
		<script type="text/javascript" src="js/Chart.min.js"></script>
		<script type="text/javascript" src="js/FileSaver.min.js"></script>
		<script type="text/javascript" src="js/canvas2svg.js"></script>
		<script type="text/javascript" src="js/app.js"></script>
		<script type="text/javascript">
			createDataArray.call(<?php echo json_encode($querIDf); ?>,<?php echo json_encode($resAlgF); ?>);
		</script>
	</body>
</html>