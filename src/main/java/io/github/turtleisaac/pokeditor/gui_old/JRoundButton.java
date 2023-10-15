package io.github.turtleisaac.pokeditor.gui_old;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class JRoundButton extends JButton
{
    public JRoundButton()
    {
        super();
//        this.setBorder(new RoundedBorder(10));

        this.setOpaque(false);
        this.setFocusPainted(false);
        this.setBorderPainted(false);
        this.setContentAreaFilled(false);
        setBorder(BorderFactory.createEmptyBorder(0,0,0,0)); // Especially important
    }

    private static class RoundedBorder implements Border
    {
        private int radius;

        RoundedBorder (int radius)
        {
            this.radius= radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
        {
            g.drawRoundRect(x, y, width-1, height-1, radius, radius);
        }

        @Override
        public Insets getBorderInsets(Component c)
        {
            return new Insets(this.radius+1, this.radius+1, this.radius+2, this.radius);
        }

        @Override
        public boolean isBorderOpaque()
        {
            return true;
        }
    }
}
