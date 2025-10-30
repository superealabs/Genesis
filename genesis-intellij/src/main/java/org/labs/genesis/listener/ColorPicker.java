package org.labs.genesis.listener;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@Setter
@Getter
public class ColorPicker implements ActionListener {
    private  Color selectedColor;
    private JTextField fieldComponent;
    public ColorPicker(JTextField field, Color defaultColor) {
        setFieldComponent(field);
        setSelectedColor(defaultColor);
    }
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        JColorChooser colorChooser = new JColorChooser();
        Color color = colorChooser.showDialog(null, "Pick a color", getSelectedColor());
        if (color != null) {
            setSelectedColor(color);
        }
    }

    public void setSelectedColor(Color selectedColor) {
        this.selectedColor = selectedColor;
        fieldComponent.setText(getSelcetedColorCode());
    }

    public String getSelcetedColorCode(){
        String rgbaHex = String.format(
                "#%02x%02x%02x",
                this.selectedColor.getRed(),
                this.selectedColor.getGreen(),
                this.selectedColor.getBlue()
        );
        return  rgbaHex;
    }
}
