/*
 * Created by JFormDesigner
 */

package io.github.turtleisaac.pokeditor.gui.editors.formats.pokemon_sprite;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import io.github.turtleisaac.nds4j.images.IndexedImage;
import io.github.turtleisaac.nds4j.images.Palette;
import io.github.turtleisaac.pokeditor.formats.personal.PersonalData;
import io.github.turtleisaac.pokeditor.formats.pokemon_sprites.PokemonSpriteData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gamedata.TextFiles;
import io.github.turtleisaac.pokeditor.gui.editors.DefaultEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.CellTypes;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.FormatModel;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.formats.LearnsetsTable;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.formats.PersonalTable;
import net.miginfocom.swing.*;

/**
 * @author turtleisaac
 */
public class PokemonSpriteEditor extends DefaultEditor<PokemonSpriteData, PokemonSpriteEditor.SpriteContents>
{
//    private static final Dimension dimension = new Dimension(1060, 670);

    public PokemonSpriteEditor(List<PokemonSpriteData> data, List<TextBankData> textBankData) {
        super(new SpriteModel(data, textBankData));
//        setPreferredSize(dimension);
//        setMinimumSize(dimension);
        initComponents();
        partyIconPanel.panel.multiplier = 2;
        femaleBackPanel.label.setText("Female Back");
        maleBackPanel.label.setText("Male Back");
        femaleFrontPanel.label.setText("Female Front");
        maleFrontPanel.label.setText("Male Front");
        partyIconPanel.label.setText("Party Icon");
    }

    @Override
    public void selectedIndexedChanged(int idx, ActionEvent e)
    {
        femaleBackPanel.setImage((IndexedImage) getModel().getValueFor(idx, SpriteContents.FEMALE_BACK));
        maleBackPanel.setImage((IndexedImage) getModel().getValueFor(idx, SpriteContents.MALE_BACK));
        femaleFrontPanel.setImage((IndexedImage) getModel().getValueFor(idx, SpriteContents.FEMALE_FRONT));
        maleFrontPanel.setImage((IndexedImage) getModel().getValueFor(idx, SpriteContents.MALE_FRONT));
        partyIconPanel.setImage((IndexedImage) getModel().getValueFor(idx, SpriteContents.PARTY_ICON));
        battleMockupPanel1.setSprites(maleFrontPanel.panel.image, maleBackPanel.panel.image, (byte) 0, (byte) 0, 0, 0, false);
        palettePanel.setPalette((Palette) getModel().getValueFor(idx, SpriteContents.PALETTE));
        shinyPalettePanel.setPalette((Palette) getModel().getValueFor(idx, SpriteContents.SHINY_PALETTE));
    }

