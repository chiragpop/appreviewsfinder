//
// Copyright (C) 2009 Ben Jaques.
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// - Redistributions of source code must retain the above copyright notice, this
//   list of conditions and the following disclaimer.
//
// - Redistributions in binary form must reproduce the above copyright notice,
//   this list of conditions and the following disclaimer in the documentation
//   and/or other materials provided with the distribution.
//
// - Neither the name of the author nor the names of its contributors may be used
//   to endorse or promote products derived from this software without specific
//   prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//

package uk.co.massycat.appreviewsfinder.ratings;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JPanel;

/**
 *
 * @author ben
 */
public class RatingsBarView extends JPanel {
    Icon[] mIcons = null;
    int[] mValues = null;
    int mTotal;
    final int PREFERRED_BAR_HEIGHT = 30;

    public void setValuesAndIcons( int[] values, Icon[] icons) {
        mValues = values.clone();
        mIcons = icons;

        mTotal = 0;
        for (int i = 0; i < mValues.length; i++) {
            mTotal += mValues[i];
        }

        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        if ( mValues == null) {
            return new Dimension(250, PREFERRED_BAR_HEIGHT);
        }
        return new Dimension(250,mValues.length * PREFERRED_BAR_HEIGHT);
    }


    public RatingsBarView() {
        mTotal = 0;
        setBackground(Color.white);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if ( mValues == null || mValues.length == 0 || mTotal == 0) {
            return;
        }

        // Draw Text
        //g.drawString("This is my custom Panel!",10,20);

        Font font = g.getFont();
        Dimension size = getSize();

        //g.setColor(Color.red);
        //g.drawRect(0, 0, size.width - 1, size.height - 1);

        float bar_height = size.height / mValues.length;

        g.setColor(Color.LIGHT_GRAY);
        for ( int i = 0; i < mValues.length; i++) {
            int width = (int)((float)size.width / (float)mTotal * mValues[i]);
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, (int)(i * bar_height), width, (int)bar_height);
            g.setColor(Color.DARK_GRAY);
            g.drawRect(0, (int)(i * bar_height), size.width - 1, (int)bar_height);

            float percent = (float)mValues[i] / (float)mTotal * 100.f;
            int int_percent = (int)percent;
            int icon_width = 0;

            if ( mIcons != null && mIcons[i] != null) {
                mIcons[i].paintIcon(null, g, 5, (int)((i) * bar_height) + 5);
                icon_width = mIcons[i].getIconWidth();
            }

            g.drawString(Integer.toString(mValues[i]) + " (" + int_percent + "%)",
                    5 + 5 + icon_width, (int)((i + 1) * bar_height) - 5);
            
        }
    }
}
