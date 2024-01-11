/*
 * Created by JFormDesigner
 */

package io.github.turtleisaac.pokeditor.gui.editors.data.formats.pokemon_sprite;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.*;

import io.github.turtleisaac.nds4j.framework.GenericNtrFile;
import io.github.turtleisaac.nds4j.images.IndexedImage;
import io.github.turtleisaac.nds4j.images.Palette;
import io.github.turtleisaac.pokeditor.formats.pokemon_sprites.PokemonSpriteData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gamedata.TextFiles;
import io.github.turtleisaac.pokeditor.gui.PokeditorManager;
import io.github.turtleisaac.pokeditor.gui.editors.data.DefaultDataEditor;
import io.github.turtleisaac.pokeditor.gui.editors.data.DefaultDataEditorPanel;
import io.github.turtleisaac.pokeditor.gui.editors.data.EditorDataModel;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.CellTypes;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.FormatModel;
import net.miginfocom.swing.*;

/**
 * @author turtleisaac
 */
public class PokemonSpriteEditor extends DefaultDataEditor<PokemonSpriteData, PokemonSpriteEditor.SpriteContents>
{
    private static final int MAXIMUM_PALETTE_SIZE = 16;

//    private static final Dimension dimension = new Dimension(1060, 670);

    public PokemonSpriteEditor(List<PokemonSpriteData> data, List<TextBankData> textBankData) {
        super(new SpriteModel(data, textBankData));
//        setPreferredSize(dimension);
//        setMinimumSize(dimension);
        initComponents();
        ResourceBundle bundle = ResourceBundle.getBundle("pokeditor.sheet_panel");
        partyIconPanel.panel.multiplier = 2;
        femaleBackPanel.label.setText(bundle.getString("PokemonSpriteEditor.femaleBackPanel.text"));
        femaleBackPanel.setContents(SpriteContents.FEMALE_BACK);
        maleBackPanel.label.setText(bundle.getString("PokemonSpriteEditor.maleBackPanel.text"));
        maleBackPanel.setContents(SpriteContents.MALE_BACK);
        femaleFrontPanel.label.setText(bundle.getString("PokemonSpriteEditor.femaleFrontPanel.text"));
        femaleFrontPanel.setContents(SpriteContents.FEMALE_FRONT);
        maleFrontPanel.label.setText(bundle.getString("PokemonSpriteEditor.maleFrontPanel.text"));
        maleFrontPanel.setContents(SpriteContents.MALE_FRONT);
        partyIconPanel.label.setText(bundle.getString("PokemonSpriteEditor.partyIconPanel.text"));
        partyIconPanel.setContents(SpriteContents.PARTY_ICON);
    }

    @Override
    public void selectedIndexedChanged(int idx, ActionEvent e)
    {
        super.selectedIndexedChanged(idx, e);
        EditorDataModel<SpriteContents> model = getModel();
        femaleBackPanel.setImage((IndexedImage) model.getValueFor(idx, SpriteContents.FEMALE_BACK));
        maleBackPanel.setImage((IndexedImage) model.getValueFor(idx, SpriteContents.MALE_BACK));
        femaleFrontPanel.setImage((IndexedImage) model.getValueFor(idx, SpriteContents.FEMALE_FRONT));
        maleFrontPanel.setImage((IndexedImage) model.getValueFor(idx, SpriteContents.MALE_FRONT));
        partyIconPanel.setImage((IndexedImage) model.getValueFor(idx, SpriteContents.PARTY_ICON));
        palettePanel.setPalette((Palette) model.getValueFor(idx, SpriteContents.PALETTE));
        shinyPalettePanel.setPalette((Palette) model.getValueFor(idx, SpriteContents.SHINY_PALETTE));

        globalFrontYSpinner.setValue(model.getValueFor(idx, SpriteContents.GLOBAL_FRONT_Y));
        femaleBackYSpinner.setValue(model.getValueFor(idx, SpriteContents.FEMALE_BACK_Y));
        maleBackYSpinner.setValue(model.getValueFor(idx, SpriteContents.MALE_BACK_Y));
        femaleFrontYSpinner.setValue(model.getValueFor(idx, SpriteContents.FEMALE_FRONT_Y));
        maleFrontYSpinner.setValue(model.getValueFor(idx, SpriteContents.MALE_FRONT_Y));
        movementSpinner.setValue(model.getValueFor(idx, SpriteContents.MOVEMENT));
        shadowXSpinner.setValue(model.getValueFor(idx, SpriteContents.SHADOW_X));
        shadowSizeComboBox.setSelectedIndex((Integer) model.getValueFor(idx, SpriteContents.SHADOW_SIZE));

        int paletteIdx = (int) model.getValueFor(idx, SpriteContents.PARTY_ICON_PALETTE);
        partyIconPaletteSpinner.setValue(paletteIdx);
        partyIconPanel.panel.image.setPaletteIdx(paletteIdx);

        tabbedPane1.setSelectedIndex(0);
        tabbedPane1.setSelectedIndex(1);
        tabbedPane1.setSelectedIndex(0);
        partyIconPanel.repaint();
        repaint();
    }

//    @Override
//    public void addNewEntry()
//    {
//
//    }
//
//    @Override
//    public void deleteCurrentEntry()
//    {
//
//    }

    @Override
    public Class<PokemonSpriteData> getDataClass()
    {
        return PokemonSpriteData.class;
    }

    @Override
    public Set<DefaultDataEditorPanel.DataEditorButtons> getEnabledToolbarButtons()
    {
        return Set.of(DefaultDataEditorPanel.DataEditorButtons.ADD_ENTRY,
                DefaultDataEditorPanel.DataEditorButtons.DELETE_ENTRY,
                DefaultDataEditorPanel.DataEditorButtons.COPY_ENTRY,
                DefaultDataEditorPanel.DataEditorButtons.PASTE_ENTRY
        );
    }

    private void thisComponentResized(ComponentEvent e) {
//        System.out.println("moo");
//        System.out.println(panel1.getMaximumSize().toString());
//        panel1.setSize();
    }