    private void thisComponentResized(ComponentEvent e) {
        System.out.println("moo");
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

    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner non-commercial license
        ResourceBundle bundle = ResourceBundle.getBundle("pokeditor.sheet_panel");
        femaleBackPanel = new PokemonSpriteEditor.PokemonSpriteDisplayPanel();
        maleBackPanel = new PokemonSpriteEditor.PokemonSpriteDisplayPanel();
        separator1 = new JSeparator();
        panel1 = new JPanel();
        battleMockupPanel1 = new PokemonSpriteEditor.BattleMockupPanel();
        toggleButton2 = new JToggleButton();
        toggleButton1 = new JToggleButton();
        toggleButton3 = new JToggleButton();
        panel2 = new JPanel();
        label1 = new JLabel();
        spinner1 = new JSpinner();
        separator4 = new JSeparator();
        label2 = new JLabel();
        spinner2 = new JSpinner();
        label3 = new JLabel();
        spinner3 = new JSpinner();
        separator5 = new JSeparator();
        label4 = new JLabel();
        spinner4 = new JSpinner();
        label5 = new JLabel();
        spinner5 = new JSpinner();
        separator3 = new JSeparator();
        label6 = new JLabel();
        spinner6 = new JSpinner();
        separator2 = new JSeparator();
        label7 = new JLabel();
        spinner7 = new JSpinner();
        label8 = new JLabel();
        comboBox1 = new JComboBox();
        separator6 = new JSeparator();
        partyIconPanel = new PokemonSpriteEditor.PokemonSpriteDisplayPanel();
        femaleFrontPanel = new PokemonSpriteEditor.PokemonSpriteDisplayPanel();
        maleFrontPanel = new PokemonSpriteEditor.PokemonSpriteDisplayPanel();
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
                "[]0" +
                "[grow]"));
            panel1.add(battleMockupPanel1, "cell 0 0");

            //---- toggleButton2 ----
            toggleButton2.setText(bundle.getString("PokemonSpriteEditor.toggleButton2.text"));
            panel1.add(toggleButton2, "cell 0 1");

            //---- toggleButton1 ----
            toggleButton1.setText(bundle.getString("PokemonSpriteEditor.toggleButton1.text"));
            panel1.add(toggleButton1, "cell 0 1");

            //---- toggleButton3 ----
            toggleButton3.setText(bundle.getString("PokemonSpriteEditor.toggleButton3.text"));
            panel1.add(toggleButton3, "cell 0 2");

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

                //---- label1 ----
                label1.setText(bundle.getString("PokemonSpriteEditor.label1.text"));
                panel2.add(label1, "cell 0 0");
                panel2.add(spinner1, "cell 1 0,growx");
                panel2.add(separator4, "cell 0 1 2 1");

                //---- label2 ----
                label2.setText(bundle.getString("PokemonSpriteEditor.label2.text"));
                panel2.add(label2, "cell 0 2");
                panel2.add(spinner2, "cell 1 2,growx");

                //---- label3 ----
                label3.setText(bundle.getString("PokemonSpriteEditor.label3.text"));
                panel2.add(label3, "cell 0 3");
                panel2.add(spinner3, "cell 1 3,growx");
                panel2.add(separator5, "cell 0 4 2 1");

                //---- label4 ----
                label4.setText(bundle.getString("PokemonSpriteEditor.label4.text"));
                panel2.add(label4, "cell 0 5");
                panel2.add(spinner4, "cell 1 5,growx");

                //---- label5 ----
                label5.setText(bundle.getString("PokemonSpriteEditor.label5.text"));
                panel2.add(label5, "cell 0 6");
                panel2.add(spinner5, "cell 1 6,growx");
                panel2.add(separator3, "cell 0 7 2 1");

                //---- label6 ----
                label6.setText(bundle.getString("PokemonSpriteEditor.label6.text"));
                panel2.add(label6, "cell 0 8");
                panel2.add(spinner6, "cell 1 8,growx");
                panel2.add(separator2, "cell 0 9 2 1,growx");

                //---- label7 ----
                label7.setText(bundle.getString("PokemonSpriteEditor.label7.text"));
                panel2.add(label7, "cell 0 10");
                panel2.add(spinner7, "cell 1 10,growx");

                //---- label8 ----
                label8.setText(bundle.getString("PokemonSpriteEditor.label8.text"));
                panel2.add(label8, "cell 0 11");
                panel2.add(comboBox1, "cell 1 11");
            }
            panel1.add(panel2, "cell 0 3,aligny top,grow 100 0");
        }
        add(panel1, "cell 3 0 1 3,alignx left,grow 0 100");

        //---- separator6 ----
        separator6.setOrientation(SwingConstants.VERTICAL);
        add(separator6, "cell 4 0 1 3,grow");
        add(partyIconPanel, "cell 5 0 1 3");

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
    private JToggleButton toggleButton2;
    private JToggleButton toggleButton1;
    private JToggleButton toggleButton3;
    private JPanel panel2;
    private JLabel label1;
    private JSpinner spinner1;
    private JSeparator separator4;
    private JLabel label2;
    private JSpinner spinner2;
    private JLabel label3;
    private JSpinner spinner3;
    private JSeparator separator5;
    private JLabel label4;
    private JSpinner spinner4;
    private JLabel label5;
    private JSpinner spinner5;
    private JSeparator separator3;
    private JLabel label6;
    private JSpinner spinner6;
    private JSeparator separator2;
    private JLabel label7;
    private JSpinner spinner7;
    private JLabel label8;
    private JComboBox comboBox1;
    private JSeparator separator6;
    private PokemonSpriteEditor.PokemonSpriteDisplayPanel partyIconPanel;
    private PokemonSpriteEditor.PokemonSpriteDisplayPanel femaleFrontPanel;
    private PokemonSpriteEditor.PokemonSpriteDisplayPanel maleFrontPanel;
    private JTabbedPane tabbedPane1;
    private PokemonSpriteEditor.PalettePanel palettePanel;
    private PokemonSpriteEditor.PalettePanel shinyPalettePanel;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    static class PokemonSpriteDisplayPanel extends JPanel
    {
        PokemonSpriteDisplaySubPanel panel = new PokemonSpriteDisplaySubPanel();
        JButton exportButton;
        JButton importButton;
        JLabel label;

        public PokemonSpriteDisplayPanel()
        {
            setLayout(new MigLayout(
                    "ins 2",
                    // columns
                    "[center,fill]",
                    // rows
                    "[]" +
                            "[]"));

            label = new JLabel("Female Front");
            label.setHorizontalAlignment(JLabel.CENTER);
            exportButton = new JButton("Export");
            importButton = new JButton("Import");

            add(label, "cell 0 0,grow");
            add(panel, "cell 0 1,grow");
            add(exportButton, "cell 0 2,grow");
            add(importButton, "cell 0 2,grow");
        }

        public void setImage(IndexedImage image)
        {
            panel.setImage(image);
            setMaximumSize(getPreferredSize());
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

    static class BattleMockupPanel extends JPanel
    {
        static Image textBar;
        static Image background;
        static Image playerPlatform;
        static Image enemyPlatform;
        static Image playerHealth;
        static Image enemyHealth;

        static BufferedImage frontSprite;
        static BufferedImage backSprite;

        static Image shadow;
        static Image genderIcon;


        static byte frontYOffset;
        static byte shadowXOffset;
//        static SpriteDataProcessor.ShadowType shadowType;


        static int frontModifier;
        static int backModifier;

        static boolean isFemale;

        public BattleMockupPanel()
        {
            super();
            textBar = new ImageIcon(BattleMockupPanel.class.getResource("/pokeditor/icons/text_bar.png")).getImage();
            background = new ImageIcon(BattleMockupPanel.class.getResource("/pokeditor/icons/background.png")).getImage();
            playerPlatform = new ImageIcon(BattleMockupPanel.class.getResource("/pokeditor/icons/platform_you.png")).getImage();
            enemyPlatform = new ImageIcon(BattleMockupPanel.class.getResource("/pokeditor/icons/platform_opponent.png")).getImage();
            playerHealth = new ImageIcon(BattleMockupPanel.class.getResource("/pokeditor/icons/health_you.png")).getImage();
            enemyHealth = new ImageIcon(BattleMockupPanel.class.getResource("/pokeditor/icons/health_opponent.png")).getImage();


            Dimension dimension= new Dimension(256,192);
//            setPreferredSize(dimension);
            setMinimumSize(dimension);
//            setMaximumSize(dimension);
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


//                if(shadowType == SpriteDataProcessor.ShadowType.Small)
//                {
//                    g2d.drawImage(shadow,179 + shadowXOffset, 83, null);
//                }
//                else if(shadowType == SpriteDataProcessor.ShadowType.Medium)
//                {
//                    g2d.drawImage(shadow,174 + shadowXOffset, 83, null);
//                }
//                else if(shadowType == SpriteDataProcessor.ShadowType.Large)
//                {
//                    g2d.drawImage(shadow,167 + shadowXOffset, 82, null);
//                }


                if(frontSprite != null)
                {
                    g2d.drawImage(frontSprite,152,10 - frontYOffset - frontModifier,null);
                    g2d.drawImage(backSprite,23,72 - backModifier,null);
                }

                g2d.drawImage(playerHealth,129,95, null);
                g2d.drawImage(enemyHealth,0,18, null);

                g2d.drawImage(genderIcon,217,103,null);
                g2d.drawImage(genderIcon,65,25, null);

                g2d.drawImage(textBar,0,getHeight()-48, null);
                g2d.dispose();

                g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
            }
            catch (NullPointerException exception)
            {
                exception.printStackTrace();
            }

        }

        public void setSprites(IndexedImage newFront, IndexedImage newBack, byte newFrontYOffset, byte newShadowXOffset, /*SpriteDataProcessor.ShadowType newShadowType,*/ int frontMod, int backMod, boolean female)
        {
            frontSprite= newFront.getTransparentImage();
            backSprite= newBack.getTransparentImage();
            frontYOffset= newFrontYOffset;
            shadowXOffset= newShadowXOffset;
//            shadowType= newShadowType;

            frontModifier= frontMod;
            backModifier= backMod;

//            switch (newShadowType)
//            {
//                case None:
//                    shadow= null;
//                    break;
//
//                case Small:
//                    shadow= new ImageIcon(BattleMockupPanel.class.getResource("/pokeditor/icons/shadow_small.png")).getImage();
//                    break;
//
//                case Medium:
//                    shadow= new ImageIcon(BattleMockupPanel.class.getResource("/pokeditor/icons/shadow_medium.png")).getImage();
//                    break;
//
//                case Large:
//                    shadow= new ImageIcon(BattleMockupPanel.class.getResource("/pokeditor/icons/shadow_large.png")).getImage();
//                    break;
//            }

            isFemale= female;

            if(female)
            {
                genderIcon= new ImageIcon(BattleMockupPanel.class.getResource("/pokeditor/icons/symbol_female.png")).getImage();
            }
            else
            {
                genderIcon= new ImageIcon(BattleMockupPanel.class.getResource("/pokeditor/icons/symbol_male.png")).getImage();
            }
            repaint();
        }
    }

    static class PalettePanel extends JPanel
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
        }

        public void setPalette(Palette palette)
        {
            this.palette = palette;
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
        NUMBER_OF_COLUMNS(PARTY_ICON.idx+1, null, null);

        private final int idx;
        private final String key;
        private final CellTypes cellType;
        int repetition;

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
}
