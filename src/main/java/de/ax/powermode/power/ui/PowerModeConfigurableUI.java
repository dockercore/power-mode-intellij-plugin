package de.ax.powermode.power.ui;

import com.intellij.openapi.options.ConfigurableUi;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import de.ax.powermode.PowerMode;
import de.ax.powermode.power.color.ColorViewController;
import de.ax.powermode.power.color.MultiGradientPanel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;

/**
 * @author Baptiste Mesta
 */
public class PowerModeConfigurableUI implements ConfigurableUi<PowerMode> {


    private JPanel mainPanel;
    private JCheckBox powerModeEnabled;
    private JCheckBox shakeEnabled;
    private JSlider sparkCount;
    private JSlider sparkLife;
    private JSlider shakeRange;
    private JSlider heatup;
    private JSlider heatupTime;
    private JLabel sparkCountValue;
    private JLabel sparkLifeValue;
    private JLabel shakeRangeValue;
    private JLabel heatupValue;
    private JLabel heatupTimeValue;
    private JSlider maxFlameSize;
    private JLabel maxFlameSizeValue;
    private JSlider flameLife;
    private JLabel flameLifeValue;
    private JSlider keyStrokesPerMinute;
    private JLabel keyStrokesPerMinuteValue;
    private JCheckBox FLAMESCheckBox;
    private JCheckBox PARTICLESCheckBox;
    private JSlider sparkSize;
    private JLabel sparkSizeValue;
    private JLabel velocityFactorValue;
    private JLabel gravityFactorValue;
    private JSlider velocityFactor;
    private JSlider gravityFactor;
    private JLabel frameRateValue;
    private JSlider frameRate;
    private JSlider sparkColorRedTo;
    private JSlider sparkColorRedFrom;
    private JSlider sparkColorGreenFrom;
    private JSlider sparkColorGreenTo;
    private JSlider sparkColorBlueFrom;
    private JSlider sparkColorBlueTo;
    private JLabel sparkColorRedFromValue;
    private JLabel sparkColorRedToValue;
    private JLabel sparkColorGreenFromValue;
    private JLabel sparkColorGreenToValue;
    private JLabel sparkColorBlueFromValue;
    private JLabel sparkColorBlueToValue;
    private JLabel sparkColorAlphaValue;
    private JSlider sparkColorAlpha;
    private JPanel colorView;
    private JCheckBox visualizeEveryCaretMovementCheckBox;
    private JCheckBox PLAYMUSICCheckBox;
    private JTextField soundsFolder;
    private JCheckBox BAMCheckBox;
    private JLabel bamLifeValue;
    private JSlider bamLife;
    private JLabel heatupThresholdValue;
    private JSlider heatupThreshold;
    private JCheckBox PowerIndicatorCheckBox;
    private JTextField flameImagesFolder;
    private JTextField bamImagesFolder;
    private JCheckBox customFlameImages;
    private JCheckBox customBamImages;
    private JCheckBox HOTKEYHEATUPCheckBox;
    private JCheckBox animateBAMImagesCheckBox;


