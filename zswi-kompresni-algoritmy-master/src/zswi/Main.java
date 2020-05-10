/**
 * Created by Slavomír Verner
 */

package zswi;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Runtime.getRuntime;

public class Main extends Application
{
	private static final String timeFormat = "yyyy/MM/dd-HH:mm:ss";
	private static final Lock connectionLock = new ReentrantLock();
	
	/**
	 * GUI elements
	 */
	private static Pane pane;
	private static ListView<Object> categoryList;
	private static ComboBox<Category> categoryComboBox;
	private static Spinner<Integer> threadSpinner, timeoutSpinner, maxBitrateSpinner;
	private static Spinner<Double> bitrateStepSpinner;
	private static CheckBox checkBox;
	private static ProgressIndicator progress;
	
	/**
	 * Writer for logging information and errors
	 */
	private static BufferedWriter logWriter;
	
	/**
	 * Messenger of all alg/met/obj files and their combinations
	 */
	private static ComboMessenger comboMessenger;
	
	/**
	 * Database connection
	 */
	private static Connection connection;
	
	@FXML
	@Override
	/**
	 * Start of application, setting up GUI (most GUI elements are in GUI.fxml)
	 * and database connection.
	 */
	public void start(Stage primaryStage)
	{
		try
		{
			logWriter = new BufferedWriter(new FileWriter("log.txt", true));
		}
		catch(IOException e)
		{
			thisShowError("Error while accessing log file");
			System.exit(0);
		}
		
		comboMessenger = new ComboMessenger();
		
		String fxmlFile = "GUI.fxml";
		FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
		Parent root = null;
		
		try
		{
			root = loader.load();
		}
		catch(Exception e)
		{
			thisShowError("Error while accessing " + fxmlFile);
			System.exit(0);
		}
		
		pane = (Pane)loader.getNamespace().get("pane");
		
		CheckBox untested = (CheckBox)loader.getNamespace().get("untested");
		untested.setSelected(true);
		
		Button testAll = (Button)loader.getNamespace().get("testAll");
		testAll.setOnAction(e -> runTests(comboMessenger.testCombinations,
										  !untested.isSelected()));
		
		GridPane gridPane = (GridPane)loader.getNamespace().get("gridPane");
		threadSpinner = new Spinner<>(1, 64,
									  Math.min(Runtime.getRuntime().availableProcessors(), 64));
		gridPane.add(threadSpinner, 1, 2);
		
		timeoutSpinner = new Spinner<>(5, 600, 60);
		((SpinnerValueFactory.IntegerSpinnerValueFactory)
				timeoutSpinner.getValueFactory()).setAmountToStepBy(5);
		gridPane.add(timeoutSpinner, 1, 3);
		
		maxBitrateSpinner = new Spinner<>(1, 1000, 35);
		gridPane.add(maxBitrateSpinner, 1, 4);
		
		bitrateStepSpinner = new Spinner<>(0.01, 5.0, 0.3, 0.01);
		gridPane.add(bitrateStepSpinner, 1, 5);
		
		Button help = (Button)loader.getNamespace().get("help");
		help.setOnAction(e -> launchHelp());
		
		Button wrapper = (Button)loader.getNamespace().get("wrapper");
		wrapper.setOnAction(e -> launchWrapper());
		
		Button test = (Button)loader.getNamespace().get("test");
		test.setOnAction(e ->
		{
			Stage dialog = new Stage();
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.initOwner(primaryStage);
			
			VBox dialogVBox = new VBox(0);
			dialogVBox.setAlignment(Pos.CENTER);
			
			ListView combos = new ListView<>();
			combos.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			combos.getItems().addAll(comboMessenger.testCombinations);
			
			Button testButton = new Button("Test selected combinations");
			testButton.setOnAction(e2 ->
		   	{
				List<FilesMessenger> selected = combos.getSelectionModel().getSelectedItems();
				dialog.close();
		   		if(!selected.isEmpty())
					runTests(selected, true);
		   	});
			dialogVBox.getChildren().addAll(combos, testButton);
			Scene dialogScene = new Scene(dialogVBox,
										  pane.getPrefWidth() / 2,
										  pane.getPrefHeight());
			dialog.setScene(dialogScene);
			dialog.showAndWait();
		});
		
		Button algUpload = (Button)loader.getNamespace().get("alg");
		algUpload.setOnAction(e ->
		{
			upload(primaryStage, Category.Algorithm);
			update();
		});

		Button algWholeNumUpload = (Button)loader.getNamespace().get("algWholeNum");
		algWholeNumUpload.setOnAction(e ->
		{
			upload(primaryStage, Category.AlgorithmWholeNum);
			update();
		});


		Button metUpload = (Button)loader.getNamespace().get("met");
		metUpload.setOnAction(e ->
		{
			upload(primaryStage, Category.Metric);
			update();
		});
		
		Button objUpload = (Button)loader.getNamespace().get("obj");
		objUpload.setOnAction(e ->
		{
			upload(primaryStage, Category.Object);
			update();
		});
		
		categoryList = (ListView<Object>)loader.getNamespace().get("list");
		
		categoryComboBox = (ComboBox<Category>)loader.getNamespace().get("cat");
		categoryComboBox.getItems().addAll(Category.values());
		categoryComboBox.valueProperty().addListener(o -> update());
		
		checkBox = (CheckBox)loader.getNamespace().get("remDatabase");
		
		Button remove = (Button)loader.getNamespace().get("rem");
		remove.setOnAction(e -> removeApp());
		
		Button removeAll = (Button)loader.getNamespace().get("remAll");
		removeAll.setOnAction(e -> removeAllApps());
		
		progress = (ProgressIndicator)loader.getNamespace().get("progress");
		
		primaryStage.setOnCloseRequest(e ->
		{
			try
			{
				connection.close();
			}
			catch(SQLException ex)
			{
				thisShowError("Error while closing the database");
			}
			try
			{
				logWriter.close();
			}
			catch(IOException ex)
			{
				thisShowError("Error while accessing log file");
			}
		});
		
		Alert info = waitInfo("Connecting to database...");
		info.show();
		
		connection = Database.connect();
		if(connection == null)
		{
			thisShowError("Error while connecting to database");
			System.exit(0);
		}
		
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask()
		{
			@Override
			/**
			 *	Ping the database once a minute so the connection will not timeout
			 */
			public void run()
			{
				Database.selectID("combo", "true");
			}
		}, 60000,60000);
		
