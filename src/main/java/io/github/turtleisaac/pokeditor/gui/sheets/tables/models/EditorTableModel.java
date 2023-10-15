package io.github.turtleisaac.pokeditor.gui.sheets.tables.models;

import javax.swing.table.DefaultTableModel;

public abstract class EditorTableModel extends DefaultTableModel
{
    protected Class<?>[] COLUMN_TYPES;
    protected String[] COLUMN_NAMES;

    public EditorTableModel(Class<?>[] COLUMN_TYPES, String[] COLUMN_NAMES)
    {
        super();
        this.COLUMN_TYPES = COLUMN_TYPES;
        this.COLUMN_NAMES = COLUMN_NAMES;

        for(String column_name : COLUMN_NAMES) {
            addColumn(column_name);
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
        return COLUMN_TYPES[columnIndex];
    }

    @Override
    public String getColumnName(int index) {
        return COLUMN_NAMES[index];
    }

//    @Override
//    public Object getValueAt(final int rowIndex, final int columnIndex) {
//        /*Adding components*/
//        if(COLUMN_TYPES[columnIndex] == Integer.class)
//        {
//            return rowIndex;
//        }
//        else if(COLUMN_TYPES[columnIndex] == String.class)
//        {
//            return "Text for " + rowIndex;
//        }
//        else if(COLUMN_TYPES[columnIndex] == JCheckBox.class)
//        {
//            final JCheckBox checkbox = new JCheckBox();
//            checkbox.addActionListener(new ActionListener()
//            {
//                public void actionPerformed(ActionEvent arg0)
//                {
//                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(checkbox),
//                            "Checkbox checked for row " + rowIndex);
//                }
//            });
//            return checkbox;
//        }
//        else if(COLUMN_TYPES[columnIndex] == JButton.class)
//        {
//            final JButton button = new JButton();
//            button.addActionListener(new ActionListener()
//            {
//                public void actionPerformed(ActionEvent arg0)
//                {
//                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(button),
//                            "Button clicked for row " + rowIndex);
//                }
//            });
//            return button;
//        }
//        else if(COLUMN_TYPES[columnIndex] == JComboBox.class)
//        {
//            final JComboBox<?> comboBox = new JComboBox<>();
//            comboBox.addActionListener(new ActionListener()
//            {
//                public void actionPerformed(ActionEvent arg0)
//                {
//                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(comboBox),
//                            "ComboBox clicked for row " + rowIndex);
//                }
//            });
//            return comboBox;
//        }
//        else if(COLUMN_TYPES[columnIndex] == JSpinner.class)
//        {
//            final JSpinner spinner = new JSpinner();
//            spinner.addChangeListener(new ChangeListener()
//            {
//                @Override
//                public void stateChanged(ChangeEvent e)
//                {
//                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(spinner),
//                            "Spinner adjusted for row " + rowIndex);
//                }
//
//            });
//            return spinner;
//        }
//        else
//        {
//            return "Error";
//        }
//    }
}