    public PowerModeConfigurableUI(PowerMode powerMode) {
        $$$setupUI$$$();
        ((MultiGradientPanel) colorView).setColorEdges(PowerMode.obtainColorEdges(powerMode));
        new ColorViewController((MultiGradientPanel) colorView, powerMode);
        powerModeEnabled.setSelected(powerMode.isEnabled());
        shakeEnabled.setSelected(powerMode.isShakeEnabled());
        shakeEnabled.addChangeListener(e -> powerMode.setShakeEnabled(shakeEnabled.isSelected()));
        FLAMESCheckBox.setSelected(powerMode.isFlamesEnabled());
        FLAMESCheckBox.addChangeListener(e -> powerMode.setFlamesEnabled(FLAMESCheckBox.isSelected()));
        PARTICLESCheckBox.setSelected(powerMode.isSparksEnabled());
        PARTICLESCheckBox.addChangeListener(e -> powerMode.setSparksEnabled(PARTICLESCheckBox.isSelected()));
        BAMCheckBox.setSelected(powerMode.isBamEnabled());
        BAMCheckBox.addChangeListener(e -> powerMode.setIsBamEnabled(BAMCheckBox.isSelected()));
        visualizeEveryCaretMovementCheckBox.setSelected(powerMode.getIsCaretAction());
        visualizeEveryCaretMovementCheckBox.addChangeListener(e -> powerMode.setIsCaretAction(visualizeEveryCaretMovementCheckBox.isSelected()));
        PLAYMUSICCheckBox.setEnabled(powerMode.mediaPlayerExists().isSuccess());
        PLAYMUSICCheckBox.setSelected(powerMode.isSoundsPlaying() && powerMode.mediaPlayerExists().isSuccess());
        PLAYMUSICCheckBox.addChangeListener(e -> powerMode.setIsSoundsPlaying(PLAYMUSICCheckBox.isSelected()));
        HOTKEYHEATUPCheckBox.setSelected(powerMode.isHotkeyHeatup());
        HOTKEYHEATUPCheckBox.addChangeListener(e -> powerMode.setHotkeyHeatup(HOTKEYHEATUPCheckBox.isSelected()));
        PowerIndicatorCheckBox.setSelected(powerMode.getIsPowerIndicatorEnabled());
        PowerIndicatorCheckBox.addChangeListener(e -> powerMode.setIsPowerIndicatorEnabled(PowerIndicatorCheckBox.isSelected()));
        animateBAMImagesCheckBox.setSelected(!powerMode.getIsSingleBamImagePerEvent());
        animateBAMImagesCheckBox.addChangeListener(e -> powerMode.setIsSingleBamImagePerEvent(!animateBAMImagesCheckBox.isSelected()));

        soundsFolder.setText(powerMode.getSoundsFolder());

        soundsFolder.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                powerMode.setSoundsFolder(soundsFolder.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                powerMode.setSoundsFolder(soundsFolder.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                powerMode.setSoundsFolder(soundsFolder.getText());
            }
        });
        initValues(powerMode.getSparkCount(), sparkCount, sparkCountValue, slider -> powerMode.setSparkCount(slider.getValue()));
        initValues(powerMode.getSparkSize(), sparkSize, sparkSizeValue, slider -> powerMode.setSparkSize(slider.getValue()));
        initValues(powerMode.getSparkLife(), sparkLife, sparkLifeValue, slider -> powerMode.setSparkLife(slider.getValue()));
        initValues(Double.valueOf((powerMode.getSparkVelocityFactor() * 100.0)).intValue(), velocityFactor, velocityFactorValue, slider -> powerMode.setSparkVelocityFactor(slider.getValue() / 100.0));
        initValues(Double.valueOf(powerMode.getGravityFactor() * 100.0).intValue(), gravityFactor, gravityFactorValue, slider -> powerMode.setGravityFactor(slider.getValue() / 100.0));
        initValues(powerMode.getShakeRange(), shakeRange, shakeRangeValue, slider -> powerMode.setShakeRange(slider.getValue()));
        initValues(powerMode.getHeatup(), heatup, heatupValue, slider -> powerMode.setHeatup(slider.getValue()));
        initValues(powerMode.getHeatupTime(), heatupTime, heatupTimeValue, slider -> powerMode.setHeatupTime(slider.getValue()));
        initValues(powerMode.getHeatupThreshold(), heatupThreshold, heatupThresholdValue, slider -> powerMode.setHeatupThreshold(slider.getValue()));
        initValues(powerMode.getFlameLife(), flameLife, flameLifeValue, slider -> powerMode.setFlameLife(slider.getValue()));
        initValues((int) powerMode.getBamLife(), bamLife, bamLifeValue, slider -> powerMode.setBamLife(slider.getValue()));
        initValues(powerMode.getmaxFlameSize(), maxFlameSize, maxFlameSizeValue, slider -> powerMode.setmaxFlameSize(slider.getValue()));
        initValues(powerMode.getKeyStrokesPerMinute(), keyStrokesPerMinute, keyStrokesPerMinuteValue, slider -> powerMode.setKeyStrokesPerMinute(slider.getValue()));
        initValues(powerMode.getFrameRate(), frameRate, frameRateValue, slider -> powerMode.setFrameRateHertz(slider.getValue()));

