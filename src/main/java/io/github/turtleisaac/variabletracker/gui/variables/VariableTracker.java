/*
 * Created by JFormDesigner
 */

package io.github.turtleisaac.variabletracker.gui.variables;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.*;
import javax.swing.border.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.turtleisaac.variabletracker.ScriptVariable;
import io.github.turtleisaac.variabletracker.gui.cells.HexadecimalCellRenderer;
import io.github.turtleisaac.variabletracker.gui.cells.NumberCellEditor;
import net.miginfocom.swing.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author turtleisaac
 */
public class VariableTracker extends JPanel
{
    private static final String ADD_VARIABLE_POPUP_MENU_TEXT;
    private static final String REMOVE_VARIABLE_POPUP_MENU_TEXT;
    private static final String COPY_VARIABLE_NAME_POPUP_MENU_TEXT;
    private static final String ADD_VARIABLE_DIALOG_PROMPT_TEXT;
    private static final String ADD_VARIABLE_DIALOG_ID_CONFLICT_TEXT;
    private static final String ADD_VARIABLE_DIALOG_INVALID_NUMBER_TEXT;
    private static final String REMOVE_VARIABLE_ERROR_TEXT;
    private static final String HELP_INFO_TEXT;

    static {
        ResourceBundle bundle = ResourceBundle.getBundle("variable_tracker.gui");

        ADD_VARIABLE_POPUP_MENU_TEXT = bundle.getString("variableTable.popUp.addItem.text");
        REMOVE_VARIABLE_POPUP_MENU_TEXT = bundle.getString("variableTable.popUp.removeItem.text");
        COPY_VARIABLE_NAME_POPUP_MENU_TEXT = bundle.getString("variableTable.popUp.copyNameItem.text");

        ADD_VARIABLE_DIALOG_PROMPT_TEXT = bundle.getString("variableTable.addDialog.prompt.text");
        ADD_VARIABLE_DIALOG_ID_CONFLICT_TEXT = bundle.getString("variableTable.addDialog.idConflict.text");
        ADD_VARIABLE_DIALOG_INVALID_NUMBER_TEXT = bundle.getString("variableTable.addDialog.invalidNumber.text");
        REMOVE_VARIABLE_ERROR_TEXT = bundle.getString("variableTable.removeItem.tempVarRemovalFailure.text");
        HELP_INFO_TEXT = bundle.getString("VariableTracker.infoDialog.text");
    }

    private List<ScriptVariable> variableList;

    private int selectedIndex = 0;

    private List<JMenuItem> developerDefinedPopupMenuItems = new ArrayList<>();

    public VariableTracker(String variableListJson)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        List<ScriptVariable> variableList;

