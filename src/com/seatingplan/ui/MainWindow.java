package com.seatingplan.ui;

import com.seatingplan.excel.ExcelReader;
import com.seatingplan.model.*;
import com.seatingplan.model.SeatingConfig.*;
import com.seatingplan.pdf.PdfGenerator;
import com.seatingplan.service.SeatingAlgorithm;
import com.seatingplan.service.SeatingAlgorithmFactory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Main window of the Seating Plan Generator application.
 * Enhanced with comprehensive customization options.
 */
public class MainWindow {

    private Stage stage;
    private TabPane tabPane;

    // Data
    private List<Classroom> classrooms = new ArrayList<>();
    private List<Student> students = new ArrayList<>();
    private SeatingPlan currentPlan = null;
    private SeatingConfig seatingConfig = new SeatingConfig();
    
    // Available branches (populated from loaded students)
    private Set<String> availableBranches = new LinkedHashSet<>();

    // UI Components - Classroom Tab
    private Spinner<Integer> numRoomsSpinner;
    private VBox roomConfigBox;
    private Label totalCapacityLabel;

    // UI Components - Students Tab
    private TableView<Student> studentTable;
    private Label studentCountLabel;
    private Label branchSummaryLabel;

    // UI Components - Pattern Tab
    private ToggleGroup patternGroup;
    private TextField examNameField;
    private DatePicker examDatePicker;
    
    // Pattern customization components
    private ComboBox<FillDirection> fillDirectionCombo;
    private ComboBox<StartCorner> startCornerCombo;
    private ComboBox<SeatGapping> seatGappingCombo;
    private ComboBox<StudentSortOrder> sortOrderCombo;
    private CheckBox enforceBranchCheck;
    private CheckBox enforceYearCheck;

    // UI Components - Generate Tab
    private ScrollPane previewScrollPane;
    private VBox previewContainer;
    private Label statusLabel;

    public MainWindow(Stage stage) {
        this.stage = stage;
        initializeUI();
    }

    private void initializeUI() {
        stage.setTitle("Seating Plan Generator");
        stage.setWidth(1000);
        stage.setHeight(700);

        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab classroomTab = createClassroomTab();
        Tab studentsTab = createStudentsTab();
        Tab patternTab = createPatternTab();
        Tab generateTab = createGenerateTab();

        tabPane.getTabs().addAll(classroomTab, studentsTab, patternTab, generateTab);

        Scene scene = new Scene(tabPane);
        stage.setScene(scene);
    }

    // ==================== CLASSROOM TAB ====================

    private Tab createClassroomTab() {
        Tab tab = new Tab("1. Classrooms");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Header
        Label header = new Label("Configure Classrooms");
        header.setFont(Font.font("System", FontWeight.BOLD, 18));

        // Number of rooms
        HBox numRoomsBox = new HBox(10);
        numRoomsBox.setAlignment(Pos.CENTER_LEFT);
        Label numRoomsLabel = new Label("Number of Classrooms:");
        numRoomsSpinner = new Spinner<>(1, 20, 3);
        numRoomsSpinner.setEditable(true);
        numRoomsSpinner.setPrefWidth(80);
        Button applyRoomsBtn = new Button("Apply");
        applyRoomsBtn.setOnAction(e -> generateRoomFields());
        numRoomsBox.getChildren().addAll(numRoomsLabel, numRoomsSpinner, applyRoomsBtn);

        // Room configuration container
        roomConfigBox = new VBox(10);
        roomConfigBox.setPadding(new Insets(10));
        roomConfigBox.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5;");

        ScrollPane roomScroll = new ScrollPane(roomConfigBox);
        roomScroll.setFitToWidth(true);
        roomScroll.setPrefHeight(350);

        // Total capacity
        totalCapacityLabel = new Label("Total Capacity: 0 seats");
        totalCapacityLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        // Save button
        Button saveRoomsBtn = new Button("Save Classroom Configuration");
        saveRoomsBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        saveRoomsBtn.setOnAction(e -> saveClassroomConfig());

        content.getChildren().addAll(header, numRoomsBox, roomScroll, totalCapacityLabel, saveRoomsBtn);

        tab.setContent(content);

        // Initialize with default rooms
        generateRoomFields();

        return tab;
    }