        initValuesColor(powerMode.getRedFrom(), sparkColorRedFrom, sparkColorRedFromValue, powerMode, slider -> powerMode.setRedFrom(slider.getValue()));
        initValuesColor(powerMode.getRedTo(), sparkColorRedTo, sparkColorRedToValue, powerMode, slider -> powerMode.setRedTo(slider.getValue()));
        bindSlieders(sparkColorRedFrom, sparkColorRedTo);

        initValuesColor(powerMode.getGreenFrom(), sparkColorGreenFrom, sparkColorGreenFromValue, powerMode, slider -> powerMode.setGreenFrom(slider.getValue()));
        initValuesColor(powerMode.getGreenTo(), sparkColorGreenTo, sparkColorGreenToValue, powerMode, slider -> powerMode.setGreenTo(slider.getValue()));
        bindSlieders(sparkColorGreenFrom, sparkColorGreenTo);

        initValuesColor(powerMode.getBlueFrom(), sparkColorBlueFrom, sparkColorBlueFromValue, powerMode, slider -> powerMode.setBlueFrom(slider.getValue()));
        initValuesColor(powerMode.getBlueTo(), sparkColorBlueTo, sparkColorBlueToValue, powerMode, slider -> powerMode.setBlueTo(slider.getValue()));
        bindSlieders(sparkColorBlueFrom, sparkColorBlueTo);

        initValuesColor(powerMode.getColorAlpha(), sparkColorAlpha, sparkColorAlphaValue, powerMode, slider -> powerMode.setColorAlpha(slider.getValue()));

        customFlameImages.setSelected(powerMode.isCustomFlameImages());
        customFlameImages.addChangeListener(e -> powerMode.setCustomFlameImages(customFlameImages.isSelected()));
        {
            flameImagesFolder.setText(powerMode.getCustomFlameImageFolder());

            flameImagesFolder.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    setFolder(powerMode);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    setFolder(powerMode);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    setFolder(powerMode);
                }