        try {
            variableList = new ArrayList<>(Arrays.asList(objectMapper.readValue(variableListJson, ScriptVariable[].class)));
        }
        catch(JsonProcessingException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e);
        }

        this.variableList = variableList;
        prepare();
    }

    public VariableTracker(List<ScriptVariable> variableList)
    {
        this.variableList = variableList;
        prepare();
    }

    public VariableTracker() {
        variableList = new ArrayList<>();
        prepare();
    }

    private static final String LOCAL_VAR_PREFIX = "VAR_LOCAL_";
    private static final String TEMP_VAR_PREFIX = "VAR_TEMP_0";

    private void prepare()
    {
        ResourceBundle bundle = ResourceBundle.getBundle("variable_tracker.text");

        for (int i = 0; i < 0x20; i++)
        {
            ScriptVariable variable = new ScriptVariable(0x4000 + i);
            String hex = Integer.toHexString(i);
            if (hex.length() == 1)
                hex = "0" + hex;
            variable.setVariableName(LOCAL_VAR_PREFIX + hex.toUpperCase());
            variable.setVariableDescription(bundle.getString("localVarDescription.text"));
            variable.setTemp(true);
            variableList.add(variable);
        }

        for (int i = 0; i < 0xD; i++)
        {
            ScriptVariable variable = new ScriptVariable(0x8000 + i);
            String hex = Integer.toHexString(i);
            variable.setVariableName(TEMP_VAR_PREFIX + hex.toUpperCase());
            variable.setVariableDescription(bundle.getString("tempVarDescription.text"));

            variable.setTemp(true);
            variableList.add(variable);
        }

//        ScriptVariable variable = new ScriptVariable(0x4020);
//        variable.setVariableName("VAR_TUTORIAL_PROGRESS");
//        ScriptVariable.VariableValue variableValue = variable.createVariableValue(0);
//        variableValue.setValueName("INCOMPLETE");
//        variable.getVariableValues().add(variableValue);
//        variableList.add(variable);
//
//        variable = new ScriptVariable(0x4021);
//        variable.setVariableName("VAR_THING_2");
//        variableValue = variable.createVariableValue(0);
//        variableValue.setValueName("INCOMPLETE");
//        variable.getVariableValues().add(variableValue);
//        variableValue = variable.createVariableValue(1);
//        variableValue.setValueName("FIRST_STEP");
//        variable.getVariableValues().add(variableValue);
//        variableValue = variable.createVariableValue(2);
//        variableValue.setValueName("COMPLETE");
//        variable.getVariableValues().add(variableValue);
//        variableList.add(variable);
//
//        variable = new ScriptVariable(0x4022);
//        variable.setVariableName("VAR_THING_3");
//        variableList.add(variable);

        initComponents();

        ListSelectionModel selectionModel = variableTable.getSelectionModel();
        selectionModel.addListSelectionListener(this::selectedVariableChanged);

        variableTable.setRowSelectionInterval(0, 0);

        variableDescriptionTextArea.getDocument().addDocumentListener(new DocumentListener()
        {
            private void update()
            {
                variableList.get(selectedIndex).setVariableDescription(variableDescriptionTextArea.getText());
            }

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                update();
            }
        });

        variableTable.getRowSorter().toggleSortOrder(0);
    }

    private void selectedVariableChanged(ListSelectionEvent e)
    {
        if (variableTable.getSelectedRow() != -1)
        {
            selectedIndex = variableTable.convertRowIndexToModel(variableTable.getSelectedRow());
            ScriptVariable variable = variableList.get(selectedIndex);
            updateUI();
            selectedTextField.setText(variable.toString());
            variableDescriptionTextArea.setText(variable.getVariableDescription());

            variableDescriptionTextArea.setEnabled(variable.isNotTemp());
            variableValuesTable.setEnabled(variable.isNotTemp());
            scrollPane2.setEnabled(variable.isNotTemp());

            variableValuesTable.updateUI();
        }
        else
        {
            selectedTextField.setText("");
            variableDescriptionTextArea.setText("");
        }
    }

    public List<ScriptVariable> getVariableList()
    {
        return variableList;
    }

    public void setVariableList(List<ScriptVariable> variableList)
    {
        this.variableList = variableList;
        ((DefaultTableModel) variableTable.getModel()).fireTableDataChanged();
    }

    public ScriptVariable getSelectedVariable()
    {
        return variableList.get(selectedIndex);
    }

    public JMenuBar getMenuBar()
    {
        return menuBar;
    }

    public void addDeveloperDefinedPopupMenuItem(JMenuItem popupMenuItem)
    {
        developerDefinedPopupMenuItems.add(popupMenuItem);
    }

    public void fireTableDataChanged()
    {
        ((DefaultTableModel) variableTable.getModel()).fireTableDataChanged();
        ((DefaultTableModel) variableValuesTable.getModel()).fireTableDataChanged();
    }

    private void variableTableMousePressed(MouseEvent e) {
//        variableTableMouseAction(e);
//        System.out.println("press");
    }

    private void variableTableMouseReleased(MouseEvent e) {
        variableTableMouseAction(e);
//        System.out.println("release");
    }

    private void variableTableMouseAction(MouseEvent e)
    {
        if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem removeItem = new JMenuItem(REMOVE_VARIABLE_POPUP_MENU_TEXT);
            JMenuItem addItem = new JMenuItem(ADD_VARIABLE_POPUP_MENU_TEXT);
            JMenuItem copyItem = new JMenuItem(COPY_VARIABLE_NAME_POPUP_MENU_TEXT);

            int r = variableTable.rowAtPoint(e.getPoint());
            if (r >= 0 && r < variableTable.getRowCount()) {
                variableTable.setRowSelectionInterval(r, r);
                menu.add(removeItem);
            } else {
                variableTable.clearSelection();
            }

            if (variableTable.isEditing())
                variableTable.getCellEditor().stopCellEditing();

            int modelIndex = variableTable.convertRowIndexToModel(r);

            removeItem.addActionListener(e12 ->
            {
                if (!variableList.get(modelIndex).isNotTemp())
                {
                    JOptionPane.showMessageDialog(this, REMOVE_VARIABLE_ERROR_TEXT, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                variableTable.clearSelection();
                ((DefaultTableModel) variableTable.getModel()).removeRow(modelIndex);
                ((DefaultTableModel) variableTable.getModel()).fireTableDataChanged();
            });

            addItem.addActionListener(e1 -> createNewEntry());

            copyItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    StringSelection selection = new StringSelection(variableList.get(modelIndex).getVariableName());
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, selection);
                }
            });

            menu.add(addItem);
            developerDefinedPopupMenuItems.forEach(menu::add);
            menu.show(variableTable, e.getX(), e.getY());
        }
    }

    private void scrollPane1MouseReleased(MouseEvent e) {
        if ((e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) && variableList.isEmpty()) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem addItem = new JMenuItem(ADD_VARIABLE_POPUP_MENU_TEXT);

            addItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    createNewEntry();
                }
            });

            menu.add(addItem);
            menu.show(scrollPane1, e.getX(), e.getY());
        }
    }

    private void createNewEntry()
    {
        String userResponse = JOptionPane.showInputDialog(this, ADD_VARIABLE_DIALOG_PROMPT_TEXT);
        if (userResponse == null)
            return;

        int num = -1;
        try {
            if (userResponse.startsWith("0x"))
                num = Integer.parseInt(userResponse.substring(2), 16);
            else
                num = Integer.parseInt(userResponse);
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(this, ADD_VARIABLE_DIALOG_INVALID_NUMBER_TEXT, "Error", JOptionPane.ERROR_MESSAGE);
        }

        for (ScriptVariable variable : variableList)
        {
            if (variable.getVariableID() == num)
            {
                JOptionPane.showMessageDialog(this, ADD_VARIABLE_DIALOG_ID_CONFLICT_TEXT, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        variableList.add(new ScriptVariable(num));
        ((DefaultTableModel) variableTable.getModel()).fireTableDataChanged();
    }

    private void hideTempVarButtonPressed(ActionEvent e) {
        TableRowSorter<VariableTableModel> tableModelTableRowSorter = (TableRowSorter<VariableTableModel>) variableTable.getRowSorter();

        if (hideTempVarButton.isSelected())
        {
            if (!variableList.get(selectedIndex).isNotTemp())
            {
                variableTable.clearSelection();
            }

            tableModelTableRowSorter.setRowFilter(new RowFilter<VariableTableModel, Integer>()
            {
                @Override
                public boolean include(Entry<? extends VariableTableModel, ? extends Integer> entry)
                {
                    ScriptVariable variable = variableList.get(entry.getIdentifier());
                    return variable.isNotTemp();
                }
            });
        }
        else
        {
            tableModelTableRowSorter.setRowFilter(null);
        }

    }

    private void infoMenuItemPressed(ActionEvent e) {
        JOptionPane.showMessageDialog(this, HELP_INFO_TEXT, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public void postUpdateVariableTableAction()
    {

    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner non-commercial license
        ResourceBundle bundle = ResourceBundle.getBundle("variable_tracker.gui");
        splitPane1 = new JSplitPane();
        panel2 = new JPanel();
        variablesLabel = new JLabel();
        hideTempVarButton = new JRadioButton();
        scrollPane1 = new JScrollPane();
        variableTable = new VariableTable();
        vSpacer1 = new JPanel(null);
        panel1 = new JPanel();
        selectedLabel = new JLabel();
        selectedTextField = new JTextField();
        variableDescriptionLabel = new JLabel();
        scrollPane3 = new JScrollPane();
        variableDescriptionTextArea = new JTextArea();
        scrollPane2 = new JScrollPane();
        variableValuesTable = new VariableValueTable();
        vSpacer2 = new JPanel(null);
        toolBar1 = new JToolBar();
        searchLabel = new JLabel();
        searchTextField = new JTextField();
        optionsButton = new JButton();
        menuBar = new JMenuBar();
        fileMenu = new JMenu();
        openMenuItem = new JMenuItem();
        saveMenuItem = new JMenuItem();
        editMenu = new JMenu();
        helpMenu = new JMenu();
        infoMenuItem = new JMenuItem();

        //======== this ========
        setMinimumSize(new Dimension(650, 400));
        setPreferredSize(new Dimension(650, 400));
        setLayout(new MigLayout(
            "hidemode 3",
            // columns
            "[grow,fill]",
            // rows
            "[grow,fill]"));

        //======== splitPane1 ========
        {
            splitPane1.setLastDividerLocation(-1);
            splitPane1.setDividerLocation(321);
            splitPane1.setBorder(LineBorder.createBlackLineBorder());

            //======== panel2 ========
            {
                panel2.setLayout(new MigLayout(
                    "insets 0 5 0 5,hidemode 3",
                    // columns
                    "[grow,fill]",
                    // rows
                    "[]" +
                    "[grow,fill]" +
                    "[]"));

                //---- variablesLabel ----
                variablesLabel.setText(bundle.getString("VariableTracker.variablesLabel.text"));
                panel2.add(variablesLabel, "cell 0 0");

                //---- hideTempVarButton ----
                hideTempVarButton.setText(bundle.getString("VariableTracker.hideTempVarButton.text"));
                hideTempVarButton.addActionListener(e -> hideTempVarButtonPressed(e));
                panel2.add(hideTempVarButton, "cell 0 0,alignx right,growx 0");

                //======== scrollPane1 ========
                {
                    scrollPane1.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseReleased(MouseEvent e) {
                            scrollPane1MouseReleased(e);
                        }
                    });

                    //---- variableTable ----
                    TableModel variableTableModel = variableTable.getModel();
                    variableTable.setAutoCreateRowSorter(true);
                    variableTable.setModel(new DefaultTableModel());
                    variableTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    variableTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
                    variableTable.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                            variableTableMousePressed(e);
                        }
                        @Override
                        public void mouseReleased(MouseEvent e) {
                            variableTableMouseReleased(e);
                        }
                    });
                    variableTable.setModel(variableTableModel);
                    ((VariableTable) variableTable).updateRenderersAndEditors();
                    scrollPane1.setViewportView(variableTable);
                }
                panel2.add(scrollPane1, "cell 0 1");
                panel2.add(vSpacer1, "cell 0 2");
            }
            splitPane1.setLeftComponent(panel2);

            //======== panel1 ========
            {
                panel1.setLayout(new MigLayout(
                    "insets 0 5 0 5,hidemode 3",
                    // columns
                    "[grow,fill]",
                    // rows
                    "[]" +
                    "[top]" +
                    "[grow,fill]" +
                    "[grow,top]" +
                    "[]"));

                //---- selectedLabel ----
                selectedLabel.setText(bundle.getString("VariableTracker.selectedLabel.text"));
                panel1.add(selectedLabel, "cell 0 0,alignx left,growx 0");

                //---- selectedTextField ----
                selectedTextField.setEditable(false);
                selectedTextField.setEnabled(false);
                panel1.add(selectedTextField, "cell 0 0");

                //---- variableDescriptionLabel ----
                variableDescriptionLabel.setText(bundle.getString("VariableTracker.variableDescriptionLabel.text"));
                panel1.add(variableDescriptionLabel, "cell 0 1,aligny top,growy 0");

                //======== scrollPane3 ========
                {

                    //---- variableDescriptionTextArea ----
                    variableDescriptionTextArea.setWrapStyleWord(true);
                    variableDescriptionTextArea.setLineWrap(true);
                    variableDescriptionTextArea.setWrapStyleWord(true);
                    scrollPane3.setViewportView(variableDescriptionTextArea);
                }
                panel1.add(scrollPane3, "cell 0 2,growy");

                //======== scrollPane2 ========
                {

                    //---- variableValuesTable ----
                    TableModel variableValueTableModel = variableValuesTable.getModel();
                    variableValuesTable.setModel(new DefaultTableModel(
                        new Object[][] {
                            {null, null, null},
                            {null, null, null},
                        },
                        new String[] {
                            "Value", "Name", "Description"
                        }
                    ) {
                        Class<?>[] columnTypes = new Class<?>[] {
                            Integer.class, String.class, String.class
                        };
                        @Override
                        public Class<?> getColumnClass(int columnIndex) {
                            return columnTypes[columnIndex];
                        }
                    });
                    variableValuesTable.setPreferredScrollableViewportSize(new Dimension(450, 150));
                    variableValuesTable.setModel(variableValueTableModel);
                    ((VariableValueTable) variableValuesTable).updateRenderersAndEditors();
                    scrollPane2.setViewportView(variableValuesTable);
                }
                panel1.add(scrollPane2, "cell 0 3,grow");
                panel1.add(vSpacer2, "cell 0 4");
            }
            splitPane1.setRightComponent(panel1);
        }
        add(splitPane1, "cell 0 0,growx");

        //======== toolBar1 ========
        {
            toolBar1.setFloatable(false);

            //---- searchLabel ----
            searchLabel.setText(bundle.getString("VariableTracker.searchLabel.text"));
            toolBar1.add(searchLabel);
            toolBar1.add(searchTextField);
            toolBar1.addSeparator();

            //---- optionsButton ----
            optionsButton.setText(bundle.getString("VariableTracker.optionsButton.text"));
            toolBar1.add(optionsButton);
        }
        add(toolBar1, "north");

        //======== menuBar ========
        {

            //======== fileMenu ========
            {
                fileMenu.setText(bundle.getString("VariableTracker.fileMenu.text"));

                //---- openMenuItem ----
                openMenuItem.setText(bundle.getString("VariableTracker.openMenuItem.text"));
                fileMenu.add(openMenuItem);

                //---- saveMenuItem ----
                saveMenuItem.setText(bundle.getString("VariableTracker.saveMenuItem.text"));
                fileMenu.add(saveMenuItem);
            }
            menuBar.add(fileMenu);

            //======== editMenu ========
            {
                editMenu.setText(bundle.getString("VariableTracker.editMenu.text"));
            }
            menuBar.add(editMenu);

            //======== helpMenu ========
            {
                helpMenu.setText(bundle.getString("VariableTracker.helpMenu.text"));

                //---- infoMenuItem ----
                infoMenuItem.setText(bundle.getString("VariableTracker.infoMenuItem.text"));
                infoMenuItem.addActionListener(e -> infoMenuItemPressed(e));
                helpMenu.add(infoMenuItem);
            }
            menuBar.add(helpMenu);
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner non-commercial license
    private JSplitPane splitPane1;
    private JPanel panel2;
    private JLabel variablesLabel;
    private JRadioButton hideTempVarButton;
    private JScrollPane scrollPane1;
    private JTable variableTable;
    private JPanel vSpacer1;
    private JPanel panel1;
    private JLabel selectedLabel;
    private JTextField selectedTextField;
    private JLabel variableDescriptionLabel;
    private JScrollPane scrollPane3;
    private JTextArea variableDescriptionTextArea;
    private JScrollPane scrollPane2;
    private JTable variableValuesTable;
    private JPanel vSpacer2;
    private JToolBar toolBar1;
    private JLabel searchLabel;
    private JTextField searchTextField;
    private JButton optionsButton;
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem openMenuItem;
    private JMenuItem saveMenuItem;
    private JMenu editMenu;
    private JMenu helpMenu;
    private JMenuItem infoMenuItem;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    private class VariableTable extends JTable
    {
        VariableTable()
        {
            super(new VariableTableModel());
            getTableHeader().setReorderingAllowed(false);
        }

        @Override
        public String getToolTipText(MouseEvent event)
        {
            int rowIdx = rowAtPoint(event.getPoint());
            int modelIdx = convertRowIndexToModel(rowIdx);

            int colIdx = convertColumnIndexToModel(columnAtPoint(event.getPoint()));

            ScriptVariable variable = variableList.get(modelIdx);
            return switch (colIdx)
            {
                case 0 -> "0x" + Integer.toHexString(variable.getVariableID()).toUpperCase();
                case 1 -> String.valueOf(variable.getVariableID());
                case 2 -> variable.getVariableName();
                default -> throw new RuntimeException("Error, invalid column");
            };
        }

        private final int[] widths = new int[] {60, 75};

        void updateRenderersAndEditors()
        {
            DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
                {
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (table.getValueAt(row, 0) instanceof Integer val)
                    {
                        setEnabled(!((val >= 0x4000 && val <= 0x401F) || val >= 0x8000));
                    }
                    return this;
                }
            };

            setDefaultRenderer(Object.class, renderer);
            setDefaultRenderer(Integer.class, renderer);

            for (int i = 0; i < VariableTableModel.NUM_COLUMNS; i++)
            {
                TableColumn col = getColumnModel().getColumn(i);

                if (i == 0)
                {
                    col.setCellEditor(new NumberCellEditor(true));
                    col.setCellRenderer(new HexadecimalCellRenderer());
                    col.setMaxWidth(widths[i]);
                }
                else if (i == 1)
                {
                    col.setCellEditor(new NumberCellEditor(false));
                    col.setMaxWidth(widths[i]);
                }
            }
        }
    }

    private class VariableTableModel extends DefaultTableModel
    {
        List<String> headerStrings = new ArrayList<>();

        public VariableTableModel()
        {
            super();
            ResourceBundle bundle = ResourceBundle.getBundle("variable_tracker.gui");

            headerStrings.add(bundle.getString("VariableTracker.variableTable.varHex"));
            headerStrings.add(bundle.getString("VariableTracker.variableTable.varDecimal"));
            headerStrings.add(bundle.getString("VariableTracker.variableTable.varName"));
        }

        @Override
        public boolean isCellEditable(int row, int column)
        {
            return variableList.get(row).isNotTemp();
        }

        @Override
        public int getRowCount()
        {
            return variableList.size();
        }

        @Override
        public void removeRow(int row)
        {
            super.removeRow(row);
            variableList.remove(row);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (variableList.isEmpty()) {
                return Object.class;
            }
            return getValueAt(0, columnIndex).getClass();
        }

        private static final int NUM_COLUMNS = 3;

        @Override
        public int getColumnCount()
        {
            return NUM_COLUMNS;
        }

        @Override
        public Object getValueAt(int row, int column)
        {
            if (row >= variableList.size())
                return null;

            ScriptVariable variable = variableList.get(row);

            if (column == 0 || column == 1)
            {
                return variable.getVariableID();
            }
            else
            {
                return variable.getVariableName();
            }
        }

        @Override
        public void setValueAt(Object aValue, int row, int column)
        {
            ScriptVariable variable = variableList.get(row);

            if (column == 0 || column == 1)
            {
                if (aValue instanceof String str)
                    aValue = Integer.parseInt(str);

                int val = (int) aValue;

                for (ScriptVariable other : variableList)
                {
                    if (variable != other && other.getVariableID() == val)
                    {
                        JOptionPane.showMessageDialog(variableTable, "The specified variable value is already in use: \"" + other.getVariableName() + "\".\nAction aborted.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                variable.setVariableID((Integer) aValue);
            }
            else
            {
                String name = (String) aValue;

                if (name.toLowerCase().startsWith("0x"))
                {
                    JOptionPane.showMessageDialog(variableTable, "You may not use a name which starts with \"0x\".\nAction aborted.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                for (ScriptVariable other : variableList)
                {
                    if (variable != other && other.getVariableName().equalsIgnoreCase(name))
                    {
                        JOptionPane.showMessageDialog(variableTable, "The specified variable name is already in use: \"" + other.getVariableName() + "\".\nAction aborted.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                variable.setVariableName(name);
            }

            postUpdateVariableTableAction();
        }

        @Override
        public String getColumnName(int column)
        {
            return headerStrings.get(column);
        }
    }

    private class VariableValueTable extends JTable
    {
        VariableValueTable()
        {
            super(new VariableValueTableModel());
            getTableHeader().setReorderingAllowed(false);
        }

        void updateRenderersAndEditors()
        {
            for (int i = 0; i < VariableValueTableModel.NUM_COLUMNS; i++)
            {
                TableColumn col = getColumnModel().getColumn(i);

                if (i == 0)
                {
                    col.setCellEditor(new NumberCellEditor(false));
                    col.setMaxWidth(50);
                }
            }
        }

        @Override
        public String getToolTipText(MouseEvent event)
        {
            int rowIdx = rowAtPoint(event.getPoint());
            int modelIdx = convertRowIndexToModel(rowIdx);

            int colIdx = convertColumnIndexToModel(columnAtPoint(event.getPoint()));

            ScriptVariable.VariableValue variableValue = variableList.get(selectedIndex).getVariableValues().get(modelIdx);
            return switch (colIdx)
            {
                case 0 -> String.valueOf(variableValue.getValue());
                case 1 -> variableValue.getValueName();
                case 2 -> variableValue.getValueDescription();
                default -> throw new RuntimeException("Error, invalid column");
            };
        }
    }

    private class VariableValueTableModel extends DefaultTableModel
    {
        List<String> headerStrings = new ArrayList<>();

        public VariableValueTableModel()
        {
            super();

            ResourceBundle bundle = ResourceBundle.getBundle("variable_tracker.gui");
            headerStrings.add(bundle.getString("VariableTracker.variableValuesTable.value"));
            headerStrings.add(bundle.getString("VariableTracker.variableValuesTable.valueName"));
            headerStrings.add(bundle.getString("VariableTracker.variableValuesTable.valueDescription"));
        }

        @Override
        public int getRowCount()
        {
            return variableList.get(selectedIndex).getVariableValues().size();
        }

        private static final int NUM_COLUMNS = 3;

        @Override
        public int getColumnCount()
        {
            return NUM_COLUMNS;
        }

        @Override
        public Object getValueAt(int row, int column)
        {
            ScriptVariable variable = variableList.get(selectedIndex);
            ScriptVariable.VariableValue variableValue = variable.getVariableValues().get(row);

            return switch (column) {
                case 0 -> variableValue.getValue();
                case 1 -> variableValue.getValueName();
                case 2 -> variableValue.getValueDescription();
                default -> throw new RuntimeException("Invalid table column index");
            };
        }

        @Override
        public void setValueAt(Object aValue, int row, int column)
        {
            ScriptVariable variable = variableList.get(selectedIndex);
            ScriptVariable.VariableValue variableValue = variable.getVariableValues().get(row);

            switch (column) {
                case 0 -> variableValue.setValue((Integer) aValue);
                case 1 -> variableValue.setValueName((String) aValue);
                case 2 -> variableValue.setValueDescription((String) aValue);
                default -> throw new RuntimeException("Invalid table column index");
            };
        }

        @Override
        public String getColumnName(int column)
        {
            return headerStrings.get(column);
        }
    }
}