    private void tabbedPane1StateChanged(ChangeEvent e) {
        Palette palette = tabbedPane1.getSelectedComponent().equals(palettePanel) ? palettePanel.palette : shinyPalettePanel.palette;
        if (palette != null)
        {
            femaleBackPanel.setPalette(palette);
            maleBackPanel.setPalette(palette);
            femaleFrontPanel.setPalette(palette);
            maleFrontPanel.setPalette(palette);
        }
        battleMockupPanel1.repaint();
    }

    private void toggleGenderButtonPressed(ActionEvent e) {
        battleMockupPanel1.repaint();
    }

    private void toggleFrameButtonPressed(ActionEvent e) {
        battleMockupPanel1.repaint();
    }

    private void spriteMetadataSpinnerStateChanged(ChangeEvent e) {
        Object source = e.getSource();
        if (source.equals(globalFrontYSpinner))
        {
            getModel().setValueFor(globalFrontYSpinner.getValue(), getSelectedIndex(), SpriteContents.GLOBAL_FRONT_Y);
        }
        else if (source.equals(femaleBackYSpinner))
        {
            getModel().setValueFor(femaleBackYSpinner.getValue(), getSelectedIndex(), SpriteContents.FEMALE_BACK_Y);
        }
        else if (source.equals(maleBackYSpinner))
        {
            getModel().setValueFor(maleBackYSpinner.getValue(), getSelectedIndex(), SpriteContents.MALE_BACK_Y);
        }
        else if (source.equals(femaleFrontYSpinner))
        {
            getModel().setValueFor(femaleFrontYSpinner.getValue(), getSelectedIndex(), SpriteContents.FEMALE_FRONT_Y);
        }
        else if (source.equals(maleFrontYSpinner))
        {
            getModel().setValueFor(maleFrontYSpinner.getValue(), getSelectedIndex(), SpriteContents.MALE_FRONT_Y);
        }
        else if (source.equals(movementSpinner))
        {
            getModel().setValueFor(movementSpinner.getValue(), getSelectedIndex(), SpriteContents.MOVEMENT);
        }
        else if (source.equals(shadowXSpinner))
        {
            getModel().setValueFor(shadowXSpinner.getValue(), getSelectedIndex(), SpriteContents.SHADOW_X);
        }
        else if (source.equals(partyIconPaletteSpinner))
        {
            if (partyIconPanel.panel.image != null)
            {
                partyIconPanel.panel.image.setPaletteIdx((Integer) partyIconPaletteSpinner.getValue());
                partyIconPanel.repaint();
            }
            getModel().setValueFor(partyIconPaletteSpinner.getValue(), getSelectedIndex(), SpriteContents.PARTY_ICON_PALETTE);
        }
        battleMockupPanel1.repaint();
    }

    private void shadowSizeChanged(ActionEvent e) {
        getModel().setValueFor(shadowSizeComboBox.getSelectedIndex(), getSelectedIndex(), SpriteContents.SHADOW_SIZE);
        battleMockupPanel1.repaint();
    }