                private void setFolder(PowerMode powerMode) {
                    if (validateImagePath(powerMode,
                            PowerModeConfigurableUI.this.flameImagesFolder,
                            PowerModeConfigurableUI.this.customFlameImages)) {
                        powerMode.setCustomFlameImageFolder(flameImagesFolder.getText());
                    }
                }
            });
        }

        customBamImages.setSelected(powerMode.isCustomBamImages());
        customBamImages.addChangeListener(e -> powerMode.setCustomBamImages(customBamImages.isSelected()));

        {
            bamImagesFolder.setText(powerMode.getCustomBamImageFolder());

            bamImagesFolder.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    setFolder(powerMode);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    setFolder(powerMode);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    setFolder(powerMode);
                }

                private void setFolder(PowerMode powerMode) {
                    if (validateImagePath(powerMode,
                            PowerModeConfigurableUI.this.bamImagesFolder,
                            PowerModeConfigurableUI.this.customBamImages)) {
                        powerMode.setCustomBamImageFolder(bamImagesFolder.getText());
                    }
                }
            });
        }
    }

    private boolean validateImagePath(PowerMode powerMode, JTextField flameImagesFolder, JCheckBox customFlameImages) {
        String folder = flameImagesFolder.getText();
        File file = new File(folder).getAbsoluteFile();
        if (!file.exists() || file.getAbsolutePath().toLowerCase().contains("temp") || file.getAbsolutePath().toLowerCase().contains("tmp")) {
            customFlameImages.setSelected(false);
            JOptionPane.showMessageDialog(mainPanel,
                    "invalid folder! Folder '" + file.getAbsolutePath() + "' does not exist or is a temp file (contains 'Temp' or 'tmp')", "invalid folder",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } else {
            return true;
        }
    }

    private void bindSlieders(JSlider from, JSlider to) {
        from.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (from.getValue() > to.getValue()) {
                    to.setValue(from.getValue());
                }
            }
        });
        to.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (to.getValue() < from.getValue()) {
                    from.setValue(to.getValue());
                }
            }
        });
    }

    private void initValuesColor(int initValue, JSlider slider, JLabel sliderValueLabel, PowerMode powerMode, ValueColorSettable valueSettable) {
        initValues(initValue, slider, sliderValueLabel, slider1 -> {
            valueSettable.setValue(slider1);
            ((MultiGradientPanel) colorView).setColorEdges(PowerMode.obtainColorEdges(powerMode));
        });

    }

    private void initValues(int initValue, JSlider slider, JLabel sliderValueLabel, ValueSettable valueSettable) {
        slider.setValue(initValue);
        sliderValueLabel.setText(String.valueOf(initValue));
        slider.addChangeListener(new MyChangeListener(slider, sliderValueLabel) {
            @Override
            public void setValue(JSlider slider) {
                valueSettable.setValue(slider);
            }
        });
    }

    @Override
    public void reset(PowerMode powerMode) {
        powerModeEnabled.setSelected(powerMode.isEnabled());
    }

    @Override
    public boolean isModified(PowerMode powerMode) {
        return powerModeEnabled.isSelected() != powerMode.isEnabled();
    }

    @Override
    public void apply(PowerMode powerMode) throws ConfigurationException {
        powerMode.setEnabled(powerModeEnabled.isSelected());
    }


    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    private void createUIComponents() {
        colorView = new MultiGradientPanel(200, PowerMode.obtainColorEdges(PowerMode.getInstance()));


    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JTabbedPane tabbedPane1 = new JTabbedPane();
        tabbedPane1.setTabLayoutPolicy(0);
        mainPanel.add(tabbedPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("GENERAL OPTIONS", panel1);
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setEnabled(true);
        panel1.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(17, 3, new Insets(0, 0, 0, 0), -1, -1));
        scrollPane1.setViewportView(panel2);
        final JLabel label1 = new JLabel();
        label1.setText("framerate");
        label1.setToolTipText("particle & flame updates per second");
        panel2.add(label1, new GridConstraints(16, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(176, 15), null, 0, false));
        frameRateValue = new JLabel();
        frameRateValue.setText("Label");
        panel2.add(frameRateValue, new GridConstraints(16, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        frameRate = new JSlider();
        frameRate.setMaximum(120);
        frameRate.setMinimum(5);
        frameRate.setValue(30);
        panel2.add(frameRate, new GridConstraints(16, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        powerModeEnabled = new JCheckBox();
        powerModeEnabled.setText("ENTER POWER MODE!");
        panel2.add(powerModeEnabled, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        PARTICLESCheckBox = new JCheckBox();
        PARTICLESCheckBox.setText("SPARKS!");
        panel2.add(PARTICLESCheckBox, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        shakeEnabled = new JCheckBox();
        shakeEnabled.setText("SHAKE!");
        panel2.add(shakeEnabled, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        FLAMESCheckBox = new JCheckBox();
        FLAMESCheckBox.setText("FLAMES!");
        panel2.add(FLAMESCheckBox, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        visualizeEveryCaretMovementCheckBox = new JCheckBox();
        visualizeEveryCaretMovementCheckBox.setText("VISUALIZE EVERY CARET MOVEMENT!");
        visualizeEveryCaretMovementCheckBox.setToolTipText("When disabled: only typing of letters will cause effects.\nWhen enabled: every caret movement will cause effects.");
        panel2.add(visualizeEveryCaretMovementCheckBox, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        PLAYMUSICCheckBox = new JCheckBox();
        PLAYMUSICCheckBox.setText("PLAY MUSIC");
        PLAYMUSICCheckBox.setToolTipText("next song: shift ctrl alt M");
        panel2.add(PLAYMUSICCheckBox, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        soundsFolder = new JTextField();
        panel2.add(soundsFolder, new GridConstraints(8, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        BAMCheckBox = new JCheckBox();
        BAMCheckBox.setText("BAM!");
        BAMCheckBox.setToolTipText("visualize document changes with BAM!");
        panel2.add(BAMCheckBox, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("BAM! life [ms]");
        panel2.add(label2, new GridConstraints(13, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        bamLifeValue = new JLabel();
        bamLifeValue.setText("Label");
        panel2.add(bamLifeValue, new GridConstraints(13, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        bamLife = new JSlider();
        bamLife.setMaximum(10000);
        panel2.add(bamLife, new GridConstraints(13, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        PowerIndicatorCheckBox = new JCheckBox();
        PowerIndicatorCheckBox.setText("POWER INDICATOR!");
        PowerIndicatorCheckBox.setToolTipText("Displays an imprecise visualization of the current heatup value.");
        panel2.add(PowerIndicatorCheckBox, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("shake range [px]");
        label3.setToolTipText("how many pixels should the shaking move?");
        panel2.add(label3, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(176, 15), null, 0, false));
        shakeRangeValue = new JLabel();
        shakeRangeValue.setText("Label");
        panel2.add(shakeRangeValue, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        shakeRange = new JSlider();
        panel2.add(shakeRange, new GridConstraints(9, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("max flame size [px]");
        label4.setToolTipText("maximum flame size in pixels");
        panel2.add(label4, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(176, 15), null, 0, false));
        maxFlameSizeValue = new JLabel();
        maxFlameSizeValue.setText("Label");
        panel2.add(maxFlameSizeValue, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        maxFlameSize = new JSlider();
        maxFlameSize.setMaximum(500);
        panel2.add(maxFlameSize, new GridConstraints(10, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("flame life [ms]");
        label5.setToolTipText("time to live for a flame in milliseconds");
        panel2.add(label5, new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(176, 15), null, 0, false));
        flameLifeValue = new JLabel();
        flameLifeValue.setText("Label");
        panel2.add(flameLifeValue, new GridConstraints(11, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        flameLife = new JSlider();
        flameLife.setMaximum(10000);
        panel2.add(flameLife, new GridConstraints(11, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("hint: 'heatup base level' should be <100%");
        panel2.add(label6, new GridConstraints(7, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        customFlameImages = new JCheckBox();
        customFlameImages.setText("custom flame images");
        customFlameImages.setToolTipText("provide a folder with the images of a flame animation");
        panel2.add(customFlameImages, new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        flameImagesFolder = new JTextField();
        panel2.add(flameImagesFolder, new GridConstraints(12, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("image folder:");
        panel2.add(label7, new GridConstraints(12, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        customBamImages = new JCheckBox();
        customBamImages.setText("custom BAM! images");
        customBamImages.setToolTipText("choose your own BAM! images");
        panel2.add(customBamImages, new GridConstraints(14, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        animateBAMImagesCheckBox = new JCheckBox();
        animateBAMImagesCheckBox.setText("animate BAM! images");
        animateBAMImagesCheckBox.setToolTipText("will cycle through all BAM! images in the given folder and create an animation (like a GIF).");
        panel2.add(animateBAMImagesCheckBox, new GridConstraints(15, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        bamImagesFolder = new JTextField();
        panel2.add(bamImagesFolder, new GridConstraints(14, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("image folder:");
        panel2.add(label8, new GridConstraints(14, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        HOTKEYHEATUPCheckBox = new JCheckBox();
        HOTKEYHEATUPCheckBox.setText("HOTKEY HEATUP");
        HOTKEYHEATUPCheckBox.setToolTipText("Disable if having problems with hotkey visualization");
        panel2.add(HOTKEYHEATUPCheckBox, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("!!! might interfere with autocompletion and other plugins/editors!");
        panel2.add(label9, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("music folder:");
        panel2.add(label10, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        tabbedPane1.addTab("HEATUP", scrollPane2);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(6, 3, new Insets(0, 0, 0, 0), -1, -1));
        scrollPane2.setViewportView(panel3);
        panel3.setBorder(BorderFactory.createTitledBorder(null, "Heatup", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label11 = new JLabel();
        label11.setText("heatup base level [%]");
        label11.setToolTipText("the lower the level the fewer happens at the start of typing.");
        panel3.add(label11, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(176, 15), null, 0, false));
        heatupValue = new JLabel();
        heatupValue.setText("Label");
        panel3.add(heatupValue, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        heatup = new JSlider();
        panel3.add(heatup, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("heatup time [ms]");
        label12.setToolTipText("time range in milliseconds. The count of keystrokes in this period is used to calculate heatup.");
        panel3.add(label12, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(176, 15), null, 0, false));
        heatupTimeValue = new JLabel();
        heatupTimeValue.setText("Label");
        panel3.add(heatupTimeValue, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        heatupTime = new JSlider();
        heatupTime.setMaximum(60000);
        panel3.add(heatupTime, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label13 = new JLabel();
        label13.setText("heatup threshold [%]");
        label13.setToolTipText("how much heatup in % has to be reached until something happens");
        panel3.add(label13, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(176, 15), null, 0, false));
        heatupThresholdValue = new JLabel();
        heatupThresholdValue.setText("Label");
        panel3.add(heatupThresholdValue, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        heatupThreshold = new JSlider();
        heatupThreshold.setMaximum(100);
        heatupThreshold.setMinimum(0);
        heatupThreshold.setValue(30);
        panel3.add(heatupThreshold, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label14 = new JLabel();
        label14.setText("keystrokes per minute");
        label14.setToolTipText("used to calculate heatup");
        panel3.add(label14, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(176, 15), null, 0, false));
        keyStrokesPerMinuteValue = new JLabel();
        keyStrokesPerMinuteValue.setText("Label");
        panel3.add(keyStrokesPerMinuteValue, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        keyStrokesPerMinute = new JSlider();
        keyStrokesPerMinute.setMaximum(3000);
        keyStrokesPerMinute.setValue(100);
        panel3.add(keyStrokesPerMinute, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JTextArea textArea1 = new JTextArea();
        textArea1.setEditable(false);
        textArea1.setEnabled(true);
        textArea1.setText("Heatup is the dynamic power factor in % which defines how much\n sparkling and fire is happening. \nIts calculated by 'keystrokes per minute' constant, \nthe 'heatup time'- window and how much typing and hotkeying is done within that time window.\n'heatup base level' is the minimum heatup that is always active. \n'heatup threshold' is the minimum heatup required such that things start happening.\n\n");
        panel3.add(textArea1, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 50), null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane3 = new JScrollPane();
        tabbedPane1.addTab("SPARKS", scrollPane3);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        scrollPane3.setViewportView(panel4);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(16, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 245), null, 0, false));
        panel5.setBorder(BorderFactory.createTitledBorder(null, "Sparks", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        sparkColorRedFromValue = new JLabel();
        sparkColorRedFromValue.setText("Label");
        panel5.add(sparkColorRedFromValue, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sparkColorRedFrom = new JSlider();
        sparkColorRedFrom.setMaximum(255);
        panel5.add(sparkColorRedFrom, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel5.add(colorView, new GridConstraints(0, 0, 11, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(200, 200), new Dimension(200, 200), new Dimension(200, 200), 0, false));
        final JLabel label15 = new JLabel();
        label15.setText("spark color red range");
        label15.setToolTipText("how many sparks should appear?");
        panel5.add(label15, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(136, 14), null, 0, false));
        sparkColorRedToValue = new JLabel();
        sparkColorRedToValue.setText("Label");
        panel5.add(sparkColorRedToValue, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sparkColorRedTo = new JSlider();
        sparkColorRedTo.setMaximum(255);
        panel5.add(sparkColorRedTo, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label16 = new JLabel();
        label16.setText("spark color green range");
        label16.setToolTipText("how many sparks should appear?");
        panel5.add(label16, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sparkColorGreenFromValue = new JLabel();
        sparkColorGreenFromValue.setText("Label");
        panel5.add(sparkColorGreenFromValue, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sparkColorGreenFrom = new JSlider();
        sparkColorGreenFrom.setMaximum(255);
        panel5.add(sparkColorGreenFrom, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sparkColorGreenToValue = new JLabel();
        sparkColorGreenToValue.setText("Label");
        panel5.add(sparkColorGreenToValue, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sparkColorGreenTo = new JSlider();
        sparkColorGreenTo.setMaximum(255);
        panel5.add(sparkColorGreenTo, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label17 = new JLabel();
        label17.setText("spark color blue range");
        label17.setToolTipText("how many sparks should appear?");
        panel5.add(label17, new GridConstraints(6, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sparkColorBlueFromValue = new JLabel();
        sparkColorBlueFromValue.setText("Label");
        panel5.add(sparkColorBlueFromValue, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sparkColorBlueFrom = new JSlider();
        sparkColorBlueFrom.setMaximum(255);
        panel5.add(sparkColorBlueFrom, new GridConstraints(7, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sparkColorBlueToValue = new JLabel();
        sparkColorBlueToValue.setText("Label");
        panel5.add(sparkColorBlueToValue, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sparkColorBlueTo = new JSlider();
        sparkColorBlueTo.setMaximum(255);
        panel5.add(sparkColorBlueTo, new GridConstraints(8, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label18 = new JLabel();
        label18.setText("alpha");
        panel5.add(label18, new GridConstraints(9, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sparkColorAlphaValue = new JLabel();
        sparkColorAlphaValue.setText("Label");
        panel5.add(sparkColorAlphaValue, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sparkColorAlpha = new JSlider();
        sparkColorAlpha.setMaximum(255);
        panel5.add(sparkColorAlpha, new GridConstraints(10, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label19 = new JLabel();
        label19.setText("spark count");
        label19.setToolTipText("how many sparks should appear?");
        panel5.add(label19, new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(176, 15), null, 0, false));
        sparkCountValue = new JLabel();
        sparkCountValue.setText("Label");
        panel5.add(sparkCountValue, new GridConstraints(11, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sparkCount = new JSlider();
        sparkCount.setMaximum(100);
        panel5.add(sparkCount, new GridConstraints(11, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label20 = new JLabel();
        label20.setText("spark life [ms]");
        label20.setToolTipText("how many milliseconds should a spark live?");
        panel5.add(label20, new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(176, 15), null, 0, false));
        sparkLifeValue = new JLabel();
        sparkLifeValue.setText("Label");
        panel5.add(sparkLifeValue, new GridConstraints(12, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sparkLife = new JSlider();
        sparkLife.setMaximum(10000);
        panel5.add(sparkLife, new GridConstraints(12, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label21 = new JLabel();
        label21.setText("spark size [px]");
        label21.setToolTipText("spark size in pixels");
        panel5.add(label21, new GridConstraints(13, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(176, 15), null, 0, false));
        sparkSizeValue = new JLabel();
        sparkSizeValue.setText("Label");
        panel5.add(sparkSizeValue, new GridConstraints(13, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sparkSize = new JSlider();
        sparkSize.setMaximum(10);
        panel5.add(sparkSize, new GridConstraints(13, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label22 = new JLabel();
        label22.setText("spark velocity factor %");
        label22.setToolTipText("100% is default spark velocity");
        panel5.add(label22, new GridConstraints(14, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(176, 15), null, 0, false));
        velocityFactorValue = new JLabel();
        velocityFactorValue.setText("Label");
        panel5.add(velocityFactorValue, new GridConstraints(14, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        velocityFactor = new JSlider();
        velocityFactor.setMaximum(10000);
        velocityFactor.setMinimum(-10000);
        velocityFactor.setValue(100);
        panel5.add(velocityFactor, new GridConstraints(14, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label23 = new JLabel();
        label23.setText("spark gravity factor %");
        label23.setToolTipText("100% is default spark gravity");
        panel5.add(label23, new GridConstraints(15, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(176, 15), null, 0, false));
        gravityFactorValue = new JLabel();
        gravityFactorValue.setText("Label");
        panel5.add(gravityFactorValue, new GridConstraints(15, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        gravityFactor = new JSlider();
        gravityFactor.setMaximum(10000);
        gravityFactor.setMinimum(-10000);
        gravityFactor.setValue(100);
        panel5.add(gravityFactor, new GridConstraints(15, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel4.add(spacer3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

    private interface ValueColorSettable {
        void setValue(JSlider slider);
    }

    private abstract class MyChangeListener implements ChangeListener, ValueSettable {
        private final JSlider slider;
        private final JLabel jLabel;

        public MyChangeListener(JSlider slider, JLabel jLabel) {
            this.slider = slider;
            this.jLabel = jLabel;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            setValue(slider);
            jLabel.setText(String.valueOf(slider.getValue()));
        }

    }
}