		update();
		
		info.close();
		
		primaryStage.setTitle("Algorithm testing");
		primaryStage.setScene(new Scene(root,
										pane.getPrefWidth() - 10,
										pane.getPrefHeight() - 10));
		primaryStage.setResizable(false);
		primaryStage.hide();
		primaryStage.show();
	}
	
	/**
	 * Opens help file in default pdf viewer.
	 */
	private static void launchHelp()
	{
		try
		{
			File help = new File("readme.pdf");
			Desktop.getDesktop().open(help);
		}
		catch (IOException e)
		{
			printToLog("Error while opening help", true);
		}
	}
	
	/**
	 * Launch of Wrapper application via included ".bat".
	 */
	private static void launchWrapper()
	{
		File dir = new File("Wrapper");
		File launch = null;
		for(File file : dir.listFiles())
			if(file.getName().endsWith(".bat"))
			{
				launch = file;
				break;
			}
		try
		{
			launchExe(launch.getAbsolutePath(), dir);
		}
		catch (Exception e)
		{
			printToLog("Error while opening wrapper", true);
		}
	}
	
	/**
	 * Removal of all alg/met/obj files and all records from the database (optional).
	 */
	private static void removeAllApps()
	{
		Alert info = waitInfo("Removing...");
		info.show();
		
		Object selectedCategory = categoryComboBox.getSelectionModel().selectedItemProperty().getValue();
		List<Object> items = categoryList.getItems();
		if(selectedCategory == null || items.isEmpty())
		{
			info.close();
			return;
		}
		
		if(selectedCategory.toString().endsWith(/*"(Database)"*/")"))
			if(selectedCategory.toString()/*.startsWith("Combination")*/.charAt(0) == 'C')
				for(Object item : items)
				{
					String[] strings = item.toString().split(" - ");
					String condition = "algorithm_name = '" + strings[0] +
							"' && metric_name = '" + strings[1] +
							"' && object_name = '" + strings[2] + "'";
					Database.tryDeleteRecords("results",
											  "combo_id = " + Database.selectID(
													  "combo", condition));
					Database.tryDeleteRecords("combo", condition);
				}
			else
				for(Object item : categoryList.getItems())
					removeAppFromDatabase(item.toString());
		else
			for(File element : getSelectedFileList())
			{
				removeFolder(element);
				if(checkBox.isSelected())
					removeAppFromDatabase(element.getName());
			}
		
		update();
		info.close();
	}
	
	/**
	 * Removal of selected alg/met/obj file or a record from the database.
	 */
	private static void removeApp()
	{
		Alert info = waitInfo("Removing...");
		
		Object selectedCategory = categoryComboBox.getSelectionModel().selectedItemProperty().getValue();
		if(categoryList.getSelectionModel().getSelectedItems().isEmpty() || selectedCategory == null)
			return;
		
		info.show();
		
		String selectedItem = categoryList.getSelectionModel().getSelectedItem().toString();
		if(selectedCategory.toString().endsWith(/*"(Database)"*/")"))
			if(selectedCategory.toString()/*.startsWith("Combination")*/.charAt(0) == 'C')
			{
				String[] strings = selectedItem.split(" - ");
				String condition = "algorithm_name = '" + strings[0] +
						"' && metric_name = '" + strings[1] +
						"' && object_name = '" + strings[2] + "'";
				Database.tryDeleteRecords("results",
										  "combo_id = " + Database.selectID(
										  		"combo", condition));
				Database.tryDeleteRecords("combo", condition);
			}
			else
				removeAppFromDatabase(selectedItem);
		else
			for(File element : getSelectedFileList())
				if(element.getName().equals(selectedItem))
				{
					removeFolder(element);
					if(checkBox.isSelected())
						removeAppFromDatabase(element.getName());
					break;
				}
		
		update();
		info.close();
	}
	
	/**
	 * Removal of a record of alg/met/obj file from database
	 * and sequential tied records from other tables.
	 *
	 * @param itemName name of removed app
	 */
	private static void removeAppFromDatabase(String itemName)
	{
		String appType = categoryComboBox.getValue().toString().split(" ")[0].toLowerCase();
		ArrayList<Integer> ids = Database.selectIDs("combo",
													appType + "_name = '" + itemName + "'");
		
		for(int id : ids)
		{
			Database.tryDeleteRecords("results", "combo_id = " + id);
			Database.tryDeleteRecords("combo", appType + "_name = '" + itemName + "'");
		}
		
		Database.tryDeleteRecords(appType, "name = '" + itemName + "'");
	}
	
	/**
	 * @return list of values of the currently selected category in database.
	 */
	private static List<String> getSelectedDatabaseList()
	{
		Category category = categoryComboBox.getValue();
		if(category == null)
			return new ArrayList<>();
		
		switch(category)
		{
			case dAlgorithm:
				return Database.selectAllRecords("algorithm", "name");
			case dMetric:
				return Database.selectAllRecords("metric", "name");
			case dObject:
				return Database.selectAllRecords("object", "name");
			case dCombination:
				return Database.selectAllRecords("combo",
										  "algorithm_name",
										  "metric_name",
										  "object_name");
			default:
				return new ArrayList<>();
		}
	}
	
	/**
	 * @return list of files of the currently selected category.
	 */
	private static List<File> getSelectedFileList()
	{
		Category category = categoryComboBox.getValue();
		if(category == null)
			return new ArrayList<>();
		
		switch(category)
		{
			case Algorithm:
				return comboMessenger.algorithms;
			case Object:
				return comboMessenger.objects;
			case Metric:
				return comboMessenger.metrics;
			default:
				return new ArrayList<>();
		}
	}
	
	/**
	 * Updates list of test combinations, progressIndicator
	 * and list of category that is being displayed in listView.
	 */
	private static void update()
	{
		comboMessenger.algorithms = Arrays.asList(comboMessenger.algDir.listFiles());
		comboMessenger.metrics = Arrays.asList(comboMessenger.metDir.listFiles());
		comboMessenger.objects = Arrays.asList(comboMessenger.objDir.listFiles());
		
		comboMessenger.algorithms.forEach(ComboMessenger::longestAlgName);
		comboMessenger.metrics.forEach(ComboMessenger::longestMetName);
		comboMessenger.objects.forEach(ComboMessenger::longestObjName);
		
		comboMessenger.testCombinations.clear();
		
		int doneAmount = 0;
		for(File algorithm : comboMessenger.algorithms)
			for(File metric : comboMessenger.metrics)
				for(File object : comboMessenger.objects)
				{
					FilesMessenger filesMessenger = new FilesMessenger(
							algorithm, metric, object,
							Database.recordExists("combo", "metric_name = '"
							+ metric.getName() + "' && object_name = '" + object.getName() +
							"' && algorithm_name = '" +	algorithm.getName() + "'"));
					if(Database.errorOccurred())
						return;
					if(filesMessenger.done)
						doneAmount++;
					comboMessenger.testCombinations.add(filesMessenger);
				}
		progress.setProgress(doneAmount / (double)comboMessenger.testCombinations.size());
		
		Object selected = categoryComboBox.getSelectionModel().selectedItemProperty().getValue();
		if(selected == null)
			return;
		Object[] items;
		
		if(selected.toString().endsWith(/*"(Database)"*/")"))
		{
			checkBox.setDisable(true);
			List<String> list = getSelectedDatabaseList();
			items = new String[list.size()];
			for(int i = 0; i < list.size(); i++)
				items[i] = list.get(i);
		}
		else
		{
			checkBox.setDisable(false);
			List<File> list = getSelectedFileList();
			items = new String[list.size()];
			for(int i = 0; i < list.size(); i++)
				items[i] = list.get(i).getName();
		}
		
		categoryList.getItems().clear();
		categoryList.getItems().addAll(items);
	}
	
	/**
	 * User chooses directory of alg/met/obj that they want to upload. The directory is then
	 * copied to the category folder and a record is inserted in the database.
	 * If the uploaded directory is a dynamic object then every frame is uploaded as one object
	 * and its name ends with "(frame X)".
	 *
	 * @param stage main stage of application
	 * @param category category that is being uploaded to
	 */
	private static void upload(Stage stage, Category category)
	{
		DirectoryChooser directoryChooser = new DirectoryChooser();
		File selectedDirectory = directoryChooser.showDialog(stage);
		if(selectedDirectory == null)
			return;
		
		Alert alert = waitInfo("Copying...");
		alert.show();
		
		File categoryDir = null;
		File[] binFiles = null;
		boolean dynObj = false;
		switch(category)
		{
			case AlgorithmWholeNum:
			case Algorithm:
				categoryDir = comboMessenger.algDir;
				break;
			case Metric:
				categoryDir = comboMessenger.metDir;
				break;
			case Object:
				categoryDir = comboMessenger.objDir;
				binFiles = selectedDirectory.listFiles(
						(dir, name) -> name.endsWith(".obj"));
				dynObj = binFiles.length > 1; //is dynamic object?
				break;
		}
		String table = "";
		if(category == Category.AlgorithmWholeNum ){
			table = Category.Algorithm.toString().toLowerCase();
		}else{
			table = category.toString().toLowerCase();
		}


		if(dynObj)
		{
			String[] names = new String[binFiles.length];
			for(int i = 0; i < binFiles.length; i++)
			{
				names[i] = selectedDirectory.getName() + " (frame " + (i + 1) + ")";
				String objFolder = categoryDir.getAbsolutePath() + "\\" + names[i];
				createFolder(Paths.get(objFolder));
				copyFile(binFiles[i].toPath(),
						 Paths.get(objFolder + "\\" + names[i] + ".obj"));
			}
			alert.setContentText("Writing to database...");
			for(int i = 0; i < binFiles.length; i++)
				insertCategory(table, names[i]);
		}
		else
		{
			String name = selectedDirectory.getName();
			copyFolder(selectedDirectory.toPath(),
					   Paths.get(categoryDir.getAbsolutePath() + "\\" + name));
			if(category==Category.AlgorithmWholeNum){
				setAlgorithmWholeNumbers(categoryDir.getAbsolutePath() + "\\" + name);
			}
			alert.setContentText("Writing to database...");
			insertCategory(table, name);
		}
		alert.close();
	}

	/**
	 * Set testing of alghorithm with whole numbers
	 *
	 * @param algorithmDir directory with algorithm
	 */
	private static void setAlgorithmWholeNumbers(String algorithmDir) {
		File wholeNumberLabel = new File(algorithmDir+"\\WholeNumbersDelta");
		try {
			wholeNumberLabel.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Inserts a record of a given category if it does not already exist in database.
	 *
	 * @param table category table
	 * @param name name of alg/met/obj
	 */
	private static void insertCategory(String table, String name)
	{
		if(!Database.recordExists(table, "name = '" + name + "'"))
			Database.insertRecord(table, "name, time", "'" + name + "', '" +
					LocalDateTime.now().format(DateTimeFormatter.ofPattern(timeFormat))
					+ "'");
	}
	
	/**
	 * Creates alert with title "Please wait" and is hidden on close.
	 *
	 * @param contentText content text of created alert
	 * @return created alert
	 */
	private static Alert waitInfo(String contentText)
	{
		Alert info = new Alert(Alert.AlertType.NONE);
		info.initModality(Modality.APPLICATION_MODAL);
		info.setTitle("Please wait");
		info.setContentText(contentText);
		info.setHeaderText("");
		hideOnClose(info);
		return info;
	}
	
	/**
	 * @return number of concurrent threads to exist, selected by user via threadSpinner.
	 */
	static synchronized int getNumOfThreads()
	{
		return threadSpinner.getValueFactory().getValue();
	}
	
	/**
	 * @return max number of tests to run selected by user via maxBitrateSpinner.
	 */
	static synchronized int getMaxBitrate()
	{
		return maxBitrateSpinner.getValueFactory().getValue();
	}
	
	/**
	 * @return size of steps selected by user via bitrateStepSpinner.
	 */
	static synchronized double getBitrateStep()
	{
		return bitrateStepSpinner.getValueFactory().getValue();
	}
	
	/**
	 * @return seconds until a test times out selected by user via timeoutSpinner.
	 */
	static synchronized int getTimeout()
	{
		return timeoutSpinner.getValueFactory().getValue();
	}
	
	/**
	 * Waits for database connection to be unlocked
	 * and then locks it and returns it. Connection should be unlocked
	 * after it is not used anymore.
	 *
	 * @return connection to database
	 */
	static Connection getAndLockConnection()
	{
		synchronized(connectionLock)
		{
			connectionLock.lock();
			return connection;
		}
	}
	
	/**
	 * Unlocks database connection.
	 */
	static void unlockConnection()
	{
		connectionLock.unlock();
	}
	
	/**
	 * Creates list of combinations to test and an instance of BossThread
	 * that splits the work between WorkerThreads. WorkerThreads can be stopped
	 * by user by clicking on "Stop" or "Force stop". "Stop" disables creating
	 * new WorkerThreads (and waits for active WorkerThreads to finish).
	 * "Force stop" kills all running processes and stops active WorkerThreads.
	 *
	 * @param testCombinations combinations to be tested
	 * @param testDone test already tested combinations
	 */
	private static void runTests(List<FilesMessenger> testCombinations, boolean testDone)
	{
		ArrayList<FilesMessenger> combosToTest = new ArrayList<>();
		for(FilesMessenger combo : testCombinations)
			if(!combo.done || testDone)
				combosToTest.add(combo);
			
		if(combosToTest.isEmpty())
			return;
		
		createFolder(ComboMessenger.tempDir.toPath());
		
		BossThread bossThread = new BossThread(combosToTest);
		
		ButtonType stopButton = new ButtonType("Stop", ButtonBar.ButtonData.OK_DONE);
		
		Alert algorithmInfo;
		if(combosToTest.size() == 1)
		{
			algorithmInfo = new Alert(Alert.AlertType.INFORMATION,
									  "Testing algorithm...",
									  stopButton);
		}
		else
		{
			algorithmInfo = new Alert(Alert.AlertType.INFORMATION,
											"Testing algorithms: 0/" + combosToTest.size(),
											stopButton);
			bossThread.getRemaining().addListener(o -> Platform.runLater(() -> algorithmInfo
					.setContentText("Testing algorithms: " + (combosToTest.size()
					- bossThread.getRemaining().getValue()) + "/" + combosToTest.size())));
		}
		
		algorithmInfo.initModality(Modality.APPLICATION_MODAL);
		algorithmInfo.setTitle("Please wait");
		algorithmInfo.setHeaderText("");
		
		ButtonType forceStopButton = new ButtonType("Force stop",
													ButtonBar.ButtonData.OK_DONE);
		Alert stoppingInfo = new Alert(Alert.AlertType.WARNING,
									   "Waiting for threads to finish...",
									   forceStopButton);
		
		stoppingInfo.initModality(Modality.APPLICATION_MODAL);
		stoppingInfo.setTitle("Please wait");
		stoppingInfo.setHeaderText("");
		
		algorithmInfo.setOnCloseRequest(e ->
		{
			algorithmInfo.close();
			stoppingInfo.show();
			bossThread.stopWork();
		});
		
		stoppingInfo.setOnCloseRequest(e -> bossThread.forceStopWork());
		
		bossThread.isDone().addListener(o -> Platform.runLater(() ->
		{
			removeFolder(ComboMessenger.tempDir);
			update();
			if(algorithmInfo.isShowing())
				algorithmInfo.close();
			if(stoppingInfo.isShowing())
				stoppingInfo.close();
		}));
		
		algorithmInfo.show();
		bossThread.start();
	}
	
	/**
	 * Prints error to the log and if popupError is true it also shows the error alert to user.
	 *
	 * @param errorString description of error
	 * @param popupError whether show error alert to user or not
	 */
	static synchronized void printToLog(String errorString, boolean popupError)
	{
		try
		{
			logWriter.write(
					"[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern(timeFormat)) +
							"]" + " - " + errorString + "\n");
			logWriter.flush();
		}
		catch(IOException e)
		{
			showError("Error while accessing log file");
			return;
		}
		if(popupError)
			showError(errorString);
	}
	
	/**
	 * Shows error to user and waits for confirmation.
	 *
	 * @param errorString description of error
	 */
	private static void thisShowError(String errorString)
	{
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText("");
		alert.setContentText(errorString);
		alert.showAndWait();
	}
	
	/**
	 * Calls a javafx thread to show an error.
	 *
	 * @param errorString description of error
	 */
	private static void showError(String errorString)
	{
		Platform.runLater(() -> thisShowError(errorString));
	}
	
	/**
	 * Hides the dialog that is to be closed
	 *
	 * @param dialog dialog that will be hidden on close
	 */
	private static void hideOnClose(Dialog<ButtonType> dialog)
	{
		dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
		Node closeButton = dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
		closeButton.managedProperty().bind(closeButton.visibleProperty());
		closeButton.setVisible(false);
	}
	
	/**
	 * Creates a copy of the file
	 *
	 * @param filePath path to file that is being copied
	 * @param savePath path to copy of the file (directory and name of file)
	 */
	static void copyFile(Path filePath, Path savePath)
	{
		try
		{
			if(!Files.exists(savePath))
				Files.createDirectories(savePath);
			Files.copy(filePath, savePath, StandardCopyOption.REPLACE_EXISTING);
		}
		catch(IOException e)
		{
			Main.printToLog("Error when copying file " + filePath.toFile().getName(), true);
		}
	}
	
	/**
	 * Creates a new folder
	 *
	 * @param path path to created folder
	 */
	private static void createFolder(Path path)
	{
		try
		{
			Files.createDirectories(path);
		}
		catch(IOException e)
		{
			printToLog("Error when creating folder", true);
		}
	}
	
	/**
	 * Tries to delete folder and on fail waits for 100ms and tries again (max 10 tries).
	 *
	 * @param folder folder to delete
	 */
	private static void removeFolder(File folder)
	{
		removeFolder(folder, 0);
	}
	
	/**
	 * Tries to delete folder and on fail waits for 100ms and tries again (max 10 tries).
	 *
	 * @param folder folder to delete
	 * @param tries number of remaining attempts
	 */
	private static void removeFolder(File folder, int tries)
	{
		try
		{
			if(folder.exists())
				FileUtils.deleteDirectory(folder);
		}
		catch(IOException e)
		{
			if(tries < 10)
			{
				try
				{
					Thread.sleep(100);
				}
				catch(InterruptedException ex)
				{
					e.printStackTrace();
				}
				removeFolder(folder, ++tries);
			}
			else
				Main.printToLog("Error while removing " + folder.getName() + " folder",
								true);
		}
	}
	
	/**
	 * Copies folder including its contents
	 *
	 * @param folderPath path to folder that is being copied
	 * @param savePath path to copy of the folder
	 */
	static void copyFolder(Path folderPath, Path savePath)
	{
		File save = savePath.toFile();
		try
		{
			Files.createDirectories(savePath);
			if(save.exists())
				FileUtils.deleteDirectory(save);
			FileUtils.copyDirectory(folderPath.toFile(), savePath.toFile());
		}
		catch(IOException e)
		{
			Main.printToLog("Error when coping folders", true);
		}
	}
	
	/**
	 * Launches executable and returns an instance of its process
	 *
	 * @param command path to executable with all commands
	 * @param dir directory in witch is executable launched
	 * @return process created by executable
	 */
	static Process launchExe(String command, File dir)
	{
		Process process = null;
		try
		{
			process = getRuntime().exec(command, null, dir);
		}
		catch(IOException e)
		{
			printToLog("Error while launching executable", true);
		}
		return process;
	}
}
