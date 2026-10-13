package edu.pioneer.ui;

import edu.pioneer.input.DeliveryFileReader;
import edu.pioneer.input.ProcessingReport;
import edu.pioneer.model.Delivery;
import edu.pioneer.model.Driver;
import edu.pioneer.scheduling.TaskAScheduler;
import edu.pioneer.scheduling.TaskBScheduler;
import edu.pioneer.scheduling.TaskCScheduler;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/** The single-page Operator Workbench for a validated delivery dataset. */
public final class MainWindow extends BorderPane {
    private final Stage stage;
    private final DeliveryFileReader fileReader = new DeliveryFileReader();
    private final TaskAScheduler taskAScheduler = new TaskAScheduler();
    private final TaskBScheduler taskBScheduler = new TaskBScheduler();
    private final TaskCScheduler taskCScheduler = new TaskCScheduler();
    private final Label fileName = label("No file loaded", "file-name");
    private final Label fileSummary = label("No dataset loaded", "file-summary");
    private final Label status = label("Choose a CSV file to see the analysis.", "status");
    private final VBox taskAResult = new VBox(10);
    private final VBox taskBResult = new VBox(10);
    private final VBox taskCResult = new VBox(10);
    private List<Delivery> acceptedDeliveries = List.of();

    public MainWindow(Stage stage) {
        this.stage = stage;
        getStyleClass().add("workbench");
        setLeft(createSidebar());
        setCenter(createMainContent());
        setStatus("Choose a CSV file to see the analysis.", "info");
        showUnavailable();
    }

    private VBox createSidebar() {
        Label appName = label("Pioneer\nDelivery", "app-name");
        Label currentFile = label("CURRENT FILE", "sidebar-eyebrow");
        fileName.setWrapText(true);
        fileSummary.setWrapText(true);
        VBox fileBlock = new VBox(9, currentFile, fileName, fileSummary);
        fileBlock.getStyleClass().add("file-block");
        VBox sidebar = new VBox(34, appName, fileBlock);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(265);
        return sidebar;
    }

    private ScrollPane createMainContent() {
        Label eyebrow = label("Scheduling Workbench", "eyebrow");
        Label title = label("Delivery Analysis", "page-title");
        VBox heading = new VBox(6, eyebrow, title);
        Button chooseFile = new Button("Choose CSV file…");
        chooseFile.getStyleClass().add("primary-button");
        chooseFile.setOnAction(event -> chooseFile());
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        HBox header = new HBox(20, heading, headerSpacer, chooseFile);
        header.setAlignment(Pos.TOP_LEFT);
        header.getStyleClass().add("page-header");

        status.setWrapText(true);
        status.setMaxWidth(Double.MAX_VALUE);
        taskAResult.getStyleClass().add("result");
        taskBResult.getStyleClass().add("result");
        taskCResult.getStyleClass().add("result");
        VBox taskASection = taskSection("Task A", "Maximum Deliveries for One Driver", taskAResult);
        VBox taskBSection = taskSection("Task B", "Minimum Drivers to Complete All Deliveries", taskBResult);
        VBox taskCSection = taskSection("Task C", "Deliveries Rejected for One Driver", taskCResult);
        VBox content = new VBox(20, header, status, taskASection, taskBSection, taskCSection);
        content.getStyleClass().add("main-content");
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("main-scroll");
        return scrollPane;
    }

    private VBox taskSection(String taskName, String title, VBox result) {
        Label taskLabel = label(taskName, "eyebrow");
        Label sectionTitle = label(title, "section-title");
        sectionTitle.setWrapText(true);
        VBox section = new VBox(5, taskLabel, sectionTitle, result);
        section.getStyleClass().add("task-section");
        return section;
    }