    private void generateRoomFields() {
        roomConfigBox.getChildren().clear();
        int numRooms = numRoomsSpinner.getValue();

        for (int i = 0; i < numRooms; i++) {
            VBox roomContainer = new VBox(5);
            roomContainer.setPadding(new Insets(8));
            roomContainer.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 5;");
            
            // Room basic info row
            HBox roomRow = new HBox(15);
            roomRow.setAlignment(Pos.CENTER_LEFT);

            Label roomLabel = new Label("Room " + (i + 1) + ":");
            roomLabel.setPrefWidth(70);
            roomLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

            TextField nameField = new TextField("Room-" + (char)('A' + i));
            nameField.setPrefWidth(100);
            nameField.setPromptText("Name");

            Label rowsLabel = new Label("Rows:");
            Spinner<Integer> rowsSpinner = new Spinner<>(1, 20, 5);
            rowsSpinner.setEditable(true);
            rowsSpinner.setPrefWidth(70);
            rowsSpinner.setId("rows_" + i);

            Label colsLabel = new Label("Columns:");
            Spinner<Integer> colsSpinner = new Spinner<>(1, 20, 6);
            colsSpinner.setEditable(true);
            colsSpinner.setPrefWidth(70);
            colsSpinner.setId("cols_" + i);

            Label capacityLabel = new Label("= 30 seats");
            capacityLabel.setPrefWidth(80);

            // Update capacity on change
            rowsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                int capacity = newVal * colsSpinner.getValue();
                capacityLabel.setText("= " + capacity + " seats");
                updateTotalCapacity();
            });
            colsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                int capacity = rowsSpinner.getValue() * newVal;
                capacityLabel.setText("= " + capacity + " seats");
                updateTotalCapacity();
            });

            roomRow.getChildren().addAll(roomLabel, nameField, rowsLabel, rowsSpinner, 
                                         colsLabel, colsSpinner, capacityLabel);
            
            // Branch assignment row
            HBox branchRow = new HBox(10);
            branchRow.setAlignment(Pos.CENTER_LEFT);
            branchRow.setPadding(new Insets(5, 0, 0, 70));
            
            Label branchLabel = new Label("Allowed Branches:");
            branchLabel.setStyle("-fx-text-fill: #666;");
            
            // Multi-select for branches
            MenuButton branchMenuBtn = new MenuButton("Select Branches");
            branchMenuBtn.setId("branches_" + i);
            branchMenuBtn.setPrefWidth(200);
            
            // Add "All Branches" option
            CheckMenuItem allBranchesItem = new CheckMenuItem("All Branches (No Restriction)");
            allBranchesItem.setSelected(true);
            branchMenuBtn.getItems().add(allBranchesItem);
            branchMenuBtn.getItems().add(new SeparatorMenuItem());
            
            // Will be populated when students are loaded
            Label branchHint = new Label("(Load students first to select specific branches)");
            branchHint.setStyle("-fx-text-fill: #999; -fx-font-size: 10;");
            branchHint.setId("branchHint_" + i);
            
            branchRow.getChildren().addAll(branchLabel, branchMenuBtn, branchHint);
            
            // Store references for later
            roomContainer.setUserData(new Object[]{nameField, rowsSpinner, colsSpinner, branchMenuBtn, allBranchesItem});
            roomContainer.getChildren().addAll(roomRow, branchRow);

            roomConfigBox.getChildren().add(roomContainer);
        }

        updateTotalCapacity();
        updateBranchMenus();
    }
    
    /**
     * Update branch selection menus when students are loaded.
     */
    private void updateBranchMenus() {
        for (var node : roomConfigBox.getChildren()) {
            if (node instanceof VBox) {
                Object[] data = (Object[]) node.getUserData();
                if (data != null && data.length >= 5) {
                    MenuButton branchMenuBtn = (MenuButton) data[3];
                    CheckMenuItem allBranchesItem = (CheckMenuItem) data[4];
                    
                    // Keep existing items (All Branches and separator)
                    while (branchMenuBtn.getItems().size() > 2) {
                        branchMenuBtn.getItems().remove(2);
                    }
                    
                    // Add available branches
                    for (String branch : availableBranches) {
                        CheckMenuItem branchItem = new CheckMenuItem(branch);
                        branchItem.setOnAction(e -> {
                            // Uncheck "All Branches" when specific branches are selected
                            if (branchItem.isSelected()) {
                                allBranchesItem.setSelected(false);
                            }
                            updateBranchButtonText(branchMenuBtn, allBranchesItem);
                        });
                        branchMenuBtn.getItems().add(branchItem);
                    }
                    
                    // Handle "All Branches" selection
                    allBranchesItem.setOnAction(e -> {
                        if (allBranchesItem.isSelected()) {
                            // Uncheck all specific branches
                            for (MenuItem item : branchMenuBtn.getItems()) {
                                if (item instanceof CheckMenuItem && item != allBranchesItem) {
                                    ((CheckMenuItem) item).setSelected(false);
                                }
                            }
                        }
                        updateBranchButtonText(branchMenuBtn, allBranchesItem);
                    });
                    
                    // Update hint visibility
                    VBox container = (VBox) node;
                    HBox branchRow = (HBox) container.getChildren().get(1);
                    for (var child : branchRow.getChildren()) {
                        if (child instanceof Label && ((Label) child).getId() != null && 
                            ((Label) child).getId().startsWith("branchHint_")) {
                            child.setVisible(availableBranches.isEmpty());
                            child.setManaged(availableBranches.isEmpty());
                        }
                    }
                }
            }
        }
    }
    
    private void updateBranchButtonText(MenuButton btn, CheckMenuItem allItem) {
        if (allItem.isSelected()) {
            btn.setText("All Branches");
            return;
        }
        
        List<String> selected = new ArrayList<>();
        for (MenuItem item : btn.getItems()) {
            if (item instanceof CheckMenuItem && item != allItem && ((CheckMenuItem) item).isSelected()) {
                selected.add(item.getText());
            }
        }
        
        if (selected.isEmpty()) {
            allItem.setSelected(true);
            btn.setText("All Branches");
        } else if (selected.size() <= 2) {
            btn.setText(String.join(", ", selected));
        } else {
            btn.setText(selected.size() + " branches");
        }
    }

    private void updateTotalCapacity() {
        int total = 0;
        for (var node : roomConfigBox.getChildren()) {
            if (node instanceof VBox) {
                Object[] data = (Object[]) node.getUserData();
                if (data != null && data.length >= 3) {
                    @SuppressWarnings("unchecked")
                    Spinner<Integer> rows = (Spinner<Integer>) data[1];
                    @SuppressWarnings("unchecked")
                    Spinner<Integer> cols = (Spinner<Integer>) data[2];
                    total += rows.getValue() * cols.getValue();
                }
            }
        }
        totalCapacityLabel.setText("Total Capacity: " + total + " seats");
    }

    private void saveClassroomConfig() {
        classrooms.clear();
        for (var node : roomConfigBox.getChildren()) {
            if (node instanceof VBox) {
                Object[] data = (Object[]) node.getUserData();
                if (data != null && data.length >= 5) {
                    TextField nameField = (TextField) data[0];
                    @SuppressWarnings("unchecked")
                    Spinner<Integer> rows = (Spinner<Integer>) data[1];
                    @SuppressWarnings("unchecked")
                    Spinner<Integer> cols = (Spinner<Integer>) data[2];
                    MenuButton branchMenuBtn = (MenuButton) data[3];
                    CheckMenuItem allBranchesItem = (CheckMenuItem) data[4];

                    String name = nameField.getText().trim();
                    if (name.isEmpty()) name = "Room-" + (classrooms.size() + 1);

                    Classroom classroom = new Classroom(name, rows.getValue(), cols.getValue());
                    
                    // Set branch assignments
                    if (!allBranchesItem.isSelected()) {
                        List<String> assignedBranches = new ArrayList<>();
                        for (MenuItem item : branchMenuBtn.getItems()) {
                            if (item instanceof CheckMenuItem && item != allBranchesItem && 
                                ((CheckMenuItem) item).isSelected()) {
                                assignedBranches.add(item.getText());
                            }
                        }
                        classroom.setAssignedBranches(assignedBranches);
                    }
                    
                    classrooms.add(classroom);
                }
            }
        }

        // Build summary with branch info
        StringBuilder summary = new StringBuilder();
        summary.append("Saved ").append(classrooms.size()).append(" classrooms\n\n");
        for (Classroom c : classrooms) {
            summary.append(c.getRoomName()).append(": ")
                   .append(c.getCapacity()).append(" seats");
            if (c.hasBranchRestrictions()) {
                summary.append(" [").append(c.getBranchSummary()).append("]");
            }
            summary.append("\n");
        }
        summary.append("\nTotal capacity: ")
               .append(classrooms.stream().mapToInt(Classroom::getCapacity).sum())
               .append(" seats");

        showAlert(Alert.AlertType.INFORMATION, "Classrooms Saved", summary.toString());
        tabPane.getSelectionModel().select(1); // Move to Students tab
    }

    // ==================== STUDENTS TAB ====================

    private Tab createStudentsTab() {
        Tab tab = new Tab("2. Students");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Header
        Label header = new Label("Import Students from Excel");
        header.setFont(Font.font("System", FontWeight.BOLD, 18));

        // File selection
        HBox fileBox = new HBox(10);
        fileBox.setAlignment(Pos.CENTER_LEFT);
        Label fileLabel = new Label("Excel File:");
        TextField filePathField = new TextField();
        filePathField.setEditable(false);
        filePathField.setPrefWidth(400);
        Button browseBtn = new Button("Browse...");
        Button loadBtn = new Button("Load Students");
        loadBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        browseBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Student Excel File");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls")
            );
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                filePathField.setText(file.getAbsolutePath());
            }
        });

        loadBtn.setOnAction(e -> loadStudentsFromExcel(filePathField.getText()));

        fileBox.getChildren().addAll(fileLabel, filePathField, browseBtn, loadBtn);

        // Expected format info
        Label formatInfo = new Label("Expected columns: Name, Roll No, Year, Semester, Branch");
        formatInfo.setStyle("-fx-text-fill: #666;");

        // Student table
        studentTable = new TableView<>();
        studentTable.setPrefHeight(350);

        TableColumn<Student, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(150);

        TableColumn<Student, String> rollCol = new TableColumn<>("Roll No");
        rollCol.setCellValueFactory(new PropertyValueFactory<>("rollNo"));
        rollCol.setPrefWidth(120);

        TableColumn<Student, Integer> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));
        yearCol.setPrefWidth(60);

        TableColumn<Student, Integer> semCol = new TableColumn<>("Semester");
        semCol.setCellValueFactory(new PropertyValueFactory<>("semester"));
        semCol.setPrefWidth(80);

        TableColumn<Student, String> branchCol = new TableColumn<>("Branch");
        branchCol.setCellValueFactory(new PropertyValueFactory<>("branch"));
        branchCol.setPrefWidth(100);

        studentTable.getColumns().addAll(nameCol, rollCol, yearCol, semCol, branchCol);

        // Summary labels
        studentCountLabel = new Label("Students loaded: 0");
        studentCountLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        branchSummaryLabel = new Label("Branches: -");

        HBox summaryBox = new HBox(30);
        summaryBox.getChildren().addAll(studentCountLabel, branchSummaryLabel);

        content.getChildren().addAll(header, fileBox, formatInfo, studentTable, summaryBox);

        tab.setContent(content);
        return tab;
    }

    private void loadStudentsFromExcel(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No File", "Please select an Excel file first.");
            return;
        }

        try {
            students = ExcelReader.readStudents(filePath);

            ObservableList<Student> data = FXCollections.observableArrayList(students);
            studentTable.setItems(data);

            studentCountLabel.setText("Students loaded: " + students.size());

            // Calculate branch summary and populate available branches
            availableBranches.clear();
            Map<String, Long> branchCounts = new LinkedHashMap<>();
            for (Student s : students) {
                branchCounts.merge(s.getBranch(), 1L, Long::sum);
                availableBranches.add(s.getBranch());
            }
            StringBuilder summary = new StringBuilder("Branches: ");
            branchCounts.forEach((branch, count) -> summary.append(branch).append("(").append(count).append(") "));
            branchSummaryLabel.setText(summary.toString());
            
            // Update branch menus in classroom tab
            updateBranchMenus();

            showAlert(Alert.AlertType.INFORMATION, "Students Loaded", 
                      "Loaded " + students.size() + " students from Excel file.\n" +
                      "Found " + availableBranches.size() + " branches: " + 
                      String.join(", ", availableBranches) + "\n\n" +
                      "You can now go back to Tab 1 to assign specific branches to rooms.");

            tabPane.getSelectionModel().select(2); // Move to Pattern tab

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error Loading File", e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== PATTERN TAB ====================

    private Tab createPatternTab() {
        Tab tab = new Tab("3. Pattern & Options");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Header
        Label header = new Label("Seating Pattern & Customization");
        header.setFont(Font.font("System", FontWeight.BOLD, 18));

        // ===== PATTERN SELECTION =====
        Label patternHeader = new Label("1. Select Seating Pattern");
        patternHeader.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        patternGroup = new ToggleGroup();
        VBox patternsBox = new VBox(10);
        patternsBox.setPadding(new Insets(10));
        patternsBox.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5;");

        for (PatternType pattern : PatternType.values()) {
            RadioButton rb = new RadioButton(pattern.getDisplayName());
            rb.setToggleGroup(patternGroup);
            rb.setUserData(pattern);

            Label desc = new Label("   " + pattern.getDescription());
            desc.setStyle("-fx-text-fill: #666; -fx-font-size: 11;");

            VBox patternItem = new VBox(2);
            patternItem.getChildren().addAll(rb, desc);
            patternsBox.getChildren().add(patternItem);
        }
        patternGroup.getToggles().get(0).setSelected(true);

        // ===== FILL DIRECTION OPTIONS =====
        Label directionHeader = new Label("2. Fill Direction & Starting Position");
        directionHeader.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        GridPane directionGrid = new GridPane();
        directionGrid.setHgap(15);
        directionGrid.setVgap(10);
        directionGrid.setPadding(new Insets(10));
        directionGrid.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5;");
        
        Label fillDirLabel = new Label("Fill Direction:");
        fillDirectionCombo = new ComboBox<>();
        fillDirectionCombo.getItems().addAll(FillDirection.values());
        fillDirectionCombo.setValue(FillDirection.ROW_FIRST);
        fillDirectionCombo.setPrefWidth(200);
        fillDirectionCombo.setTooltip(new Tooltip("How seats are filled within each room"));
        
        Label startCornerLabel = new Label("Start From:");
        startCornerCombo = new ComboBox<>();
        startCornerCombo.getItems().addAll(StartCorner.values());
        startCornerCombo.setValue(StartCorner.TOP_LEFT);
        startCornerCombo.setPrefWidth(200);
        startCornerCombo.setTooltip(new Tooltip("Which corner to start seating from"));
        
        directionGrid.add(fillDirLabel, 0, 0);
        directionGrid.add(fillDirectionCombo, 1, 0);
        directionGrid.add(startCornerLabel, 0, 1);
        directionGrid.add(startCornerCombo, 1, 1);

        // ===== SEAT GAPPING OPTIONS =====
        Label gappingHeader = new Label("3. Seat Spacing");
        gappingHeader.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        HBox gappingBox = new HBox(15);
        gappingBox.setPadding(new Insets(10));
        gappingBox.setAlignment(Pos.CENTER_LEFT);
        gappingBox.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5;");
        
        Label gapLabel = new Label("Spacing Pattern:");
        seatGappingCombo = new ComboBox<>();
        seatGappingCombo.getItems().addAll(SeatGapping.values());
        seatGappingCombo.setValue(SeatGapping.NO_GAP);
        seatGappingCombo.setPrefWidth(200);
        
        Label gapDesc = new Label("");
        gapDesc.setStyle("-fx-text-fill: #666;");
        seatGappingCombo.setOnAction(e -> {
            SeatGapping selected = seatGappingCombo.getValue();
            gapDesc.setText(selected != null ? selected.getDescription() : "");
        });
        
        gappingBox.getChildren().addAll(gapLabel, seatGappingCombo, gapDesc);

        // ===== STUDENT SORTING =====
        Label sortHeader = new Label("4. Student Sort Order");
        sortHeader.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        HBox sortBox = new HBox(15);
        sortBox.setPadding(new Insets(10));
        sortBox.setAlignment(Pos.CENTER_LEFT);
        sortBox.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5;");
        
        Label sortLabel = new Label("Sort Students By:");
        sortOrderCombo = new ComboBox<>();
        sortOrderCombo.getItems().addAll(StudentSortOrder.values());
        sortOrderCombo.setValue(StudentSortOrder.ROLL_NUMBER);
        sortOrderCombo.setPrefWidth(200);
        
        sortBox.getChildren().addAll(sortLabel, sortOrderCombo);

        // ===== CONSTRAINT OPTIONS =====
        Label constraintHeader = new Label("5. Neighbor Constraints");
        constraintHeader.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        VBox constraintBox = new VBox(8);
        constraintBox.setPadding(new Insets(10));
        constraintBox.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5;");
        
        enforceBranchCheck = new CheckBox("No same branch adjacent (left/right/front/back)");
        enforceBranchCheck.setSelected(true);
        
        enforceYearCheck = new CheckBox("No same year adjacent");
        enforceYearCheck.setSelected(false);
        
        Label constraintNote = new Label("Note: Constraints are best-effort. If impossible to satisfy, some violations may occur.");
        constraintNote.setStyle("-fx-text-fill: #999; -fx-font-size: 10;");
        
        constraintBox.getChildren().addAll(enforceBranchCheck, enforceYearCheck, constraintNote);

        // ===== EXAM DETAILS =====
        Label examHeader = new Label("6. Exam Details (Optional)");
        examHeader.setFont(Font.font("System", FontWeight.BOLD, 14));

        GridPane examGrid = new GridPane();
        examGrid.setHgap(10);
        examGrid.setVgap(10);
        examGrid.setPadding(new Insets(10));
        examGrid.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5;");

        Label examNameLabel = new Label("Exam Name:");
        examNameField = new TextField();
        examNameField.setPromptText("e.g., Mid-Term Examination");
        examNameField.setPrefWidth(250);

        Label examDateLabel = new Label("Exam Date:");
        examDatePicker = new DatePicker(LocalDate.now());

        examGrid.add(examNameLabel, 0, 0);
        examGrid.add(examNameField, 1, 0);
        examGrid.add(examDateLabel, 0, 1);
        examGrid.add(examDatePicker, 1, 1);

        // Generate button
        Button generateBtn = new Button("Generate Seating Plan");
        generateBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 14;");
        generateBtn.setPrefWidth(200);
        generateBtn.setOnAction(e -> generateSeatingPlan());

        content.getChildren().addAll(
            header, 
            patternHeader, patternsBox,
            new Separator(),
            directionHeader, directionGrid,
            new Separator(),
            gappingHeader, gappingBox,
            new Separator(),
            sortHeader, sortBox,
            new Separator(),
            constraintHeader, constraintBox,
            new Separator(),
            examHeader, examGrid, 
            new Separator(),
            generateBtn
        );

        scrollPane.setContent(content);
        tab.setContent(scrollPane);
        return tab;
    }

    private void generateSeatingPlan() {
        // Validate
        if (classrooms.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Classrooms", 
                      "Please configure classrooms first (Tab 1).");
            tabPane.getSelectionModel().select(0);
            return;
        }

        if (students.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Students", 
                      "Please load student data first (Tab 2).");
            tabPane.getSelectionModel().select(1);
            return;
        }

        int totalCapacity = classrooms.stream().mapToInt(Classroom::getCapacity).sum();
        if (students.size() > totalCapacity) {
            showAlert(Alert.AlertType.WARNING, "Insufficient Capacity", 
                      "Not enough seats! Students: " + students.size() + 
                      ", Capacity: " + totalCapacity + 
                      "\nPlease add more classrooms or increase room sizes.");
            return;
        }

        // Build SeatingConfig from UI selections
        seatingConfig = new SeatingConfig();
        seatingConfig.setFillDirection(fillDirectionCombo.getValue());
        seatingConfig.setStartCorner(startCornerCombo.getValue());
        seatingConfig.setSeatGapping(seatGappingCombo.getValue());
        seatingConfig.setSortOrder(sortOrderCombo.getValue());
        seatingConfig.setEnforceNoSameBranchAdjacent(enforceBranchCheck.isSelected());
        seatingConfig.setEnforceNoSameYearAdjacent(enforceYearCheck.isSelected());

        // Get selected pattern
        RadioButton selected = (RadioButton) patternGroup.getSelectedToggle();
        PatternType patternType = (PatternType) selected.getUserData();

        // Generate plan with config
        SeatingAlgorithm algorithm = SeatingAlgorithmFactory.getAlgorithm(patternType);
        currentPlan = algorithm.allocate(students, classrooms, seatingConfig);

        // Set exam details
        currentPlan.setExamName(examNameField.getText().trim());
        if (examDatePicker.getValue() != null) {
            currentPlan.setExamDate(examDatePicker.getValue().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        }

        // Update preview
        updatePreview();

        // Build detailed summary
        StringBuilder summary = new StringBuilder();
        summary.append("Seating plan generated successfully!\n\n");
        summary.append("Pattern: ").append(patternType.getDisplayName()).append("\n");
        summary.append("Fill Direction: ").append(seatingConfig.getFillDirection().getDisplayName()).append("\n");
        summary.append("Start Corner: ").append(seatingConfig.getStartCorner().getDisplayName()).append("\n");
        summary.append("Spacing: ").append(seatingConfig.getSeatGapping().getDisplayName()).append("\n\n");
        summary.append("Students seated: ").append(currentPlan.getTotalOccupied())
               .append("/").append(students.size()).append("\n");
        
        if (!currentPlan.getUnassignedStudents().isEmpty()) {
            summary.append("\n⚠ ").append(currentPlan.getUnassignedStudents().size())
                   .append(" students could not be assigned.");
        }

        showAlert(Alert.AlertType.INFORMATION, "Plan Generated", summary.toString());
        tabPane.getSelectionModel().select(3); // Move to Generate tab
    }

    // ==================== GENERATE TAB ====================

    private Tab createGenerateTab() {
        Tab tab = new Tab("4. Preview & Export");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Header
        Label header = new Label("Preview & Export Seating Plan");
        header.setFont(Font.font("System", FontWeight.BOLD, 18));

        // Status
        statusLabel = new Label("Generate a seating plan first to preview.");
        statusLabel.setStyle("-fx-text-fill: #666;");

        // Preview area
        previewContainer = new VBox(20);
        previewScrollPane = new ScrollPane(previewContainer);
        previewScrollPane.setFitToWidth(true);
        previewScrollPane.setPrefHeight(450);
        previewScrollPane.setStyle("-fx-border-color: #ccc;");

        // Export buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button exportPdfBtn = new Button("Export to PDF");
        exportPdfBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14;");
        exportPdfBtn.setPrefWidth(150);
        exportPdfBtn.setOnAction(e -> exportToPdf());

        Button regenerateBtn = new Button("Regenerate Plan");
        regenerateBtn.setOnAction(e -> {
            tabPane.getSelectionModel().select(2);
        });

        buttonBox.getChildren().addAll(exportPdfBtn, regenerateBtn);

        content.getChildren().addAll(header, statusLabel, previewScrollPane, buttonBox);

        tab.setContent(content);
        return tab;
    }

    private void updatePreview() {
        previewContainer.getChildren().clear();

        if (currentPlan == null) {
            statusLabel.setText("No seating plan generated yet.");
            return;
        }

        statusLabel.setText("Pattern: " + currentPlan.getPatternUsed().getDisplayName() + 
                           " | Students: " + currentPlan.getTotalOccupied() + "/" + currentPlan.getStudents().size());

        // Create preview for each classroom
        for (Classroom classroom : currentPlan.getClassrooms()) {
            VBox roomBox = new VBox(5);
            roomBox.setPadding(new Insets(10));
            roomBox.setStyle("-fx-border-color: #2196F3; -fx-border-width: 2; -fx-border-radius: 5;");

            // Room header with branch info
            String roomInfo = classroom.getRoomName() + " (" + 
                              classroom.getOccupiedCount() + "/" + classroom.getCapacity() + " occupied)";
            if (classroom.hasBranchRestrictions()) {
                roomInfo += " - Branches: " + classroom.getBranchSummary();
            }
            Label roomLabel = new Label(roomInfo);
            roomLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

            GridPane grid = new GridPane();
            grid.setHgap(3);
            grid.setVgap(3);

            // Column headers
            for (int c = 0; c < classroom.getColumns(); c++) {
                Label colHeader = new Label("C" + (c + 1));
                colHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 10;");
                colHeader.setAlignment(Pos.CENTER);
                colHeader.setPrefWidth(80);
                grid.add(colHeader, c + 1, 0);
            }

            // Rows with data
            for (int r = 0; r < classroom.getRows(); r++) {
                Label rowHeader = new Label("R" + (r + 1));
                rowHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 10;");
                grid.add(rowHeader, 0, r + 1);

                for (int c = 0; c < classroom.getColumns(); c++) {
                    Seat seat = classroom.getSeat(r, c);
                    Label seatLabel = createSeatLabel(seat);
                    grid.add(seatLabel, c + 1, r + 1);
                }
            }

            roomBox.getChildren().addAll(roomLabel, grid);
            previewContainer.getChildren().add(roomBox);
        }

        // Add unassigned students warning if any
        if (!currentPlan.getUnassignedStudents().isEmpty()) {
            Label warning = new Label("⚠ " + currentPlan.getUnassignedStudents().size() + 
                                     " students could not be assigned seats.");
            warning.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
            previewContainer.getChildren().add(0, warning);
        }
    }

    private Label createSeatLabel(Seat seat) {
        Label label = new Label();
        label.setPrefSize(80, 40);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-border-color: #999; -fx-border-width: 1; -fx-font-size: 9;");

        if (seat.isOccupied()) {
            Student s = seat.getAssignedStudent();
            label.setText(s.getRollNo() + "\n" + truncateName(s.getName(), 10));

            // Color by branch
            String color = getBranchColor(s.getBranch());
            label.setStyle(label.getStyle() + "-fx-background-color: " + color + ";");
        } else {
            label.setText("[Empty]");
            label.setStyle(label.getStyle() + "-fx-background-color: #f5f5f5;");
        }

        return label;
    }

    private String truncateName(String name, int maxLen) {
        if (name == null) return "";
        if (name.length() <= maxLen) return name;
        return name.substring(0, maxLen - 2) + "..";
    }

    private String getBranchColor(String branch) {
        // Generate consistent colors for branches
        int hash = branch.hashCode();
        String[] colors = {"#FFCDD2", "#C8E6C9", "#BBDEFB", "#FFF9C4", "#E1BEE7", 
                          "#B2DFDB", "#FFE0B2", "#F0F4C3", "#D1C4E9", "#B3E5FC"};
        return colors[Math.abs(hash) % colors.length];
    }

    private void exportToPdf() {
        if (currentPlan == null) {
            showAlert(Alert.AlertType.WARNING, "No Plan", 
                      "Please generate a seating plan first.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Seating Plan PDF");
        fileChooser.setInitialFileName("seating_plan.pdf");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                PdfGenerator.generatePdf(currentPlan, file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "PDF Exported", 
                          "Seating plan saved to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Export Failed", 
                          "Failed to generate PDF:\n" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void show() {
        stage.show();
    }
}