    private void importSprite(PokemonSpriteDisplayPanel panel)
    {
        GenericNtrFile result = PokeditorManager.readIndexedImage(panel.contents != SpriteContents.PARTY_ICON);
        if (result == null) {
            JOptionPane.showMessageDialog(this, "An error occurred while reading the provided file.\nAction has been aborted.");
            return;
        }

        if (result instanceof IndexedImage image)
        {
            if (image.getWidth() != panel.expectedWidth || image.getHeight() != panel.expectedHeight) {
                JOptionPane.showMessageDialog(this,
                        String.format("Expected image width is %d wide by %d tall. Provided is %d by %d.\nAction has been aborted.",
                                panel.expectedWidth,
                                panel.expectedHeight,
                                image.getWidth(),
                                image.getHeight()),
                        "Size error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (panel.panel.image != null && !image.getPalette().equals(Palette.defaultPalette)) {
                if (image.getPalette().getColors().length > MAXIMUM_PALETTE_SIZE) {
                    JOptionPane.showMessageDialog(this,
                            String.format("The provided image's palette needs to have at most %d colors. This image's palette has %d.\nAction has been aborted.", MAXIMUM_PALETTE_SIZE, image.getPalette().getNumColors()),
                            "Palette Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (panel.buttonStyle == PokemonSpriteDisplayPanel.HORIZONTAL && !image.getPalette().equals(panel.panel.image.getPalette())) {
                    int confirmResult = JOptionPane.showConfirmDialog(this, "The provided image has a palette which differs from that of the current image. Would you like to overwrite?", "Palette Conflict", JOptionPane.YES_NO_CANCEL_OPTION);

                    switch (confirmResult) {
                        case JOptionPane.CANCEL_OPTION -> {
                            JOptionPane.showMessageDialog(this, "Action has been aborted", "Abort", JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }
                        case JOptionPane.NO_OPTION -> {
                            image.setPalette(panel.panel.image.getPalette());
                        }
                    }
                }
            }

            if (!image.getPalette().equals(Palette.defaultPalette))
            {
                if (tabbedPane1.getSelectedComponent().equals(palettePanel))
                {
                    palettePanel.setPalette(image.getPalette());
                    getModel().setValueFor(image.getPalette(), getSelectedIndex(), SpriteContents.PALETTE);
                }
                else
                {
                    shinyPalettePanel.setPalette(image.getPalette());
                    getModel().setValueFor(image.getPalette(), getSelectedIndex(), SpriteContents.SHINY_PALETTE);
                }
            }

            panel.panel.image.setPixels(image.getPixels());
            getModel().setValueFor(panel.panel.image, getSelectedIndex(), panel.contents);
            panel.repaint();
            tabbedPane1StateChanged(null);
        }
        else if (result instanceof Palette palette)
        {
            if (panel.panel.image != null) {
                if (palette.getColors().length > MAXIMUM_PALETTE_SIZE) {
                    JOptionPane.showMessageDialog(this,
                            String.format("The provided palette needs to have at most %d colors. This image's palette has %d.\nAction has been aborted.", MAXIMUM_PALETTE_SIZE, palette.getNumColors()),
                            "Palette Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (tabbedPane1.getSelectedComponent().equals(palettePanel))
                {
                    palettePanel.setPalette(palette);
                    getModel().setValueFor(palette, getSelectedIndex(), SpriteContents.PALETTE);
                }
                else
                {
                    shinyPalettePanel.setPalette(palette);
                    getModel().setValueFor(palette, getSelectedIndex(), SpriteContents.SHINY_PALETTE);
                }
                tabbedPane1StateChanged(null);
            }
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner non-commercial license
        ResourceBundle bundle = ResourceBundle.getBundle("pokeditor.sheet_panel");
        femaleBackPanel = new PokemonSpriteDisplayPanel(PokemonSpriteDisplayPanel.HORIZONTAL);
        maleBackPanel = new PokemonSpriteDisplayPanel(PokemonSpriteDisplayPanel.HORIZONTAL);
        separator1 = new JSeparator();
        panel1 = new JPanel();
        battleMockupPanel1 = new PokemonSpriteEditor.BattleMockupPanel();
        toggleGenderButton = new JToggleButton();
        toggleFrameButton = new JToggleButton();
        panel2 = new JPanel();
        globalFrontYLabel = new JLabel();
        globalFrontYSpinner = new JSpinner();
        separator4 = new JSeparator();
        femaleBackYLabel = new JLabel();
        femaleBackYSpinner = new JSpinner();
        femaleFrontYLabel = new JLabel();
        femaleFrontYSpinner = new JSpinner();
        separator5 = new JSeparator();
        maleBackYLabel = new JLabel();
        maleBackYSpinner = new JSpinner();
        maleFrontYLabel = new JLabel();
        maleFrontYSpinner = new JSpinner();
        separator3 = new JSeparator();
        movementLabel = new JLabel();
        movementSpinner = new JSpinner();
        separator2 = new JSeparator();
        shadowXLabel = new JLabel();
        shadowXSpinner = new JSpinner();
        shadowSizeLabel = new JLabel();
        shadowSizeComboBox = new JComboBox<>();
        separator6 = new JSeparator();
        partyIconGroupPanel = new JPanel();
        partyIconPanel = new PokemonSpriteDisplayPanel(PokemonSpriteDisplayPanel.VERTICAL);
        separator7 = new JSeparator();
        partyIconPaletteIdLabel = new JLabel();
        partyIconPaletteSpinner = new JSpinner();
        femaleFrontPanel = new PokemonSpriteDisplayPanel(PokemonSpriteDisplayPanel.HORIZONTAL);
        maleFrontPanel = new PokemonSpriteDisplayPanel(PokemonSpriteDisplayPanel.HORIZONTAL);
        tabbedPane1 = new JTabbedPane();
        palettePanel = new PokemonSpriteEditor.PalettePanel();
        shinyPalettePanel = new PokemonSpriteEditor.PalettePanel();

        //======== this ========
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                thisComponentResized(e);
            }
        });
        setLayout(new MigLayout(
            "insets 0 null 0 null,hidemode 3",
            // columns
            "[left]" +
            "[left]para" +
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]",
            // rows
            "[]" +
            "[]unrel" +
            "[]"));

        //---- femaleBackPanel ----
        femaleBackPanel.setMinimumSize(new Dimension(320, 160));
        femaleBackPanel.setBorder(null);
        add(femaleBackPanel, "cell 0 0,alignx left,growx 0");

        //---- maleBackPanel ----
        maleBackPanel.setMinimumSize(new Dimension(320, 160));
        add(maleBackPanel, "cell 1 0");

        //---- separator1 ----
        separator1.setOrientation(SwingConstants.VERTICAL);
        add(separator1, "cell 2 0 1 3,grow");

        //======== panel1 ========
        {
            panel1.setLayout(new MigLayout(
                "hidemode 3",
                // columns
                "[fill]",
                // rows
                "[]0" +
                "[]0" +
                "[grow]"));
            panel1.add(battleMockupPanel1, "cell 0 0");

            //---- toggleGenderButton ----
            toggleGenderButton.setText(bundle.getString("PokemonSpriteEditor.toggleGenderButton.text"));
            toggleGenderButton.addActionListener(e -> toggleGenderButtonPressed(e));
            panel1.add(toggleGenderButton, "cell 0 1");

            //---- toggleFrameButton ----
            toggleFrameButton.setText(bundle.getString("PokemonSpriteEditor.toggleFrameButton.text"));
            toggleFrameButton.addActionListener(e -> toggleFrameButtonPressed(e));
            panel1.add(toggleFrameButton, "cell 0 1");

            //======== panel2 ========
            {
                panel2.setLayout(new MigLayout(
                    "hidemode 3",
                    // columns
                    "[fill]" +
                    "[grow,fill]",
                    // rows
                    "[]0" +
                    "[]0" +
                    "[]0" +
                    "[]0" +
                    "[]0" +
                    "[]0" +
                    "[]0" +
                    "[]0" +
                    "[]0" +
                    "[]0" +
                    "[]0" +
                    "[]"));

                //---- globalFrontYLabel ----
                globalFrontYLabel.setText(bundle.getString("PokemonSpriteEditor.globalFrontYLabel.text"));
                panel2.add(globalFrontYLabel, "cell 0 0");

                //---- globalFrontYSpinner ----
                globalFrontYSpinner.setModel(new SpinnerNumberModel(0, -128, 127, 1));
                globalFrontYSpinner.addChangeListener(e -> spriteMetadataSpinnerStateChanged(e));
                panel2.add(globalFrontYSpinner, "cell 1 0,growx");
                panel2.add(separator4, "cell 0 1 2 1");

                //---- femaleBackYLabel ----
                femaleBackYLabel.setText(bundle.getString("PokemonSpriteEditor.femaleBackYLabel.text"));
                panel2.add(femaleBackYLabel, "cell 0 2");

                //---- femaleBackYSpinner ----
                femaleBackYSpinner.setModel(new SpinnerNumberModel(0, -255, 0, 1));
                femaleBackYSpinner.addChangeListener(e -> spriteMetadataSpinnerStateChanged(e));
                panel2.add(femaleBackYSpinner, "cell 1 2,growx");

                //---- femaleFrontYLabel ----
                femaleFrontYLabel.setText(bundle.getString("PokemonSpriteEditor.femaleFrontYLabel.text"));
                panel2.add(femaleFrontYLabel, "cell 0 3");

                //---- femaleFrontYSpinner ----
                femaleFrontYSpinner.setModel(new SpinnerNumberModel(0, -255, 0, 1));
                femaleFrontYSpinner.addChangeListener(e -> spriteMetadataSpinnerStateChanged(e));
                panel2.add(femaleFrontYSpinner, "cell 1 3,growx");
                panel2.add(separator5, "cell 0 4 2 1");

                //---- maleBackYLabel ----
                maleBackYLabel.setText(bundle.getString("PokemonSpriteEditor.maleBackYLabel.text"));
                panel2.add(maleBackYLabel, "cell 0 5");

                //---- maleBackYSpinner ----
                maleBackYSpinner.setModel(new SpinnerNumberModel(0, -255, 0, 1));
                maleBackYSpinner.addChangeListener(e -> spriteMetadataSpinnerStateChanged(e));
                panel2.add(maleBackYSpinner, "cell 1 5,growx");

                //---- maleFrontYLabel ----
                maleFrontYLabel.setText(bundle.getString("PokemonSpriteEditor.maleFrontYLabel.text"));
                panel2.add(maleFrontYLabel, "cell 0 6");

                //---- maleFrontYSpinner ----
                maleFrontYSpinner.setModel(new SpinnerNumberModel(0, -255, 0, 1));
                maleFrontYSpinner.addChangeListener(e -> spriteMetadataSpinnerStateChanged(e));
                panel2.add(maleFrontYSpinner, "cell 1 6,growx");
                panel2.add(separator3, "cell 0 7 2 1");

                //---- movementLabel ----
                movementLabel.setText(bundle.getString("PokemonSpriteEditor.movementLabel.text"));
                panel2.add(movementLabel, "cell 0 8");

                //---- movementSpinner ----
                movementSpinner.setModel(new SpinnerNumberModel(0, 0, 255, 1));
                movementSpinner.addChangeListener(e -> spriteMetadataSpinnerStateChanged(e));
                panel2.add(movementSpinner, "cell 1 8,growx");
                panel2.add(separator2, "cell 0 9 2 1,growx");

                //---- shadowXLabel ----
                shadowXLabel.setText(bundle.getString("PokemonSpriteEditor.shadowXLabel.text"));
                panel2.add(shadowXLabel, "cell 0 10");

                //---- shadowXSpinner ----
                shadowXSpinner.setModel(new SpinnerNumberModel(0, -128, 127, 1));
                shadowXSpinner.addChangeListener(e -> spriteMetadataSpinnerStateChanged(e));
                panel2.add(shadowXSpinner, "cell 1 10,growx");

                //---- shadowSizeLabel ----
                shadowSizeLabel.setText(bundle.getString("PokemonSpriteEditor.shadowSizeLabel.text"));
                panel2.add(shadowSizeLabel, "cell 0 11");

                //---- shadowSizeComboBox ----
                shadowSizeComboBox.setModel(new DefaultComboBoxModel<>(new String[] {
                    "None",
                    "Small",
                    "Medium",
                    "Large"
                }));
                shadowSizeComboBox.addActionListener(e -> shadowSizeChanged(e));
                panel2.add(shadowSizeComboBox, "cell 1 11");
            }
            panel1.add(panel2, "cell 0 2,aligny top,grow 100 0");
        }
        add(panel1, "cell 3 0 1 3,alignx left,grow 0 100");

        //---- separator6 ----
        separator6.setOrientation(SwingConstants.VERTICAL);
        add(separator6, "cell 4 0 1 3,grow");

        //======== partyIconGroupPanel ========
        {
            partyIconGroupPanel.setLayout(new MigLayout(
                "insets 0,hidemode 3",
                // columns
                "[fill]",
                // rows
                "[]" +
                "[]unrel" +
                "[]" +
                "[]" +
                "[]" +
                "[]"));
            partyIconGroupPanel.add(partyIconPanel, "cell 0 0 1 3");
            partyIconGroupPanel.add(separator7, "cell 0 3");

            //---- partyIconPaletteIdLabel ----
            partyIconPaletteIdLabel.setText(bundle.getString("PokemonSpriteEditor.partyIconPaletteIdLabel.text"));
            partyIconPaletteIdLabel.setHorizontalAlignment(SwingConstants.CENTER);
            partyIconGroupPanel.add(partyIconPaletteIdLabel, "cell 0 4,alignx center,growx 0");

            //---- partyIconPaletteSpinner ----
            partyIconPaletteSpinner.setModel(new SpinnerNumberModel(0, 0, null, 1));
            partyIconPaletteSpinner.addChangeListener(e -> spriteMetadataSpinnerStateChanged(e));
            partyIconGroupPanel.add(partyIconPaletteSpinner, "cell 0 5");
        }
        add(partyIconGroupPanel, "cell 5 0 1 3");

        //---- femaleFrontPanel ----
        femaleFrontPanel.setMinimumSize(new Dimension(320, 160));
        add(femaleFrontPanel, "cell 0 1,alignx left,growx 0");

        //---- maleFrontPanel ----
        maleFrontPanel.setMinimumSize(new Dimension(320, 160));
        add(maleFrontPanel, "cell 1 1");

        //======== tabbedPane1 ========
        {
            tabbedPane1.setTabPlacement(SwingConstants.LEFT);
            tabbedPane1.addChangeListener(e -> tabbedPane1StateChanged(e));
            tabbedPane1.addTab(bundle.getString("PokemonSpriteEditor.palettePanel.tab.title"), palettePanel);
            tabbedPane1.addTab(bundle.getString("PokemonSpriteEditor.shinyPalettePanel.tab.title"), shinyPalettePanel);
        }
        add(tabbedPane1, "cell 0 2 2 1,aligny top,grow 100 0");
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner non-commercial license
    private PokemonSpriteEditor.PokemonSpriteDisplayPanel femaleBackPanel;
    private PokemonSpriteEditor.PokemonSpriteDisplayPanel maleBackPanel;
    private JSeparator separator1;
    private JPanel panel1;
    private PokemonSpriteEditor.BattleMockupPanel battleMockupPanel1;
    private JToggleButton toggleGenderButton;
    private JToggleButton toggleFrameButton;
    private JPanel panel2;
    private JLabel globalFrontYLabel;
    private JSpinner globalFrontYSpinner;
    private JSeparator separator4;
    private JLabel femaleBackYLabel;
    private JSpinner femaleBackYSpinner;
    private JLabel femaleFrontYLabel;
    private JSpinner femaleFrontYSpinner;
    private JSeparator separator5;
    private JLabel maleBackYLabel;
    private JSpinner maleBackYSpinner;
    private JLabel maleFrontYLabel;
    private JSpinner maleFrontYSpinner;
    private JSeparator separator3;
    private JLabel movementLabel;
    private JSpinner movementSpinner;
    private JSeparator separator2;
    private JLabel shadowXLabel;
    private JSpinner shadowXSpinner;
    private JLabel shadowSizeLabel;
    private JComboBox<String> shadowSizeComboBox;
    private JSeparator separator6;
    private JPanel partyIconGroupPanel;
    private PokemonSpriteEditor.PokemonSpriteDisplayPanel partyIconPanel;
    private JSeparator separator7;
    private JLabel partyIconPaletteIdLabel;
    private JSpinner partyIconPaletteSpinner;
    private PokemonSpriteEditor.PokemonSpriteDisplayPanel femaleFrontPanel;
    private PokemonSpriteEditor.PokemonSpriteDisplayPanel maleFrontPanel;
    private JTabbedPane tabbedPane1;
    private PokemonSpriteEditor.PalettePanel palettePanel;
    private PokemonSpriteEditor.PalettePanel shinyPalettePanel;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    class PokemonSpriteDisplayPanel extends JPanel
    {
        PokemonSpriteDisplaySubPanel panel = new PokemonSpriteDisplaySubPanel();
        JButton exportButton;
        JButton importButton;
        JButton swapButton;
        JLabel label;

        public static final int HORIZONTAL = 0;
        public static final int VERTICAL = 1;

        private int expectedWidth;
        private int expectedHeight;

        private final int buttonStyle;

        private SpriteContents contents;

        public PokemonSpriteDisplayPanel()
        {
            buttonStyle = HORIZONTAL;
            setup(HORIZONTAL);
        }

        public PokemonSpriteDisplayPanel(int buttonStyle)
        {
            this.buttonStyle = buttonStyle;
            setup(buttonStyle);
        }

        private void setup(int buttonStyle)
        {
            ResourceBundle bundle = ResourceBundle.getBundle("pokeditor.sheet_panel");
            setLayout(new MigLayout(
                    "ins 2",
                    // columns
                    "[center,fill]",
                    // rows
                    "[]" +
                            "[]"));

            label = new JLabel("Female Front");
            label.setHorizontalAlignment(JLabel.CENTER);
            exportButton = new JButton(bundle.getString("PokemonSpriteEditor.exportButton.text"));
            importButton = new JButton(bundle.getString("PokemonSpriteEditor.importButton.text"));
            swapButton = new JButton(bundle.getString("PokemonSpriteEditor.swapFramesButton.text"));

            add(label, "cell 0 0,grow");
            add(panel, "cell 0 1,grow");
            if (buttonStyle == HORIZONTAL)
            {
                expectedWidth = PokemonSpriteData.BATTLE_SPRITE_WIDTH*2;
                expectedHeight = PokemonSpriteData.BATTLE_SPRITE_HEIGHT;

                add(exportButton, "cell 0 2,grow");
                add(importButton, "cell 0 2,grow");
                add(swapButton, "cell 0 2,grow");
            }
            else
            {
                expectedWidth = PokemonSpriteData.PARTY_ICON_WIDTH;
                expectedHeight = PokemonSpriteData.PARTY_ICON_HEIGHT*2;

                add(exportButton, "cell 0 2,grow");
                add(importButton, "cell 0 3,grow");
            }


            exportButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    if (panel.image != null)
                        PokeditorManager.writeIndexedImage(panel.image);
                }
            });

            importButton.addActionListener(e -> importSprite(this));

            swapButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    try {
                        IndexedImage left = panel.image.getSubImage(0, 0, PokemonSpriteData.BATTLE_SPRITE_WIDTH, PokemonSpriteData.BATTLE_SPRITE_HEIGHT);
                        IndexedImage right = panel.image.getSubImage(PokemonSpriteData.BATTLE_SPRITE_WIDTH, 0, PokemonSpriteData.BATTLE_SPRITE_WIDTH, PokemonSpriteData.BATTLE_SPRITE_HEIGHT);
                        IndexedImage result = IndexedImage.getHorizontalCompositeImage(right, left);

                        panel.image.setPixels(result.getPixels());
                        getModel().setValueFor(panel.image, getSelectedIndex(), contents);
                        panel.repaint();
                        battleMockupPanel1.repaint();
                    }
                    catch (IndexedImage.ImageException exception)
                    {
                        exception.printStackTrace();
                        return;
                    }

                }
            });
        }

        public void setImage(IndexedImage image)
        {
            if (image == null) {
                setImage(new IndexedImage(80, 160, 4, Palette.defaultPalette));
                setEnabled(false);
            }
            else {
                setEnabled(true);
                panel.setImage(image);
                setMaximumSize(getPreferredSize());
            }
            repaint();
        }

        public void setPalette(Palette palette)
        {
            if (panel.image != null)
            {
                panel.image.setPalette(palette);
                repaint();
            }

        }

        @Override
        public void setEnabled(boolean enabled)
        {
//            super.setEnabled(enabled);
            exportButton.setEnabled(enabled);
            swapButton.setEnabled(enabled);
        }

        public void setContents(SpriteContents contents)
        {
            this.contents = contents;
        }

        static class PokemonSpriteDisplaySubPanel extends JPanel {
            private double multiplier = 1.5;
            IndexedImage image;

            private void setImage(IndexedImage image)
            {
                this.image = image;
                Dimension d = new Dimension((int) (image.getWidth()*multiplier), (int) (image.getHeight()*multiplier));
                setMinimumSize(d);
                setPreferredSize(d);
                setMaximumSize(d);
            }

            @Override
            public void paint(Graphics g)
            {
                super.paint(g);
                if (image != null)
                {
                    g.drawImage(image.getImage(), 0, 0, (int) (image.getWidth()*multiplier), (int) (image.getHeight()*multiplier), null);
                }
            }
        }
    }

    class BattleMockupPanel extends JPanel
    {
        private static final int spriteSize = 80;

        static Image textBar = new ImageIcon(BattleMockupPanel.class.getResource("/pokeditor/icons/text_bar.png")).getImage();
        static Image background = new ImageIcon(BattleMockupPanel.class.getResource("/pokeditor/icons/background.png")).getImage();
        static Image playerPlatform = new ImageIcon(BattleMockupPanel.class.getResource("/pokeditor/icons/platform_you.png")).getImage();
        static Image enemyPlatform = new ImageIcon(BattleMockupPanel.class.getResource("/pokeditor/icons/platform_opponent.png")).getImage();
        static Image playerHealth = new ImageIcon(BattleMockupPanel.class.getResource("/pokeditor/icons/health_you.png")).getImage();
        static Image enemyHealth = new ImageIcon(BattleMockupPanel.class.getResource("/pokeditor/icons/health_opponent.png")).getImage();
        static Image maleIcon = new ImageIcon(BattleMockupPanel.class.getResource("/pokeditor/icons/symbol_male.png")).getImage();
        static Image femaleIcon = new ImageIcon(BattleMockupPanel.class.getResource("/pokeditor/icons/symbol_female.png")).getImage();

        static Image smallShadow = new ImageIcon(BattleMockupPanel.class.getResource("/pokeditor/icons/shadow_small.png")).getImage();
        static Image mediumShadow = new ImageIcon(BattleMockupPanel.class.getResource("/pokeditor/icons/shadow_medium.png")).getImage();
        static Image largeShadow = new ImageIcon(BattleMockupPanel.class.getResource("/pokeditor/icons/shadow_large.png")).getImage();

        public BattleMockupPanel()
        {
            super();

            Dimension dimension= new Dimension(256,192);
            setPreferredSize(dimension);
            setMinimumSize(dimension);
            setMaximumSize(dimension);
            setVisible(true);
        }

        @Override
        public void paint(Graphics g)
        {
            try
            {
                //The origin (top left) is 0, 0
                BufferedImage image = new BufferedImage(256, 192, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = image.createGraphics();
                g2d.drawImage(background,0,0, null);
                g2d.drawImage(enemyPlatform,129,72,null);
                g2d.drawImage(playerPlatform,-42,122,null);

                ShadowSize shadowSize = ShadowSize.getSize(shadowSizeComboBox.getSelectedIndex());
                if(shadowSize == ShadowSize.SMALL)
                {
                    g2d.drawImage(smallShadow,179 + (Integer) shadowXSpinner.getValue(), 83, null);
                }
                else if(shadowSize == ShadowSize.MEDIUM)
                {
                    g2d.drawImage(mediumShadow,174 + (Integer) shadowXSpinner.getValue(), 83, null);
                }
                else if(shadowSize == ShadowSize.LARGE)
                {
                    g2d.drawImage(largeShadow,167 + (Integer) shadowXSpinner.getValue(), 82, null);
                }

                Image frontSprite = null;
                Image backSprite = null;
                IndexedImage front;
                IndexedImage back;

                boolean isFemale = toggleGenderButton.isSelected();

                if (isFemale)
                {
                    front = femaleFrontPanel.panel.image;
                    back = femaleBackPanel.panel.image;
                }
                else
                {
                    front = maleFrontPanel.panel.image;
                    back = maleBackPanel.panel.image;
                }

                int startCoordinateX = toggleFrameButton.isSelected() ? spriteSize : 0;

                if (front != null)
                    frontSprite = front.getSubImage(startCoordinateX, 0, spriteSize, spriteSize).getTransparentImage();
                if (back != null)
                    backSprite = back.getSubImage(startCoordinateX, 0, spriteSize, spriteSize).getTransparentImage();

                int frontModifier = (int) (isFemale ? femaleFrontYSpinner.getValue() : maleFrontYSpinner.getValue());
                int backModifier = (int) (isFemale ? femaleBackYSpinner.getValue() : maleBackYSpinner.getValue());

                if (frontSprite != null)
                    g2d.drawImage(frontSprite,152,10 - (Integer) globalFrontYSpinner.getValue() - frontModifier,null);
                if (backSprite != null)
                    g2d.drawImage(backSprite,23,72 - backModifier,null);

                g2d.drawImage(playerHealth,129,95, null);
                g2d.drawImage(enemyHealth,0,18, null);

                Image genderIcon = isFemale ? femaleIcon : maleIcon;
                g2d.drawImage(genderIcon,217,103,null);
                g2d.drawImage(genderIcon,65,25, null);

                g2d.drawImage(textBar,0,getHeight()-48, null);
                g2d.dispose();

                g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
            }
            catch(IndexedImage.ImageException e)
            {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "A fatal image-parsing error has occurred while attempting to display the in-battle sprite preview. Check the command-line for details.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        }
    }

    class PalettePanel extends JPanel
    {
        private Palette palette;

        private JButton button0 = new JButton();
        private JButton button1 = new JButton();
        private JButton button2 = new JButton();
        private JButton button3 = new JButton();
        private JButton button4 = new JButton();
        private JButton button5 = new JButton();
        private JButton button6 = new JButton();
        private JButton button7 = new JButton();
        private JButton button8 = new JButton();
        private JButton button9 = new JButton();
        private JButton button10 = new JButton();
        private JButton button11 = new JButton();
        private JButton button12 = new JButton();
        private JButton button13 = new JButton();
        private JButton button14 = new JButton();
        private JButton button15 = new JButton();

        private final JButton[] buttons = new JButton[] {button0, button1, button2, button3, button4, button5, button6, button7, button8, button9, button10, button11, button12, button13, button14, button15};

        private final PaletteEditingSpritePanel spritePanel = new PaletteEditingSpritePanel();
        private int selectedColorIdx = -1;

        public PalettePanel()
        {
            setLayout(new MigLayout(
                    "insets 0,hidemode 3",
                    // columns
                    "[fill]" +
                            "[fill]" +
                            "[fill]" +
                            "[fill]",
                    // rows
                    "[fill]" +
                            "[]" +
                            "[]" +
                            "[]" +
                            "[]"));

            for (int row = 0; row < 4; row++)
            {
                for (int col = 0; col < 4; col++)
                {
                    JButton button = buttons[row*4 + col];
                    button.setText("" + (row*4 + col));
                    add(button, String.format("cell %d %d", col, row));
                }
            }
            add(new JButton("Set to Shiny"), "cell 0 4 4 1");
            add(new JButton("Swap with Shiny"), "cell 0 4 4 1");

            for (JButton button : buttons) {
                button.addActionListener(this::colorChangeRequested);
            }
        }

        public void setPalette(Palette palette)
        {
            this.palette = palette;
            int i = 0;
            for (JButton button : buttons)
            {
                Color c = palette.getColor(i++);
                button.setBackground(c);
            }
        }

        private void colorChangeRequested(ActionEvent e)
        {
            selectedColorIdx = -1;
            for (JButton button : buttons)
            {
                selectedColorIdx++;
                if (button.equals(e.getSource()))
                    break;
            }

            if (selectedColorIdx == -1)
                return;

            spritePanel.setPalette(palette);
            spritePanel.button.setSelected(false);

            JColorChooser colorChooser = new JColorChooser(palette.getColor(selectedColorIdx));
            ColorSelectionModel colorModel = colorChooser.getSelectionModel();
            colorModel.addChangeListener(this::roundAndUpdateColor);

            try {
                spritePanel.setBackImage(femaleBackPanel.panel.image);
                spritePanel.setFrontImage(femaleFrontPanel.panel.image);
            }
            catch (IndexedImage.ImageException ex) {
                ex.printStackTrace();
                return;
            }

            colorChooser.setPreviewPanel(spritePanel);
            tabbedPane1StateChanged(null);
            JDialog dialog = JColorChooser.createDialog(tabbedPane1, getModel().getEntryName(getSelectedIndex()) + " - Color #" + selectedColorIdx, true, colorChooser, this::finalizeColorChange, this::cancelColorChange);
            dialog.setVisible(true);
            //todo create new JColorChooser and dialog
        }

        private void finalizeColorChange(ActionEvent e)
        {
            Color newColor = spritePanel.paletteCopy.getColor(selectedColorIdx);
            palette.setColor(selectedColorIdx, newColor);
            buttons[selectedColorIdx].setBackground(newColor);
            repaint();
            tabbedPane1StateChanged(null);
        }

        private void cancelColorChange(ActionEvent e)
        {
            tabbedPane1StateChanged(null);
        }

        private void roundAndUpdateColor(ChangeEvent e)
        {
            ColorSelectionModel model1 = (ColorSelectionModel) e.getSource();
            Color newColor = model1.getSelectedColor();
            int r = newColor.getRed() - (newColor.getRed() % 8);
            r = Math.min(r,248);

            int g = newColor.getGreen() - (newColor.getGreen() % 8);
            g = Math.min(g,248);

            int b = newColor.getBlue() - (newColor.getBlue() % 8);
            b = Math.min(b,248);
            newColor = new Color(r,g,b);

            spritePanel.paletteCopy.setColor(selectedColorIdx, newColor);
            spritePanel.frontPanel.image.setPalette(spritePanel.paletteCopy);
            spritePanel.backPanel.image.setPalette(spritePanel.paletteCopy);
            spritePanel.repaint();
        }

        class PaletteEditingSpritePanel extends JPanel
        {
            private final PokemonSpriteDisplayPanel.PokemonSpriteDisplaySubPanel backPanel = new PokemonSpriteDisplayPanel.PokemonSpriteDisplaySubPanel();
            private final PokemonSpriteDisplayPanel.PokemonSpriteDisplaySubPanel frontPanel = new PokemonSpriteDisplayPanel.PokemonSpriteDisplaySubPanel();
            private final JToggleButton button;
            private Palette paletteCopy;

            PaletteEditingSpritePanel()
            {
                super();
                setLayout(new MigLayout());
                add(backPanel, "cell 0 0");
                add(frontPanel, "cell 1 0");

                this.button = new JToggleButton(ResourceBundle.getBundle("pokeditor.sheet_panel").getString("PokemonSpriteEditor.toggleGenderButton.text"));
                button.addActionListener(e -> {
                    IndexedImage front = button.isSelected() ? maleFrontPanel.panel.image : femaleFrontPanel.panel.image;
                    IndexedImage back = button.isSelected() ? maleBackPanel.panel.image : femaleBackPanel.panel.image;
                    try {
                        setFrontImage(front);
                        setBackImage(back);
                        repaint();
                    }
                    catch (IndexedImage.ImageException ignored) {}

                });
                add(button, "cell 2 0");
            }

            void setPalette(Palette palette)
            {
                paletteCopy = palette.copyOf();
            }

            void setFrontImage(IndexedImage image) throws IndexedImage.ImageException
            {
                image.setPalette(paletteCopy);
                frontPanel.setImage(image.getSubImage(0, 0, image.getWidth(), image.getHeight()));
            }

            void setBackImage(IndexedImage image) throws IndexedImage.ImageException
            {
                image.setPalette(paletteCopy);
                backPanel.setImage(image.getSubImage(0, 0, image.getWidth(), image.getHeight()));
            }
        }
    }

    static class SpriteModel extends FormatModel<PokemonSpriteData, SpriteContents>
    {

        public SpriteModel(List<PokemonSpriteData> data, List<TextBankData> textBankData)
        {
            super(data, textBankData);
        }

        @Override
        public Object getValueFor(int entryIdx, SpriteContents property)
        {
            PokemonSpriteData entry = getData().get(entryIdx);

            switch (property) {
                case FEMALE_BACK -> {
                    return entry.getFemaleBack();
                }
                case MALE_BACK -> {
                    return entry.getMaleBack();
                }
                case FEMALE_FRONT -> {
                    return entry.getFemaleFront();
                }
                case MALE_FRONT -> {
                    return entry.getMaleFront();
                }
                case PALETTE -> {
                    return entry.getPalette();
                }
                case SHINY_PALETTE -> {
                    return entry.getShinyPalette();
                }
                case PARTY_ICON -> {
                    return entry.getPartyIcon();
                }
                case GLOBAL_FRONT_Y -> {
                    return entry.getGlobalFrontYOffset();
                }
                case FEMALE_BACK_Y -> {
                    return entry.getFemaleBackOffset();
                }
                case MALE_BACK_Y -> {
                    return entry.getMaleBackOffset();
                }
                case FEMALE_FRONT_Y -> {
                    return entry.getFemaleFrontOffset();
                }
                case MALE_FRONT_Y -> {
                    return entry.getMaleFrontOffset();
                }
                case MOVEMENT -> {
                    return entry.getMovement();
                }
                case SHADOW_X -> {
                    return entry.getShadowXOffset();
                }
                case SHADOW_SIZE -> {
                    return entry.getShadowSize();
                }
                case PARTY_ICON_PALETTE -> {
                    return entry.getPartyIconPaletteIndex();
                }
            }

            return null;
        }

        @Override
        public void setValueFor(Object aValue, int entryIdx, SpriteContents property)
        {
            PokemonSpriteData entry = getData().get(entryIdx);

            switch (property) {
                case FEMALE_BACK -> entry.setFemaleBack((IndexedImage) aValue);
                case MALE_BACK -> entry.setMaleBack((IndexedImage) aValue);
                case FEMALE_FRONT -> entry.setFemaleFront((IndexedImage) aValue);
                case MALE_FRONT -> entry.setMaleFront((IndexedImage) aValue);
                case PALETTE -> entry.setPalette((Palette) aValue);
                case SHINY_PALETTE -> entry.setShinyPalette((Palette) aValue);
                case PARTY_ICON -> entry.setPartyIcon((IndexedImage) aValue);
                case GLOBAL_FRONT_Y -> entry.setGlobalFrontYOffset((Integer) aValue);
                case FEMALE_BACK_Y -> entry.setFemaleBackOffset((Integer) aValue);
                case MALE_BACK_Y -> entry.setMaleBackOffset((Integer) aValue);
                case FEMALE_FRONT_Y -> entry.setFemaleFrontOffset((Integer) aValue);
                case MALE_FRONT_Y -> entry.setMaleFrontOffset((Integer) aValue);
                case MOVEMENT -> entry.setMovement((Integer) aValue);
                case SHADOW_X -> entry.setShadowXOffset((Integer) aValue);
                case SHADOW_SIZE -> entry.setShadowSize((Integer) aValue);
                case PARTY_ICON_PALETTE -> entry.setPartyIconPaletteIndex((Integer) aValue);
            }
        }

        @Override
        public String getColumnNameKey(int columnIndex)
        {
            return null;
        }

        @Override
        public String getEntryName(int entryIdx)
        {
            TextBankData speciesNames = getTextBankData().get(TextFiles.SPECIES_NAMES.getValue());
            if (entryIdx < speciesNames.size())
                return speciesNames.get(entryIdx).getText();
            return super.getEntryName(entryIdx);
        }

        @Override
        public FormatModel<PokemonSpriteData, SpriteContents> getFrozenColumnModel()
        {
            return null;
        }

        @Override
        public int getColumnCount()
        {
            return 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            return null;
        }
    }

    enum SpriteContents
    {
        FEMALE_BACK(0, "move", null),
        MALE_BACK(1, "move", null),
        FEMALE_FRONT(2, "move", null),
        MALE_FRONT(3, "move", null),
        PALETTE(4, "move", null),
        SHINY_PALETTE(5, "level", null),
        PARTY_ICON(6, "", null),
        GLOBAL_FRONT_Y(7, "", null),
        FEMALE_BACK_Y(8, "", null),
        MALE_BACK_Y(9, "", null),
        FEMALE_FRONT_Y(10, "", null),
        MALE_FRONT_Y(11, "", null),
        MOVEMENT(12, "", null),
        SHADOW_X(13, "", null),
        SHADOW_SIZE(14, "", null),
        PARTY_ICON_PALETTE(15, "", null),
        NUMBER_OF_COLUMNS(PARTY_ICON_PALETTE.idx+1, null, null);

        private final int idx;
        private final String key;
        private final CellTypes cellType;

        SpriteContents(int idx, String key, CellTypes cellType)
        {
            this.idx = idx;
            this.key = key;
            this.cellType = cellType;
        }

        static SpriteContents getColumn(int idx)
        {
            for (SpriteContents column : SpriteContents.values())
            {
                if (column.idx == idx) {
                    return column;
                }
            }
            return NUMBER_OF_COLUMNS;
        }
    }

    enum ShadowSize {
        NONE(0),
        SMALL(1),
        MEDIUM(2),
        LARGE(3);

        final int value;

        ShadowSize(int value) {this.value= value;}

        static ShadowSize getSize(int value)
        {
            for (ShadowSize size : ShadowSize.values())
            {
                if (size.value == value)
                    return size;
            }
            return NONE;
        }
    }
}