    private void chooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose a delivery CSV file");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv", "*.CSV"));
        File selected = chooser.showOpenDialog(stage);
        if (selected != null) {
            loadFile(selected.toPath());
        }
    }

    /** Loads a supplied path through the same CSV boundary as the file chooser. */
    public void loadFile(Path source) {
        try {
            ProcessingReport report = fileReader.read(source);
            acceptedDeliveries = report.getDeliveries();
            fileName.setText(source.getFileName().toString());
            fileSummary.setText(report.getTotalRows() + " rows · " + report.getAcceptedCount()
                    + " accepted · " + report.getSkippedCount() + " skipped");
            showProcessingStatus(report);
            showResults();
        } catch (IOException failure) {
            acceptedDeliveries = List.of();
            fileName.setText("No file loaded");
            fileSummary.setText("No dataset loaded");
            setStatus("Could not load " + source.getFileName() + ": " + failure.getMessage()
                    + ". No dataset is loaded; choose another CSV file.", "error");
            showUnavailable();
        }
    }

    private void showProcessingStatus(ProcessingReport report) {
        if (report.getSkippedCount() > 0) {
            setStatus("Results use " + report.getAcceptedCount() + " accepted records only. "
                    + report.getSkippedCount() + " row(s) skipped. Diagnostic log: "
                    + report.getDiagnosticLogPath(), "warning");
        } else if (report.getTotalRows() == 0) {
            setStatus("The CSV file is empty. All task results are zero.", "info");
        } else {
            setStatus("All " + report.getAcceptedCount() + " delivery requests were accepted.", "info");
        }
    }

    private void setStatus(String message, String style) {
        status.setText(message);
        status.getStyleClass().removeAll("info", "warning", "error");
        status.getStyleClass().add(style);
    }

    private void showUnavailable() {
        taskAResult.getChildren().setAll(emptyMessage("Load a CSV file to run this task."));
        taskBResult.getChildren().setAll(emptyMessage("Load a CSV file to run this task."));
        taskCResult.getChildren().setAll(emptyMessage("Load a CSV file to run this task."));
    }

    private void showResults() {
        List<Delivery> selected = taskAScheduler.selectDeliveries(acceptedDeliveries);
        List<Driver> drivers = taskBScheduler.assignDrivers(acceptedDeliveries);
        int rejected = taskCScheduler.countRejectedDeliveries(acceptedDeliveries);
        showTaskA(selected);
        showTaskB(drivers);
        showTaskC(rejected);
    }

    private void showTaskA(List<Delivery> selected) {
        taskAResult.getChildren().setAll(count(selected.size()), label("deliveries selected", "count-description"));
        if (selected.isEmpty()) {
            taskAResult.getChildren().add(emptyMessage("No deliveries to schedule."));
        } else {
            taskAResult.getChildren().add(deliveryTable(selected));
        }
    }

    private void showTaskB(List<Driver> drivers) {
        taskBResult.getChildren().setAll(count(drivers.size()), label("drivers required", "count-description"));
        if (drivers.isEmpty()) {
            taskBResult.getChildren().add(emptyMessage("No deliveries to assign."));
            return;
        }
        for (Driver driver : drivers) {
            Label driverName = label("Driver " + driver.getId(), "driver-name");
            VBox schedule = new VBox(5);
            for (Delivery delivery : driver.getDeliveries()) {
                Label scheduleEntry = label(delivery.getId() + "  (" + delivery.getStartTime() + "–" + delivery.getFinishTime() + ")", "driver-delivery");
                scheduleEntry.setWrapText(true);
                schedule.getChildren().add(scheduleEntry);
            }
            VBox driverBlock = new VBox(8, driverName, schedule);
            driverBlock.getStyleClass().add("driver-block");
            taskBResult.getChildren().add(driverBlock);
        }
    }

    private void showTaskC(int rejected) {
        String noun = rejected == 1 ? "delivery" : "deliveries";
        taskCResult.getChildren().setAll(count(rejected),
                label(noun + " to reject when only one driver is available.", "count-description"));
        if (rejected == 0) {
            taskCResult.getChildren().add(emptyMessage("No deliveries to reject."));
        }
    }

    private GridPane deliveryTable(List<Delivery> deliveries) {
        GridPane table = new GridPane();
        table.getStyleClass().add("delivery-table");
        double[] widths = {50, 25, 25};
        for (double width : widths) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(width);
            table.getColumnConstraints().add(column);
        }
        addCell(table, "ID", 0, 0, "table-heading");
        addCell(table, "START", 1, 0, "table-heading");
        addCell(table, "FINISH", 2, 0, "table-heading");
        for (int index = 0; index < deliveries.size(); index++) {
            Delivery delivery = deliveries.get(index);
            addCell(table, delivery.getId(), 0, index + 1, "table-cell");
            addCell(table, Integer.toString(delivery.getStartTime()), 1, index + 1, "table-cell");
            addCell(table, Integer.toString(delivery.getFinishTime()), 2, index + 1, "table-cell");
        }
        return table;
    }

    private void addCell(GridPane table, String text, int column, int row, String style) {
        Label cell = label(text, style);
        cell.setWrapText(true);
        cell.setMaxWidth(Double.MAX_VALUE);
        GridPane.setMargin(cell, new Insets(10, 8, 10, 8));
        table.add(cell, column, row);
    }

    private Label count(int value) {
        return label(Integer.toString(value), "count");
    }

    private Label emptyMessage(String message) {
        Label empty = label(message, "empty-message");
        empty.setMaxWidth(Double.MAX_VALUE);
        return empty;
    }

    private Label label(String text, String style) {
        Label result = new Label(text);
        result.getStyleClass().add(style);
        return result;
    }
}
